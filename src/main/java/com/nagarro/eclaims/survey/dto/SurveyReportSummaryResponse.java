package com.nagarro.eclaims.survey.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record SurveyReportSummaryResponse(
        String id,
        String claimNumber,
        String customerName,
        String damageSeverity,
        BigDecimal estimatedRepairCost,
        Instant submittedAt
) {}

