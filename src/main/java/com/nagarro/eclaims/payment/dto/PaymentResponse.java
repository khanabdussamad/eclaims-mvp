package com.nagarro.eclaims.payment.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(
        String id,
        String claimId,
        String claimNumber,
        String paymentStatus,
        BigDecimal paymentAmount,
        BigDecimal appliedDeductible,
        String paymentMethod,
        String paymentReference,
        Instant initiatedAt,
        Instant completedAt
) {}

