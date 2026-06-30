package com.nagarro.eclaims.workshop.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record RepairUpdateRequest(
    @NotBlank(message = "Repair status is required")
    String repairStatus,

    @NotNull(message = "Progress percentage is required")
    @Min(value = 0, message = "Progress percentage must be between 0 and 100")
    @Max(value = 100, message = "Progress percentage must be between 0 and 100")
    Integer progressPercentage,

    LocalDate expectedDeliveryDate,

    String remarks
) {}

