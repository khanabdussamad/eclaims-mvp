package com.nagarro.eclaims.assignment.dto;

public record UserWorkloadResponse(
        String userId,
        String userName,
        Long activeClaimsCount,
        Long totalCapacity,
        Double utilizationPercentage
) {}

