package com.nagarro.eclaims.assignment.service;

import com.nagarro.eclaims.assignment.entity.ClaimAssignment;
import com.nagarro.eclaims.assignment.enums.AssignmentRole;
import com.nagarro.eclaims.assignment.repository.ClaimAssignmentRepository;
import com.nagarro.eclaims.user.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional(readOnly = true)
public class UserAvailabilityService {

    private final ClaimAssignmentRepository claimAssignmentRepository;
    private static final Long DEFAULT_CAPACITY = 100L;
    private static final Long CASE_MANAGER_CAPACITY = 20L;
    private static final Long SURVEYOR_CAPACITY = 30L;
    private static final Long ADJUSTOR_CAPACITY = 25L;

    public UserAvailabilityService(ClaimAssignmentRepository claimAssignmentRepository) {
        this.claimAssignmentRepository = claimAssignmentRepository;
    }

    public Long getActiveClaimsCount(UUID userId) {
        return claimAssignmentRepository.countActiveAssignmentsByUser(userId);
    }

    public Long getCapacityForRole(AssignmentRole role) {
        return switch (role) {
            case CASE_MANAGER -> CASE_MANAGER_CAPACITY;
            case SURVEYOR -> SURVEYOR_CAPACITY;
            case ADJUSTOR -> ADJUSTOR_CAPACITY;
        };
    }

    public Double getUtilizationPercentage(UUID userId, AssignmentRole role) {
        Long activeCount = getActiveClaimsCount(userId);
        Long capacity = getCapacityForRole(role);
        return (double) activeCount / capacity * 100;
    }

    public Boolean isUserAvailable(UUID userId, AssignmentRole role) {
        Long activeCount = getActiveClaimsCount(userId);
        Long capacity = getCapacityForRole(role);
        return activeCount < capacity;
    }

    public Boolean isUserOverCapacity(UUID userId, AssignmentRole role) {
        return !isUserAvailable(userId, role);
    }

    public List<ClaimAssignment> getActiveAssignmentsForUser(UUID userId) {
        return claimAssignmentRepository.findActiveAssignmentsByUser(userId);
    }

    public List<ClaimAssignment> getUncompletedAssignmentsByRole(AssignmentRole role) {
        return claimAssignmentRepository.findUncompletedByRole(role);
    }

    public Long countUncompletedAssignmentsByRole(AssignmentRole role) {
        return (long) claimAssignmentRepository.findUncompletedByRole(role).size();
    }
}

