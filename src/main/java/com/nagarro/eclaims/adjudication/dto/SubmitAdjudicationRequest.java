package com.nagarro.eclaims.adjudication.dto;

import java.math.BigDecimal;

public record SubmitAdjudicationRequest(
        String decision,
        BigDecimal approvedAmount,
        String rationale,
        String remarks,
        String denialReason
) {}

