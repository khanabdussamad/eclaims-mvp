package com.nagarro.eclaims.adjudication.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record AdjudicationResponse(
        String id,
        String claimId,
        String claimNumber,
        String adjustorName,
        String decision,
        BigDecimal approvedAmount,
        String rationale,
        String denialReason,
        Instant decisionDate,
        Instant submittedAt
) {}

