package com.nagarro.eclaims.payment.dto;

public record InitiatePaymentRequest(
        String paymentMethod,
        String paymentNotes
) {}

