package com.nagarro.eclaims.auth.controller;

import com.nagarro.eclaims.auth.dto.CurrentUserResponse;
import com.nagarro.eclaims.auth.dto.LoginRequest;
import com.nagarro.eclaims.auth.dto.LoginResponse;
import com.nagarro.eclaims.auth.dto.RegisterCustomerRequest;
import com.nagarro.eclaims.auth.dto.RegisterCustomerResponse;
import com.nagarro.eclaims.auth.service.AuthService;
import com.nagarro.eclaims.common.response.ApiResponse;
import com.nagarro.eclaims.common.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Authentication and authorization endpoints")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Authenticate user and get JWT token")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login endpoint called for email: {}", request.email());
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful.", response));
    }

    @PostMapping("/register-customer")
    @Operation(summary = "Register customer", description = "Register a new customer using policy number")
    public ResponseEntity<ApiResponse<RegisterCustomerResponse>> registerCustomer(
            @Valid @RequestBody RegisterCustomerRequest request) {
        log.info("Customer registration endpoint called for email: {}", request.email());
        RegisterCustomerResponse response = authService.registerCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Customer registered successfully.", response));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Get profile of currently authenticated user")
    public ResponseEntity<ApiResponse<CurrentUserResponse>> getCurrentUser() {
        UUID userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Not authenticated", null));
        }

        log.info("Getting profile for user: {}", userId);
        CurrentUserResponse response = authService.getCurrentUser(userId);
        return ResponseEntity.ok(ApiResponse.success("Current user profile fetched.", response));
    }
}

