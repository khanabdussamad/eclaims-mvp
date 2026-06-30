package com.nagarro.eclaims.auth.dto;

public record RegisterCustomerResponse(
    String userId,
    String customerId,
    String email
) {}

