package com.nagarro.eclaims.document.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DocumentUploadRequest(
    @NotBlank(message = "Document type is required")
    String documentType,

    @NotNull(message = "File size is required")
    @Positive(message = "File size must be positive")
    Long fileSize,

    @NotBlank(message = "Content type is required")
    String contentType
) {}

