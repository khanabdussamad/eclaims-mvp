package com.nagarro.eclaims.reporting.dto;

public record ClaimsByRoleResponse(
        String roleName,
        Long assignedClaimsCount,
        Long completedClaimsCount,
        Double completionRate
) {}

