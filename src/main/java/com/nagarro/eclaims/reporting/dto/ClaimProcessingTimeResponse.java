package com.nagarro.eclaims.reporting.dto;

public record ClaimProcessingTimeResponse(
        Double averageSubmissionToAssignmentDays,
        Double averageAssignmentToSurveyCompletionDays,
        Double averageSurveyToAdjudicationDays,
        Double averageAdjudicationToPaymentDays,
        Double averageTotalProcessingDays
) {}

