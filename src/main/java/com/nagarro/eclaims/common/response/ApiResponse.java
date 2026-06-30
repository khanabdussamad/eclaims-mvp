package com.nagarro.eclaims.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
    boolean success,
    String message,
    T data,
    ApiError error,
    Instant timestamp
) {
    public ApiResponse(boolean success, String message, T data) {
        this(success, message, data, null, Instant.now());
    }

    public ApiResponse(boolean success, String message, ApiError error) {
        this(success, message, null, error, Instant.now());
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Operation completed successfully.", data);
    }

    public static <T> ApiResponse<T> error(String message, ApiError error) {
        return new ApiResponse<>(false, message, error);
    }
}

