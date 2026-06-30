package com.nagarro.eclaims.assignment.dto;

import com.nagarro.eclaims.assignment.enums.AssignmentRole;

public record ManualAssignmentRequest(
        String assignedUserId,
        AssignmentRole assignmentRole,
        String reason
) {}

