package com.nagarro.eclaims.common.exception;

import com.nagarro.eclaims.common.response.ApiError;
import com.nagarro.eclaims.common.response.ApiResponse;
import com.nagarro.eclaims.common.response.FieldErrorDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleResourceNotFound(
            ResourceNotFoundException ex, WebRequest request) {
        log.warn("Resource not found: {}", ex.getMessage());
        ApiError error = new ApiError(ex.getErrorCode(), ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Resource not found.", error));
    }

    @ExceptionHandler(InvalidWorkflowTransitionException.class)
    public ResponseEntity<ApiResponse<?>> handleInvalidWorkflowTransition(
            InvalidWorkflowTransitionException ex, WebRequest request) {
        log.warn("Invalid workflow transition: {}", ex.getMessage());
        ApiError error = new ApiError(ex.getErrorCode(), ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiResponse.error("Workflow transition not allowed.", error));
    }

    @ExceptionHandler(AccessDeniedBusinessException.class)
    public ResponseEntity<ApiResponse<?>> handleAccessDenied(
            AccessDeniedBusinessException ex, WebRequest request) {
        log.warn("Access denied: {}", ex.getMessage());
        ApiError error = new ApiError(ex.getErrorCode(), ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Access denied.", error));
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<?>> handleDuplicateResource(
            DuplicateResourceException ex, WebRequest request) {
        log.warn("Duplicate resource: {}", ex.getMessage());
        ApiError error = new ApiError(ex.getErrorCode(), ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("Resource already exists.", error));
    }

    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<ApiResponse<?>> handleFileStorageError(
            FileStorageException ex, WebRequest request) {
        log.error("File storage error: {}", ex.getMessage(), ex);
        ApiError error = new ApiError(ex.getErrorCode(), ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("File storage operation failed.", error));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationError(
            ValidationException ex, WebRequest request) {
        log.warn("Validation error: {}", ex.getMessage());
        ApiError error = new ApiError(ex.getErrorCode(), ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Validation failed.", error));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<?>> handleBusinessException(
            BusinessException ex, WebRequest request) {
        log.warn("Business error: {}", ex.getMessage());
        ApiError error = new ApiError(ex.getErrorCode(), ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Business rule violation.", error));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request) {
        log.warn("Validation error in request: {}", ex.getMessage());

        List<FieldErrorDetail> fieldErrors = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(error -> {
                    String fieldName = error instanceof FieldError
                            ? ((FieldError) error).getField()
                            : error.getObjectName();
                    String message = error.getDefaultMessage();
                    return new FieldErrorDetail(fieldName, message);
                })
                .collect(Collectors.toList());

        ApiError error = new ApiError("VALIDATION_ERROR", "One or more fields are invalid.", fieldErrors);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Validation failed.", error));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<?>> handleAuthenticationException(
            AuthenticationException ex, WebRequest request) {
        log.warn("Authentication error: {}", ex.getMessage());
        ApiError error = new ApiError("AUTHENTICATION_ERROR", "Invalid credentials or authentication failed.");
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Authentication failed.", error));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<?>> handleAccessDeniedException(
            AccessDeniedException ex, WebRequest request) {
        log.warn("Access denied: {}", ex.getMessage());
        ApiError error = new ApiError("AUTHORIZATION_ERROR", "You do not have permission to access this resource.");
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Access denied.", error));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGlobalException(
            Exception ex, WebRequest request) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        ApiError error = new ApiError("INTERNAL_SERVER_ERROR", "An unexpected error occurred. Please try again later.");
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An error occurred.", error));
    }
}

