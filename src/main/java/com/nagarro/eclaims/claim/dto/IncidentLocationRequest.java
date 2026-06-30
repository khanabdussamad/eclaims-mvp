package com.nagarro.eclaims.claim.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record IncidentLocationRequest(
    @NotBlank(message = "Address line 1 is required")
    String addressLine1,
    
    @NotBlank(message = "City is required")
    String city,
    
    @NotBlank(message = "State is required")
    String state,
    
    @NotBlank(message = "Zip code is required")
    String zipCode,
    
    @NotBlank(message = "Country is required")
    String country
) {}

