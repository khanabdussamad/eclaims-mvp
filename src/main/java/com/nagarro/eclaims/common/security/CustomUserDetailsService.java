package com.nagarro.eclaims.common.security;

import com.nagarro.eclaims.rbac.service.RbacService;
import com.nagarro.eclaims.user.entity.User;
import com.nagarro.eclaims.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RbacService rbacService;

    public CustomUserDetailsService(UserRepository userRepository, RbacService rbacService) {
        this.userRepository = userRepository;
        this.rbacService = rbacService;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User not found with email: {}", email);
                    return new UsernameNotFoundException(String.format("User not found with email: %s", email));
                });

        Set<String> roles = user.getRoles()
                .stream()
                .map(role -> role.getCode())
                .collect(Collectors.toSet());

        Set<String> permissions = rbacService.getPermissionsForRoles(roles);

        boolean enabled = "ACTIVE".equals(user.getStatus());

        return new CustomUserDetails(user.getId(), user.getEmail(), user.getPasswordHash(), roles, permissions, enabled);
    }
}

