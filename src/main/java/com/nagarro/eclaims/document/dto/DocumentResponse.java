package com.nagarro.eclaims.document.dto;

import java.time.Instant;

public record DocumentResponse(
    String documentId,
    String claimId,
    String documentType,
    String originalFileName,
    String contentType,
    Long fileSize,
    String uploadedByName,
    Integer version,
    Instant uploadedAt,
    String downloadUrl
) {}

