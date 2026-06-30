package com.nagarro.eclaims.common.exception;

public class AccessDeniedBusinessException extends BusinessException {
    public AccessDeniedBusinessException(String message) {
        super("ACCESS_DENIED", message);
    }

    public AccessDeniedBusinessException(String reason, String entity) {
        super("ACCESS_DENIED", String.format("Access denied: %s - %s", reason, entity));
    }
}

