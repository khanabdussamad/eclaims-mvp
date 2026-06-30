package com.nagarro.eclaims.auth.service;

import com.nagarro.eclaims.auth.dto.CurrentUserResponse;
import com.nagarro.eclaims.auth.dto.LoginRequest;
import com.nagarro.eclaims.auth.dto.LoginResponse;
import com.nagarro.eclaims.auth.dto.RegisterCustomerRequest;
import com.nagarro.eclaims.auth.dto.RegisterCustomerResponse;
import com.nagarro.eclaims.common.exception.BusinessException;
import com.nagarro.eclaims.common.exception.DuplicateResourceException;
import com.nagarro.eclaims.common.exception.ResourceNotFoundException;
import com.nagarro.eclaims.common.security.CustomUserDetailsService;
import com.nagarro.eclaims.common.security.JwtTokenProvider;
import com.nagarro.eclaims.policy.entity.Policy;
import com.nagarro.eclaims.policy.repository.PolicyRepository;
import com.nagarro.eclaims.rbac.entity.Role;
import com.nagarro.eclaims.rbac.repository.RoleRepository;
import com.nagarro.eclaims.rbac.service.RbacService;
import com.nagarro.eclaims.user.entity.Customer;
import com.nagarro.eclaims.user.entity.User;
import com.nagarro.eclaims.user.enums.UserStatus;
import com.nagarro.eclaims.user.repository.CustomerRepository;
import com.nagarro.eclaims.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PolicyRepository policyRepository;
    private final RoleRepository roleRepository;
    private final RbacService rbacService;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailsService customUserDetailsService;

    public AuthService(AuthenticationManager authenticationManager,
                      UserRepository userRepository,
                      CustomerRepository customerRepository,
                      PolicyRepository policyRepository,
                      RoleRepository roleRepository,
                      RbacService rbacService,
                      JwtTokenProvider jwtTokenProvider,
                      PasswordEncoder passwordEncoder,
                      CustomUserDetailsService customUserDetailsService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.policyRepository = policyRepository;
        this.roleRepository = roleRepository;
        this.rbacService = rbacService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.customUserDetailsService = customUserDetailsService;
    }

    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.email());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );

            User user = userRepository.findByEmail(request.email())
                    .orElseThrow(() -> new ResourceNotFoundException("User", request.email()));

            // Update last login time
            user.setLastLoginAt(Instant.now());
            userRepository.save(user);

            Set<String> roles = user.getRoles()
                    .stream()
                    .map(Role::getCode)
                    .collect(Collectors.toSet());

            Set<String> permissions = rbacService.getPermissionsForRoles(roles);

            String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), roles, permissions);

            CurrentUserResponse userResponse = new CurrentUserResponse(
                    user.getId().toString(),
                    user.getFullName(),
                    user.getEmail(),
                    roles,
                    permissions
            );

            log.info("User logged in successfully: {}", request.email());
            return new LoginResponse(token, "Bearer", jwtTokenProvider.getExpirationTime(), userResponse);

        } catch (BadCredentialsException e) {
            log.warn("Invalid credentials for email: {}", request.email());
            throw new BusinessException("INVALID_CREDENTIALS", "Invalid email or password");
        }
    }

    public RegisterCustomerResponse registerCustomer(RegisterCustomerRequest request) {
        log.info("Customer registration attempt for email: {}", request.email());

        // Check if email already exists
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("User", "email", request.email());
        }

        // Validate policy exists and is active
        Policy policy = policyRepository.findByPolicyNumber(request.policyNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Policy", request.policyNumber()));

        if (!"ACTIVE".equals(policy.getPolicyStatus())) {
            throw new BusinessException("POLICY_NOT_ACTIVE", "Policy must be active to register");
        }

        // Get customer role
        Role customerRole = roleRepository.findByCode("CUSTOMER")
                .orElseThrow(() -> new ResourceNotFoundException("Role", "CUSTOMER"));

        // Create user
        User user = User.builder()
                .fullName(request.lastName())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .status(UserStatus.ACTIVE.toString())
                .roles(Set.of(customerRole))
                .build();

        user = userRepository.save(user);

        // Create customer record
        Customer customer = Customer.builder()
                .user(user)
                .customerNumber("CUST-" + System.currentTimeMillis())
                .firstName(request.lastName())
                .lastName(request.lastName())
                .phone(request.phone())
                .billingCycle("MONTHLY")
                .build();

        customer = customerRepository.save(customer);

        log.info("Customer registered successfully: {}", request.email());

        return new RegisterCustomerResponse(
                user.getId().toString(),
                customer.getId().toString(),
                user.getEmail()
        );
    }

    public CurrentUserResponse getCurrentUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));

        Set<String> roles = user.getRoles()
                .stream()
                .map(Role::getCode)
                .collect(Collectors.toSet());

        Set<String> permissions = rbacService.getPermissionsForRoles(roles);

        return new CurrentUserResponse(
                user.getId().toString(),
                user.getFullName(),
                user.getEmail(),
                roles,
                permissions
        );
    }
}

