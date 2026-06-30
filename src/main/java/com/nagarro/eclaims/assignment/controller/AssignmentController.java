package com.nagarro.eclaims.assignment.controller;

import com.nagarro.eclaims.assignment.dto.*;
import com.nagarro.eclaims.assignment.service.AssignmentService;
import com.nagarro.eclaims.common.response.ApiResponse;
import com.nagarro.eclaims.common.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/claims")
@Tag(name = "Assignments", description = "Claim assignment management")
@SecurityRequirement(name = "bearer-jwt")
public class AssignmentController {

    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @PostMapping("/{claimId}/assignments/auto")
    @PreAuthorize("hasAuthority('CLAIM_ASSIGN')")
    @Operation(summary = "Auto-assign a claim",
               description = "Automatically assign a claim to the least busy user with the required role")
    public ResponseEntity<ApiResponse<AssignmentResponse>> autoAssignClaim(
            @PathVariable UUID claimId,
            @RequestBody AssignmentRequest request,
            Principal principal) {
        log.info("Auto-assigning claim: {} with role: {}", claimId, request.assignmentRole());

        UUID userId = UUID.fromString(principal.getName());
        AssignmentResponse response = assignmentService.autoAssignClaim(claimId, request.assignmentRole(), userId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Claim auto-assigned successfully", response));
    }

    @PostMapping("/{claimId}/assignments/reassign")
    @PreAuthorize("hasAuthority('CLAIM_REASSIGN')")
    @Operation(summary = "Manually reassign a claim",
               description = "Manually reassign a claim to a specific user")
    public ResponseEntity<ApiResponse<AssignmentResponse>> reassignClaim(
            @PathVariable UUID claimId,
            @RequestBody ManualAssignmentRequest request,
            Principal principal) {
        log.info("Reassigning claim: {} to user: {}", claimId, request.assignedUserId());

        UUID userId = UUID.fromString(principal.getName());
        AssignmentResponse response = assignmentService.manualAssign(
                claimId,
                UUID.fromString(request.assignedUserId()),
                request.assignmentRole(),
                request.reason(),
                userId
        );

        return ResponseEntity.ok(
                ApiResponse.success("Claim reassigned successfully", response));
    }

    @GetMapping("/{claimId}/assignments")
    @PreAuthorize("hasAuthority('CLAIM_VIEW_ALL') or hasAuthority('CLAIM_VIEW_OWN')")
    @Operation(summary = "Get claim assignments",
               description = "Retrieve all active assignments for a specific claim")
    public ResponseEntity<ApiResponse<List<AssignmentListResponse>>> getClaimAssignments(
            @PathVariable UUID claimId) {
        log.info("Fetching assignments for claim: {}", claimId);

        List<AssignmentListResponse> assignments = assignmentService.getClaimAssignments(claimId);

        return ResponseEntity.ok(
                ApiResponse.success("Assignments retrieved successfully", assignments));
    }

    @GetMapping("/user/assignments")
    @PreAuthorize("authenticated")
    @Operation(summary = "Get user assignments",
               description = "Retrieve assignments for the authenticated user")
    public ResponseEntity<ApiResponse<PageResponse<AssignmentListResponse>>> getUserAssignments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Principal principal) {
        log.info("Fetching assignments for user: {}", principal.getName());

        UUID userId = UUID.fromString(principal.getName());
        // Note: This is a simplified version. Full implementation should determine role from user's roles
        Pageable pageable = PageRequest.of(page, size);
        Page<AssignmentListResponse> assignments = assignmentService.getUserAssignments(userId, null, pageable);

        PageResponse<AssignmentListResponse> pageResponse = new PageResponse<>(
                assignments.getContent(),
                assignments.getNumber(),
                assignments.getSize(),
                assignments.getTotalElements(),
                assignments.getTotalPages(),
                assignments.isLast()
        );

        return ResponseEntity.ok(
                ApiResponse.success("User assignments retrieved successfully", pageResponse));
    }

    @GetMapping("/user/{userId}/workload")
    @PreAuthorize("hasAuthority('CLAIM_VIEW_ALL')")
    @Operation(summary = "Get user workload",
               description = "Get the current workload and utilization for a user")
    public ResponseEntity<ApiResponse<UserWorkloadResponse>> getUserWorkload(
            @PathVariable UUID userId) {
        log.info("Fetching workload for user: {}", userId);

        UserWorkloadResponse workload = assignmentService.getUserWorkload(userId);

        return ResponseEntity.ok(
                ApiResponse.success("User workload retrieved successfully", workload));
    }
}

