package com.nagarro.eclaims.workflow.dto;

public record AvailableTransitionResponse(
        String fromStatus,
        String toStatus,
        String requiredPermission,
        String description
) {}

