package com.nagarro.eclaims.workflow.service;

import com.nagarro.eclaims.claim.entity.Claim;
import com.nagarro.eclaims.claim.entity.ClaimStatusHistory;
import com.nagarro.eclaims.claim.enums.ClaimStatus;
import com.nagarro.eclaims.claim.repository.ClaimRepository;
import com.nagarro.eclaims.claim.repository.ClaimStatusHistoryRepository;
import com.nagarro.eclaims.common.exception.BusinessException;
import com.nagarro.eclaims.common.exception.InvalidWorkflowTransitionException;
import com.nagarro.eclaims.common.exception.ResourceNotFoundException;
import com.nagarro.eclaims.user.entity.User;
import com.nagarro.eclaims.user.repository.UserRepository;
import com.nagarro.eclaims.workflow.dto.AvailableTransitionResponse;
import com.nagarro.eclaims.workflow.dto.ClaimStatusUpdateResponse;
import com.nagarro.eclaims.workflow.entity.WorkflowTransition;
import com.nagarro.eclaims.workflow.repository.WorkflowTransitionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class ClaimWorkflowService {

    private final WorkflowTransitionRepository workflowTransitionRepository;
    private final ClaimRepository claimRepository;
    private final ClaimStatusHistoryRepository claimStatusHistoryRepository;
    private final UserRepository userRepository;
    private final WorkflowValidationService workflowValidationService;

    public ClaimWorkflowService(WorkflowTransitionRepository workflowTransitionRepository,
                               ClaimRepository claimRepository,
                               ClaimStatusHistoryRepository claimStatusHistoryRepository,
                               UserRepository userRepository,
                               WorkflowValidationService workflowValidationService) {
        this.workflowTransitionRepository = workflowTransitionRepository;
        this.claimRepository = claimRepository;
        this.claimStatusHistoryRepository = claimStatusHistoryRepository;
        this.userRepository = userRepository;
        this.workflowValidationService = workflowValidationService;
    }

    public ClaimStatusUpdateResponse transitionClaim(UUID claimId, String newStatus, String reason,
                                                      String comments, UUID userId) {
        log.info("Attempting to transition claim {} to status {} by user {}", claimId, newStatus, userId);

        // Validate claim exists
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim", claimId.toString()));

        String currentStatus = claim.getCurrentStatus();

        // Validate transition exists
        if (!canTransition(currentStatus, newStatus)) {
            throw new InvalidWorkflowTransitionException(
                    String.format("Cannot transition from %s to %s", currentStatus, newStatus)
            );
        }

        // Get transition and validate
        WorkflowTransition transition = workflowTransitionRepository
                .findTransition(currentStatus, newStatus)
                .orElseThrow(() -> new InvalidWorkflowTransitionException(
                        String.format("Transition from %s to %s is not allowed", currentStatus, newStatus)
                ));

        // Validate domain preconditions
        workflowValidationService.validatePreconditions(claim, transition);

        // Update claim status
        claim.setCurrentStatus(newStatus);
        claim.setUpdatedAt(Instant.now());
        claim = claimRepository.save(claim);

        // Create status history entry
        User changedByUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));

        ClaimStatusHistory history = ClaimStatusHistory.builder()
                .claim(claim)
                .fromStatus(currentStatus)
                .toStatus(newStatus)
                .changedByUser(changedByUser)
                .changedByRole(transition.getActorRole())
                .reason(reason)
                .comments(comments)
                .createdAt(Instant.now())
                .build();

        claimStatusHistoryRepository.save(history);

        log.info("Successfully transitioned claim {} from {} to {}", claimId, currentStatus, newStatus);

        return new ClaimStatusUpdateResponse(
                claimId.toString(),
                currentStatus,
                newStatus,
                reason,
                Instant.now()
        );
    }

    public List<AvailableTransitionResponse> getAvailableTransitions(UUID claimId) {
        log.info("Fetching available transitions for claim {}", claimId);

        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim", claimId.toString()));

        String currentStatus = claim.getCurrentStatus();

        return workflowTransitionRepository.findAvailableTransitionsFrom(currentStatus)
                .stream()
                .map(transition -> new AvailableTransitionResponse(
                        transition.getFromStatus(),
                        transition.getToStatus(),
                        transition.getRequiredPermission(),
                        transition.getDescription()
                ))
                .collect(Collectors.toList());
    }

    public boolean canTransition(String fromStatus, String toStatus) {
        return workflowTransitionRepository
                .findTransition(fromStatus, toStatus)
                .isPresent();
    }

    public Set<String> getRequiredPermissionsForTransition(String fromStatus, String toStatus) {
        return workflowTransitionRepository
                .findTransition(fromStatus, toStatus)
                .map(transition -> Set.of(transition.getRequiredPermission()))
                .orElse(Set.of());
    }

    public List<WorkflowTransition> getAvailableTransitionsForStatus(String currentStatus) {
        return workflowTransitionRepository.findAvailableTransitionsFrom(currentStatus);
    }

    public WorkflowTransition getTransition(String fromStatus, String toStatus) {
        return workflowTransitionRepository
                .findTransition(fromStatus, toStatus)
                .orElseThrow(() -> new InvalidWorkflowTransitionException(
                        String.format("No valid transition from %s to %s", fromStatus, toStatus)
                ));
    }
}

