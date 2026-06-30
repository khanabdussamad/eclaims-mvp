package com.nagarro.eclaims.workshop.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record SubmitFinalInvoiceRequest(
    @NotBlank(message = "Invoice number is required")
    String invoiceNumber,

    @NotNull(message = "Invoice amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Invoice amount must be positive")
    BigDecimal invoiceAmount
) {}

