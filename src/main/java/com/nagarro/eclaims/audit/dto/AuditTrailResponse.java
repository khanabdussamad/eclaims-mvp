package com.nagarro.eclaims.audit.dto;

import java.time.Instant;
import java.util.List;

public record AuditTrailResponse(
        String claimId,
        String claimNumber,
        Long totalActions,
        List<AuditLogResponse> actions
) {}

