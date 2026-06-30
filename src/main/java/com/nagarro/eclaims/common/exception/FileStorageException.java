package com.nagarro.eclaims.common.exception;

public class FileStorageException extends BusinessException {
    public FileStorageException(String message) {
        super("FILE_STORAGE_ERROR", message);
    }

    public FileStorageException(String message, Throwable cause) {
        super("FILE_STORAGE_ERROR", message, cause);
    }
}

