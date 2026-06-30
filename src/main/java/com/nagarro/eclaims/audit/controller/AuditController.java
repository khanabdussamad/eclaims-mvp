package com.nagarro.eclaims.audit.controller;

import com.nagarro.eclaims.audit.dto.AuditLogResponse;
import com.nagarro.eclaims.audit.dto.AuditTrailResponse;
import com.nagarro.eclaims.audit.service.AuditService;
import com.nagarro.eclaims.common.response.ApiResponse;
import com.nagarro.eclaims.common.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/audit")
@Tag(name = "Audit", description = "Audit logging and trails")
@SecurityRequirement(name = "bearer-jwt")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping("/claims/{claimId}")
    @PreAuthorize("hasAuthority('AUDIT_VIEW')")
    @Operation(summary = "Get claim audit trail",
               description = "Retrieve the audit trail for a specific claim")
    public ResponseEntity<ApiResponse<PageResponse<AuditLogResponse>>> getClaimAuditTrail(
            @PathVariable UUID claimId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        log.info("Fetching audit trail for claim: {}", claimId);

        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLogResponse> auditLogs = auditService.getClaimAuditTrail(claimId.toString(), pageable);

        PageResponse<AuditLogResponse> pageResponse = new PageResponse<>(
                auditLogs.getContent(),
                auditLogs.getNumber(),
                auditLogs.getSize(),
                auditLogs.getTotalElements(),
                auditLogs.getTotalPages(),
                auditLogs.isLast()
        );

        return ResponseEntity.ok(
                ApiResponse.success("Audit trail retrieved successfully", pageResponse)
        );
    }

    @GetMapping("/users/{userId}")
    @PreAuthorize("hasAuthority('AUDIT_VIEW')")
    @Operation(summary = "Get user audit trail",
               description = "Retrieve the audit trail for actions performed by a user")
    public ResponseEntity<ApiResponse<PageResponse<AuditLogResponse>>> getUserAuditTrail(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        log.info("Fetching audit trail for user: {}", userId);

        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLogResponse> auditLogs = auditService.getUserAuditTrail(userId, pageable);

        PageResponse<AuditLogResponse> pageResponse = new PageResponse<>(
                auditLogs.getContent(),
                auditLogs.getNumber(),
                auditLogs.getSize(),
                auditLogs.getTotalElements(),
                auditLogs.getTotalPages(),
                auditLogs.isLast()
        );

        return ResponseEntity.ok(
                ApiResponse.success("User audit trail retrieved successfully", pageResponse)
        );
    }
}

