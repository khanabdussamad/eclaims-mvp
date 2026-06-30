package com.nagarro.eclaims.reporting.dto;

import java.math.BigDecimal;

public record ClaimsSummaryResponse(
        Long totalClaims,
        Long submittedClaims,
        Long approvedClaims,
        Long rejectedClaims,
        Long closedClaims,
        BigDecimal totalApprovedAmount,
        Double averageProcessingTimeInDays
) {}

