package com.nagarro.eclaims.claim.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalTime;

public record CreateClaimRequest(
    @NotBlank(message = "Policy number is required")
    String policyNumber,

    @NotNull(message = "Claim type is required")
    String claimType,

    @NotNull(message = "Incident date is required")
    LocalDate incidentDate,

    @NotNull(message = "Incident time is required")
    LocalTime incidentTime,

    @NotNull(message = "Incident location is required")
    IncidentLocationRequest incidentLocation,

    @NotBlank(message = "Description is required")
    @Size(max = 4000, message = "Description cannot exceed 4000 characters")
    String description,

    @NotNull(message = "Vehicle drivable is required")
    Boolean vehicleDrivable,

    @NotNull(message = "Police report available is required")
    Boolean policeReportAvailable
) {}

