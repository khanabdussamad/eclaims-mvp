package com.nagarro.eclaims.common.response;

public record FieldErrorDetail(
    String field,
    String message
) {}

