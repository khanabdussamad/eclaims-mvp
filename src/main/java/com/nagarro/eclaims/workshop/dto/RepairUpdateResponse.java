package com.nagarro.eclaims.workshop.dto;

import java.time.LocalDate;
import java.time.Instant;

public record RepairUpdateResponse(
    String repairUpdateId,
    String claimId,
    String repairStatus,
    Integer progressPercentage,
    LocalDate expectedDeliveryDate,
    String remarks,
    Instant createdAt
) {}

