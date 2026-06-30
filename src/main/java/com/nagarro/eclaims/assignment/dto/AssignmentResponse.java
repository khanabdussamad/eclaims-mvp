package com.nagarro.eclaims.assignment.dto;

import com.nagarro.eclaims.assignment.enums.AssignmentRole;
import java.time.Instant;

public record AssignmentResponse(
        String id,
        String claimId,
        String assignedUserId,
        String assignedUserName,
        AssignmentRole assignmentRole,
        Boolean isActive,
        Instant assignedAt,
        Instant completedAt
) {}

