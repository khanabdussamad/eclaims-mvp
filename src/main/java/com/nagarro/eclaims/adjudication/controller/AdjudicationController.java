package com.nagarro.eclaims.adjudication.controller;

import com.nagarro.eclaims.adjudication.dto.SubmitAdjudicationRequest;
import com.nagarro.eclaims.adjudication.dto.AdjudicationResponse;
import com.nagarro.eclaims.adjudication.dto.AdjustorClaimResponse;
import com.nagarro.eclaims.adjudication.service.AdjudicationService;
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
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Adjudication", description = "Claim adjudication and approval management")
@SecurityRequirement(name = "bearer-jwt")
public class AdjudicationController {

    private final AdjudicationService adjudicationService;

    public AdjudicationController(AdjudicationService adjudicationService) {
        this.adjudicationService = adjudicationService;
    }

    @GetMapping("/adjustor/claims")
    @PreAuthorize("hasAuthority('ADJUDICATION_VIEW_ASSIGNED')")
    @Operation(summary = "Get adjustor's claim queue",
               description = "Retrieve claims assigned to the authenticated adjustor")
    public ResponseEntity<ApiResponse<PageResponse<AdjustorClaimResponse>>> getAdjustorClaimsQueue(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Principal principal) {
        log.info("Fetching adjudication queue for adjustor: {}", principal.getName());

        UUID adjustorId = UUID.fromString(principal.getName());
        Pageable pageable = PageRequest.of(page, size);

        Page<AdjustorClaimResponse> claims = adjudicationService.getAdjustorClaimsQueue(adjustorId, pageable);

        PageResponse<AdjustorClaimResponse> pageResponse = new PageResponse<>(
                claims.getContent(),
                claims.getNumber(),
                claims.getSize(),
                claims.getTotalElements(),
                claims.getTotalPages(),
                claims.isLast()
        );

        return ResponseEntity.ok(
                ApiResponse.success("Adjustor claims retrieved successfully", pageResponse)
        );
    }

    @PostMapping("/claims/{claimId}/adjudications")
    @PreAuthorize("hasAuthority('ADJUDICATION_APPROVE') or hasAuthority('ADJUDICATION_REJECT')")
    @Operation(summary = "Submit adjudication decision",
               description = "Submit an approval or rejection decision for a claim")
    public ResponseEntity<ApiResponse<AdjudicationResponse>> submitAdjudication(
            @PathVariable UUID claimId,
            @RequestBody SubmitAdjudicationRequest request,
            Principal principal) {
        log.info("Submitting adjudication for claim: {}", claimId);

        UUID adjustorId = UUID.fromString(principal.getName());
        AdjudicationResponse response = adjudicationService.submitAdjudication(claimId, request, adjustorId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Adjudication submitted successfully", response));
    }

    @GetMapping("/claims/{claimId}/adjudications")
    @PreAuthorize("hasAuthority('DOCUMENT_VIEW')")
    @Operation(summary = "Get adjudication for claim",
               description = "Retrieve the adjudication decision for a specific claim")
    public ResponseEntity<ApiResponse<AdjudicationResponse>> getAdjudication(
            @PathVariable UUID claimId) {
        log.info("Fetching adjudication for claim: {}", claimId);

        AdjudicationResponse adjudication = adjudicationService.getAdjudication(claimId);

        return ResponseEntity.ok(
                ApiResponse.success("Adjudication retrieved successfully", adjudication)
        );
    }
}

