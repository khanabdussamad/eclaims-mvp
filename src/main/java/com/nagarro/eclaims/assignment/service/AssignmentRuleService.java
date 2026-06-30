package com.nagarro.eclaims.assignment.service;

import com.nagarro.eclaims.assignment.entity.ClaimAssignment;
import com.nagarro.eclaims.assignment.enums.AssignmentRole;
import com.nagarro.eclaims.assignment.repository.ClaimAssignmentRepository;
import com.nagarro.eclaims.common.exception.BusinessException;
import com.nagarro.eclaims.rbac.entity.Role;
import com.nagarro.eclaims.rbac.repository.RoleRepository;
import com.nagarro.eclaims.user.entity.User;
import com.nagarro.eclaims.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
public class AssignmentRuleService {

    private final UserRepository userRepository;
    private final ClaimAssignmentRepository claimAssignmentRepository;
    private final RoleRepository roleRepository;
    private final UserAvailabilityService userAvailabilityService;

    public AssignmentRuleService(UserRepository userRepository,
                                ClaimAssignmentRepository claimAssignmentRepository,
                                RoleRepository roleRepository,
                                UserAvailabilityService userAvailabilityService) {
        this.userRepository = userRepository;
        this.claimAssignmentRepository = claimAssignmentRepository;
        this.roleRepository = roleRepository;
        this.userAvailabilityService = userAvailabilityService;
    }

    public Optional<User> findLeastBusyUserForRole(AssignmentRole assignmentRole) {
        log.debug("Finding least busy user for role: {}", assignmentRole);

        String roleCode = getRoleCodeForAssignmentRole(assignmentRole);
        Role dbRole = roleRepository.findByCode(roleCode)
                .orElseThrow(() -> new BusinessException("ROLE_NOT_FOUND", "Role not found: " + roleCode));

        // Get all users with this role
        List<User> usersWithRole = userRepository.findByRolesContaining(dbRole);

        // Filter available users and sort by least busy
        return usersWithRole.stream()
                .filter(user -> userAvailabilityService.isUserAvailable(user.getId(), assignmentRole))
                .min(Comparator.comparingLong(user -> userAvailabilityService.getActiveClaimsCount(user.getId())));
    }

    public void validateUserHasRole(User user, AssignmentRole assignmentRole) {
        log.debug("Validating user {} has role {}", user.getId(), assignmentRole);

        String requiredRoleCode = getRoleCodeForAssignmentRole(assignmentRole);
        boolean hasRole = user.getRoles().stream()
                .anyMatch(role -> role.getCode().equalsIgnoreCase(requiredRoleCode));

        if (!hasRole) {
            throw new BusinessException("USER_ROLE_MISMATCH",
                    "User does not have role: " + assignmentRole);
        }
    }

    public List<User> findAvailableUsersForRole(AssignmentRole assignmentRole) {
        log.debug("Finding available users for role: {}", assignmentRole);

        String roleCode = getRoleCodeForAssignmentRole(assignmentRole);
        Role dbRole = roleRepository.findByCode(roleCode)
                .orElseThrow(() -> new BusinessException("ROLE_NOT_FOUND", "Role not found: " + roleCode));

        // Get all users with this role
        return userRepository.findByRolesContaining(dbRole).stream()
                .filter(user -> userAvailabilityService.isUserAvailable(user.getId(), assignmentRole))
                .sorted(Comparator.comparingLong(user -> userAvailabilityService.getActiveClaimsCount(user.getId())))
                .collect(Collectors.toList());
    }

    public List<User> getOverCapacityUsers(AssignmentRole assignmentRole) {
        log.debug("Finding over-capacity users for role: {}", assignmentRole);

        String roleCode = getRoleCodeForAssignmentRole(assignmentRole);
        Role dbRole = roleRepository.findByCode(roleCode)
                .orElseThrow(() -> new BusinessException("ROLE_NOT_FOUND", "Role not found: " + roleCode));

        return userRepository.findByRolesContaining(dbRole).stream()
                .filter(user -> userAvailabilityService.isUserOverCapacity(user.getId(), assignmentRole))
                .collect(Collectors.toList());
    }

    public Double calculateAvgWorkload(AssignmentRole assignmentRole) {
        log.debug("Calculating average workload for role: {}", assignmentRole);

        String roleCode = getRoleCodeForAssignmentRole(assignmentRole);
        Role dbRole = roleRepository.findByCode(roleCode)
                .orElseThrow(() -> new BusinessException("ROLE_NOT_FOUND", "Role not found: " + roleCode));

        List<User> usersWithRole = userRepository.findByRolesContaining(dbRole);

        if (usersWithRole.isEmpty()) {
            return 0.0;
        }

        return usersWithRole.stream()
                .mapToLong(user -> userAvailabilityService.getActiveClaimsCount(user.getId()))
                .average()
                .orElse(0.0);
    }

    public boolean shouldRebalanceWorkload(AssignmentRole assignmentRole) {
        Double avgWorkload = calculateAvgWorkload(assignmentRole);
        Long capacity = userAvailabilityService.getCapacityForRole(assignmentRole);

        // Rebalance if average workload is above 75% capacity
        return (avgWorkload / capacity) > 0.75;
    }

    private String getRoleCodeForAssignmentRole(AssignmentRole assignmentRole) {
        return switch (assignmentRole) {
            case CASE_MANAGER -> "CASE_MANAGER";
            case SURVEYOR -> "SURVEYOR";
            case ADJUSTOR -> "ADJUSTOR";
        };
    }
}

