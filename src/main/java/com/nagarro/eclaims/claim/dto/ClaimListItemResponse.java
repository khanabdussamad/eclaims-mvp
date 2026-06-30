package com.nagarro.eclaims.claim.dto;

import java.time.Instant;
import java.time.LocalDate;

public record ClaimListItemResponse(
    String claimId,
    String claimNumber,
    String policyNumber,
    String customerName,
    String claimType,
    String status,
    LocalDate incidentDate,
    Instant submittedAt
) {}

