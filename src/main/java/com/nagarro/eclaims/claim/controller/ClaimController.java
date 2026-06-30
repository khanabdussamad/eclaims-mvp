package com.nagarro.eclaims.claim.controller;

import com.nagarro.eclaims.claim.dto.ClaimCreatedResponse;
import com.nagarro.eclaims.claim.dto.ClaimDetailResponse;
import com.nagarro.eclaims.claim.dto.ClaimListItemResponse;
import com.nagarro.eclaims.claim.dto.CreateClaimRequest;
import com.nagarro.eclaims.claim.entity.ClaimStatusHistory;
import com.nagarro.eclaims.claim.service.ClaimService;
import com.nagarro.eclaims.common.response.ApiResponse;
import com.nagarro.eclaims.common.response.PageResponse;
import com.nagarro.eclaims.common.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/claims")
@Tag(name = "Claims", description = "Claim management endpoints")
public class ClaimController {

    private final ClaimService claimService;

    public ClaimController(ClaimService claimService) {
        this.claimService = claimService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CLAIM_CREATE')")
    @Operation(summary = "Create claim", description = "Create a new claim for a policy")
    public ResponseEntity<ApiResponse<ClaimCreatedResponse>> createClaim(
            @Valid @RequestBody CreateClaimRequest request) {
        log.info("Create claim request received");
        UUID customerId = SecurityUtils.getCurrentUserId();
        ClaimCreatedResponse response = claimService.createClaim(request, customerId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Claim submitted successfully.", response));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('CLAIM_VIEW_SELF', 'CLAIM_VIEW_ASSIGNED', 'CLAIM_VIEW_ALL')")
    @Operation(summary = "List claims", description = "List claims with pagination")
    public ResponseEntity<ApiResponse<PageResponse<ClaimListItemResponse>>> listClaims(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("List claims request received - page: {}, size: {}", page, size);
        UUID customerId = SecurityUtils.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        Page<ClaimListItemResponse> claims = claimService.listClaims(customerId, pageable);

        PageResponse<ClaimListItemResponse> pageResponse = new PageResponse<>(
                claims.getContent(),
                claims.getNumber(),
                claims.getSize(),
                claims.getTotalElements(),
                claims.getTotalPages(),
                claims.isLast()
        );

        return ResponseEntity.ok(ApiResponse.success("Claims fetched.", pageResponse));
    }

    @GetMapping("/{claimId}")
    @PreAuthorize("hasAnyAuthority('CLAIM_VIEW_SELF', 'CLAIM_VIEW_ASSIGNED', 'CLAIM_VIEW_ALL')")
    @Operation(summary = "Get claim details", description = "Get detailed information about a claim")
    public ResponseEntity<ApiResponse<ClaimDetailResponse>> getClaimDetail(@PathVariable UUID claimId) {
        log.info("Get claim detail request for claim: {}", claimId);
        UUID userId = SecurityUtils.getCurrentUserId();
        ClaimDetailResponse response = claimService.getClaimDetail(claimId, userId);
        return ResponseEntity.ok(ApiResponse.success("Claim details fetched.", response));
    }

    @GetMapping("/{claimId}/timeline")
    @PreAuthorize("hasAnyAuthority('CLAIM_VIEW_SELF', 'CLAIM_VIEW_ASSIGNED', 'CLAIM_VIEW_ALL')")
    @Operation(summary = "Get claim timeline", description = "Get status history timeline for a claim")
    public ResponseEntity<ApiResponse<List<ClaimStatusHistory>>> getClaimTimeline(@PathVariable UUID claimId) {
        log.info("Get claim timeline request for claim: {}", claimId);
        UUID userId = SecurityUtils.getCurrentUserId();
        List<ClaimStatusHistory> timeline = claimService.getClaimTimeline(claimId, userId);
        return ResponseEntity.ok(ApiResponse.success("Claim timeline fetched.", timeline));
    }
}

