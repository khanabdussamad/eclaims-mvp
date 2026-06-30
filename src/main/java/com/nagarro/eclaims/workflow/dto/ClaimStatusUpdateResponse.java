package com.nagarro.eclaims.workflow.dto;

import java.time.Instant;

public record ClaimStatusUpdateResponse(
        String claimId,
        String previousStatus,
        String newStatus,
        String reason,
        Instant updatedAt
) {}

