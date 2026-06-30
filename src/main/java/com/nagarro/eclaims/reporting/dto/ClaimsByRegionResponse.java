package com.nagarro.eclaims.reporting.dto;

import java.math.BigDecimal;

public record ClaimsByRegionResponse(
        String region,
        Long claimCount,
        Long approvedCount,
        BigDecimal totalAmount,
        Double approvalRate
) {}

