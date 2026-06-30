package com.nagarro.eclaims.common.exception;

public class InvalidWorkflowTransitionException extends BusinessException {
    public InvalidWorkflowTransitionException(String fromStatus, String toStatus) {
        super("INVALID_WORKFLOW_TRANSITION",
              String.format("Cannot transition from %s to %s", fromStatus, toStatus));
    }

    public InvalidWorkflowTransitionException(String message) {
        super("INVALID_WORKFLOW_TRANSITION", message);
    }
}

