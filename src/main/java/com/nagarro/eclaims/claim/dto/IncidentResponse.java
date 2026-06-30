package com.nagarro.eclaims.claim.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record IncidentResponse(
    LocalDate incidentDate,
    LocalTime incidentTime,
    String addressLine1,
    String city,
    String state,
    String zipCode,
    String country,
    String description
) {}

