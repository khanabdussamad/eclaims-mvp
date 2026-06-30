package com.nagarro.eclaims.survey.dto;

import java.math.BigDecimal;

public record SubmitSurveyReportRequest(
        String damageSeverity,
        String damageAssessment,
        String repairRecommendations,
        BigDecimal estimatedRepairCost,
        String observations,
        String surveyNotes
) {}

