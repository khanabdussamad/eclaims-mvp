package com.nagarro.eclaims.workflow.dto;

public record UpdateClaimStatusRequest(
        String newStatus,
        String reason,
        String comments
) {}

