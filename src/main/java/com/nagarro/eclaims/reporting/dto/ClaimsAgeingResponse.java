package com.nagarro.eclaims.reporting.dto;

public record ClaimsAgeingResponse(
        Long claimsLessThan7Days,
        Long claimsBetween7And30Days,
        Long claimsBetween30And60Days,
        Long claimsMoreThan60Days,
        Long pendingClaims
) {}

