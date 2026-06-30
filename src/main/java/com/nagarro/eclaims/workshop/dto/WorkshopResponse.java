package com.nagarro.eclaims.workshop.dto;

public record WorkshopResponse(
    String workshopId,
    String name,
    String partnerCode,
    String city,
    String state,
    String zipCode,
    String phone,
    String email
) {}

