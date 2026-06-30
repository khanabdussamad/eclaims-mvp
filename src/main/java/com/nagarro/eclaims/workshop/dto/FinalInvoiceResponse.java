package com.nagarro.eclaims.workshop.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record FinalInvoiceResponse(
    String invoiceId,
    String claimId,
    String workshopName,
    String invoiceNumber,
    BigDecimal invoiceAmount,
    String currency,
    Instant submittedAt
) {}

