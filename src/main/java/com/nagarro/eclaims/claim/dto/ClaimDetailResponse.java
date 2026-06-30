package com.nagarro.eclaims.claim.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

public record ClaimDetailResponse(
    String claimId,
    String claimNumber,
    String status,
    String claimType,
    PolicySummaryResponse policy,
    CustomerSummaryResponse customer,
    IncidentResponse incident,
    boolean vehicleDrivable,
    boolean policeReportAvailable,
    Instant submittedAt
) {}



