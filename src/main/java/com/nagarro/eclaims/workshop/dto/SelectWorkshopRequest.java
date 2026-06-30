package com.nagarro.eclaims.workshop.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record SelectWorkshopRequest(
    @NotNull(message = "Workshop ID is required")
    UUID workshopId
) {}

