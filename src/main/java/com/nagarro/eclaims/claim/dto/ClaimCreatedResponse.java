package com.nagarro.eclaims.claim.dto;

import java.time.Instant;

public record ClaimCreatedResponse(
    String claimId,
    String claimNumber,
    String status,
    Instant createdAt
) {}

