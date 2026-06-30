package com.nagarro.eclaims.auth.dto;

import java.util.Set;

public record CurrentUserResponse(
    String id,
    String fullName,
    String email,
    Set<String> roles,
    Set<String> permissions
) {}

