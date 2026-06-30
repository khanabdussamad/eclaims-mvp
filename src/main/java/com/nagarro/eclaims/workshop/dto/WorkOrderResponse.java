package com.nagarro.eclaims.workshop.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record WorkOrderResponse(
    String workOrderId,
    String claimId,
    String workshopName,
    BigDecimal estimateAmount,
    String currency,
    String description,
    Instant submittedAt
) {}

