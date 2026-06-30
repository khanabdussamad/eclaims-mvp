package com.nagarro.eclaims.adjudication.dto;

import java.math.BigDecimal;

public record AdjustorClaimResponse(
        String claimId,
        String claimNumber,
        String customerName,
        String incidentZipCode,
        BigDecimal estimatedCost,
        String surveyStatus
) {}

