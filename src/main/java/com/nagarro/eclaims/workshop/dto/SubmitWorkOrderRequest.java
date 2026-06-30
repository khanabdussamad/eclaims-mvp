package com.nagarro.eclaims.workshop.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record SubmitWorkOrderRequest(
    @NotBlank(message = "Description is required")
    String description,

    @NotNull(message = "Estimate amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Estimate amount must be positive")
    BigDecimal estimateAmount
) {}

