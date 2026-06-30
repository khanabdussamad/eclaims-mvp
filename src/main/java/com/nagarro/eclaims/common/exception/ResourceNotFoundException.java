package com.nagarro.eclaims.common.exception;

public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String resource, String identifier) {
        super("RESOURCE_NOT_FOUND", String.format("%s not found: %s", resource, identifier));
    }

    public ResourceNotFoundException(String message) {
        super("RESOURCE_NOT_FOUND", message);
    }
}

