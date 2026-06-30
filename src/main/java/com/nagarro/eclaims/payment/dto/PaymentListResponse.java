package com.nagarro.eclaims.payment.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentListResponse(
        String claimNumber,
        BigDecimal paymentAmount,
        String paymentStatus,
        Instant initiatedAt,
        Instant completedAt
) {}

