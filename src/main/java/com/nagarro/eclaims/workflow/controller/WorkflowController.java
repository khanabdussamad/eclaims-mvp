package com.nagarro.eclaims.workflow.controller;

import com.nagarro.eclaims.common.response.ApiResponse;
import com.nagarro.eclaims.workflow.dto.AvailableTransitionResponse;
import com.nagarro.eclaims.workflow.dto.ClaimStatusUpdateResponse;
import com.nagarro.eclaims.workflow.dto.UpdateClaimStatusRequest;
import com.nagarro.eclaims.workflow.service.ClaimWorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/claims")
@Tag(name = "Workflow", description = "Claim status workflow transitions")
@SecurityRequirement(name = "bearer-jwt")
public class WorkflowController {

    private final ClaimWorkflowService claimWorkflowService;

    public WorkflowController(ClaimWorkflowService claimWorkflowService) {
        this.claimWorkflowService = claimWorkflowService;
    }

    @PatchMapping("/{claimId}/status")
    @PreAuthorize("hasAuthority('CLAIM_STATUS_UPDATE')")
    @Operation(summary = "Update claim status with workflow validation",
            description = "Transition a claim to a new status based on allowed workflow transitions")
    public ResponseEntity<ApiResponse<ClaimStatusUpdateResponse>> updateClaimStatus(
            @PathVariable UUID claimId,
            @RequestBody UpdateClaimStatusRequest request,
            Principal principal) {
        log.info("Updating claim status for claim: {}", claimId);

        UUID userId = UUID.fromString(principal.getName());
        ClaimStatusUpdateResponse response = claimWorkflowService.transitionClaim(
                claimId,
                request.newStatus(),
                request.reason(),
                request.comments(),
                userId
        );

        return ResponseEntity.ok(
                ApiResponse.success("Claim status updated successfully", response)
        );
    }

    @GetMapping("/{claimId}/transitions")
    @PreAuthorize("hasAuthority('CLAIM_VIEW_ALL') or hasAuthority('CLAIM_VIEW_OWN')")
    @Operation(summary = "Get available status transitions",
            description = "Retrieve all valid status transitions for a claim from its current state")
    public ResponseEntity<ApiResponse<List<AvailableTransitionResponse>>> getAvailableTransitions(
            @PathVariable UUID claimId) {
        log.info("Fetching available transitions for claim: {}", claimId);

        List<AvailableTransitionResponse> transitions = claimWorkflowService.getAvailableTransitions(claimId);

        return ResponseEntity.ok(
                ApiResponse.success("Available transitions retrieved successfully", transitions)
        );
    }
}

