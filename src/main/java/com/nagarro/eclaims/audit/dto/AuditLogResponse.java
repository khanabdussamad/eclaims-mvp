package com.nagarro.eclaims.audit.dto;

import java.time.Instant;

public record AuditLogResponse(
        String id,
        String actionType,
        String entityType,
        String relatedEntityId,
        String performedByUserName,
        String performedByRole,
        String description,
        Boolean successful,
        Instant createdAt
) {}

