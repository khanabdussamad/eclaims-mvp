package com.nagarro.eclaims.payment.dto;

public record CompletePaymentRequest(
        String paymentReference,
        String externalTransactionId
) {}

