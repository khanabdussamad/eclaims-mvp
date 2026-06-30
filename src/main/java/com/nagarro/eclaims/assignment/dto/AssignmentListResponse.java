package com.nagarro.eclaims.assignment.dto;

import com.nagarro.eclaims.assignment.enums.AssignmentRole;
import java.time.Instant;

public record AssignmentListResponse(
        String claimId,
        String claimNumber,
        AssignmentRole assignmentRole,
        String assignedUserName,
        Instant assignedAt,
        Boolean isActive
) {}

