package com.nagarro.eclaims.reporting.dto;

public record FraudIndicatorsResponse(
        Long highValueClaimsCount,
        Long claimsWithFrequentResubmissions,
        Long claimsRejectedForFraud,
        Long claimsRequiringManualReview,
        Double potentialFraudPercentage
) {}

