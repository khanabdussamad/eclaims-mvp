package com.nagarro.eclaims.workflow.dto;

public record WorkflowTransitionResponse(
        String id,
        String fromStatus,
        String toStatus,
        String requiredPermission,
        String actorRole,
        Boolean active
) {}

