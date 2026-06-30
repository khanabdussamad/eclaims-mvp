package com.nagarro.eclaims.document.dto;

import java.time.Instant;

public record DocumentListResponse(
    String documentId,
    String documentType,
    String originalFileName,
    Long fileSize,
    String uploadedByName,
    Instant uploadedAt
) {}

