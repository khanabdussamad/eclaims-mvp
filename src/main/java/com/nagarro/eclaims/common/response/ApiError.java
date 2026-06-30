package com.nagarro.eclaims.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
    String code,
    String message,
    List<FieldErrorDetail> fieldErrors
) {
    public ApiError(String code, String message) {
        this(code, message, null);
    }
}

