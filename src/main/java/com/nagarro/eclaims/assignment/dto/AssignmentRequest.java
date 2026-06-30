package com.nagarro.eclaims.assignment.dto;

import com.nagarro.eclaims.assignment.enums.AssignmentRole;

public record AssignmentRequest(
        AssignmentRole assignmentRole,
        String reason
) {}

