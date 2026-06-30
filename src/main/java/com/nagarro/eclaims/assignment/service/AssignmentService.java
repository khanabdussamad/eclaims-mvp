package com.nagarro.eclaims.assignment.service;

import com.nagarro.eclaims.assignment.dto.*;
import com.nagarro.eclaims.assignment.entity.ClaimAssignment;
import com.nagarro.eclaims.assignment.enums.AssignmentRole;
import com.nagarro.eclaims.assignment.repository.ClaimAssignmentRepository;
import com.nagarro.eclaims.claim.entity.Claim;
import com.nagarro.eclaims.claim.repository.ClaimRepository;
import com.nagarro.eclaims.common.exception.BusinessException;
import com.nagarro.eclaims.common.exception.ResourceNotFoundException;
import com.nagarro.eclaims.user.entity.User;
import com.nagarro.eclaims.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class AssignmentService {

    private final ClaimAssignmentRepository claimAssignmentRepository;
    private final ClaimRepository claimRepository;
    private final UserRepository userRepository;
    private final UserAvailabilityService userAvailabilityService;
    private final AssignmentRuleService assignmentRuleService;

    public AssignmentService(ClaimAssignmentRepository claimAssignmentRepository,
                            ClaimRepository claimRepository,
                            UserRepository userRepository,
                            UserAvailabilityService userAvailabilityService,
                            AssignmentRuleService assignmentRuleService) {
        this.claimAssignmentRepository = claimAssignmentRepository;
        this.claimRepository = claimRepository;
        this.userRepository = userRepository;
        this.userAvailabilityService = userAvailabilityService;
        this.assignmentRuleService = assignmentRuleService;
    }

    public AssignmentResponse autoAssignClaim(UUID claimId, AssignmentRole assignmentRole, UUID assignedByUserId) {
        log.info("Auto-assigning claim {} with role {}", claimId, assignmentRole);

        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim", claimId.toString()));

        // Check if already assigned for this role
        claimAssignmentRepository.findActiveByClaimIdAndRole(claimId, assignmentRole)
                .ifPresent(existing -> {
                    throw new BusinessException("ALREADY_ASSIGNED",
                        "Claim already assigned to " + assignmentRole);
                });

        // Find least busy user with the role
        User assignedUser = assignmentRuleService.findLeastBusyUserForRole(assignmentRole)
                .orElseThrow(() -> new BusinessException("NO_AVAILABLE_USER",
                    "No available user for assignment role: " + assignmentRole));

        User assignedByUser = userRepository.findById(assignedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", assignedByUserId.toString()));

        ClaimAssignment assignment = ClaimAssignment.builder()
                .claim(claim)
                .assignedUser(assignedUser)
                .assignmentRole(assignmentRole)
                .assignedByUser(assignedByUser)
                .isActive(true)
                .assignedAt(Instant.now())
                .build();

        assignment = claimAssignmentRepository.save(assignment);

        log.info("Successfully auto-assigned claim {} to user {} as {}",
                claimId, assignedUser.getId(), assignmentRole);

        return mapToResponse(assignment);
    }

    public AssignmentResponse manualAssign(UUID claimId, UUID assignedUserId, AssignmentRole assignmentRole,
                                          String reason, UUID assignedByUserId) {
        log.info("Manually assigning claim {} to user {} with role {}", claimId, assignedUserId, assignmentRole);

        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim", claimId.toString()));

        User assignedUser = userRepository.findById(assignedUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", assignedUserId.toString()));

        User assignedByUser = userRepository.findById(assignedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", assignedByUserId.toString()));

        // Validate user has appropriate role
        assignmentRuleService.validateUserHasRole(assignedUser, assignmentRole);

        // Mark any existing assignment for this role as inactive
        claimAssignmentRepository.findActiveByClaimIdAndRole(claimId, assignmentRole)
                .ifPresent(existing -> {
                    existing.setIsActive(false);
                    existing.setCompletedAt(Instant.now());
                    existing.setCompletionNotes("Reassigned to " + assignedUser.getFullName());
                    claimAssignmentRepository.save(existing);
                });

        ClaimAssignment assignment = ClaimAssignment.builder()
                .claim(claim)
                .assignedUser(assignedUser)
                .assignmentRole(assignmentRole)
                .assignedByUser(assignedByUser)
                .isActive(true)
                .assignedAt(Instant.now())
                .completionNotes(reason)
                .build();

        assignment = claimAssignmentRepository.save(assignment);

        log.info("Successfully manually assigned claim {} to user {}", claimId, assignedUserId);

        return mapToResponse(assignment);
    }

    public List<AssignmentListResponse> getClaimAssignments(UUID claimId) {
        log.info("Fetching assignments for claim: {}", claimId);

        return claimAssignmentRepository.findActiveByClaimId(claimId)
                .stream()
                .map(assignment -> new AssignmentListResponse(
                        assignment.getClaim().getId().toString(),
                        assignment.getClaim().getClaimNumber(),
                        assignment.getAssignmentRole(),
                        assignment.getAssignedUser().getFullName(),
                        assignment.getAssignedAt(),
                        assignment.getIsActive()
                ))
                .collect(Collectors.toList());
    }

    public Page<AssignmentListResponse> getUserAssignments(UUID userId, AssignmentRole role, Pageable pageable) {
        log.info("Fetching assignments for user {} with role {}", userId, role);

        Page<ClaimAssignment> assignments = claimAssignmentRepository
                .findActiveAssignmentsByUserAndRole(userId, role, pageable);

        List<AssignmentListResponse> content = assignments.getContent()
                .stream()
                .map(assignment -> new AssignmentListResponse(
                        assignment.getClaim().getId().toString(),
                        assignment.getClaim().getClaimNumber(),
                        assignment.getAssignmentRole(),
                        assignment.getAssignedUser().getFullName(),
                        assignment.getAssignedAt(),
                        assignment.getIsActive()
                ))
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, assignments.getTotalElements());
    }

    public UserWorkloadResponse getUserWorkload(UUID userId) {
        log.info("Fetching workload for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));

        Long activeClaimsCount = claimAssignmentRepository.countActiveAssignmentsByUser(userId);

        return new UserWorkloadResponse(
                userId.toString(),
                user.getFullName(),
                activeClaimsCount,
                100L, // Default capacity
                (double) activeClaimsCount / 100 * 100
        );
    }

    public void completeAssignment(UUID assignmentId, String completionNotes) {
        log.info("Completing assignment: {}", assignmentId);

        ClaimAssignment assignment = claimAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment", assignmentId.toString()));

        assignment.setIsActive(false);
        assignment.setCompletedAt(Instant.now());
        assignment.setCompletionNotes(completionNotes);
        claimAssignmentRepository.save(assignment);

        log.info("Assignment completed: {}", assignmentId);
    }

    private AssignmentResponse mapToResponse(ClaimAssignment assignment) {
        return new AssignmentResponse(
                assignment.getId().toString(),
                assignment.getClaim().getId().toString(),
                assignment.getAssignedUser().getId().toString(),
                assignment.getAssignedUser().getFullName(),
                assignment.getAssignmentRole(),
                assignment.getIsActive(),
                assignment.getAssignedAt(),
                assignment.getCompletedAt()
        );
    }
}

