package com.nagarro.eclaims.survey.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record SurveyReportResponse(
        String id,
        String claimId,
        String claimNumber,
        String surveyorName,
        String damageSeverity,
        String damageAssessment,
        String repairRecommendations,
        BigDecimal estimatedRepairCost,
        String observations,
        String surveyStatus,
        Instant surveyDate,
        Instant submittedAt
) {}

