package com.nagarro.eclaims.common.exception;

public class DuplicateResourceException extends BusinessException {
    public DuplicateResourceException(String resource, String field, String value) {
        super("DUPLICATE_RESOURCE",
              String.format("%s with %s '%s' already exists", resource, field, value));
    }

    public DuplicateResourceException(String message) {
        super("DUPLICATE_RESOURCE", message);
    }
}

