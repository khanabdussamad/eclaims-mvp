package com.nagarro.eclaims.payment.controller;

import com.nagarro.eclaims.common.response.ApiResponse;
import com.nagarro.eclaims.payment.dto.InitiatePaymentRequest;
import com.nagarro.eclaims.payment.dto.CompletePaymentRequest;
import com.nagarro.eclaims.payment.dto.PaymentResponse;
import com.nagarro.eclaims.payment.dto.PaymentListResponse;
import com.nagarro.eclaims.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/claims")
@Tag(name = "Payments", description = "Payment processing")
@SecurityRequirement(name = "bearer-jwt")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/{claimId}/payments/initiate")
    @PreAuthorize("hasAuthority('PAYMENT_INITIATE')")
    @Operation(summary = "Initiate payment",
               description = "Initiate a payment for an approved claim")
    public ResponseEntity<ApiResponse<PaymentResponse>> initiatePayment(
            @PathVariable UUID claimId,
            @RequestBody InitiatePaymentRequest request) {
        log.info("Initiating payment for claim: {}", claimId);

        PaymentResponse response = paymentService.initiatePayment(claimId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment initiated successfully", response));
    }

    @PostMapping("/{claimId}/payments/mock-complete")
    @PreAuthorize("hasAuthority('PAYMENT_COMPLETE')")
    @Operation(summary = "Complete payment (mock)",
               description = "Complete a payment for testing purposes (mock implementation)")
    public ResponseEntity<ApiResponse<PaymentResponse>> completePaymentMock(
            @PathVariable UUID claimId,
            @RequestBody CompletePaymentRequest request) {
        log.info("Completing payment for claim: {}", claimId);

        PaymentResponse response = paymentService.completePaymentMock(claimId, request);

        return ResponseEntity.ok(
                ApiResponse.success("Payment completed successfully", response));
    }

    @GetMapping("/{claimId}/payments")
    @PreAuthorize("hasAuthority('CLAIM_VIEW_ALL') or hasAuthority('CLAIM_VIEW_OWN')")
    @Operation(summary = "Get claim payments",
               description = "Retrieve all payments for a specific claim")
    public ResponseEntity<ApiResponse<List<PaymentListResponse>>> getClaimPayments(
            @PathVariable UUID claimId) {
        log.info("Fetching payments for claim: {}", claimId);

        List<PaymentListResponse> payments = paymentService.getClaimPayments(claimId);

        return ResponseEntity.ok(
                ApiResponse.success("Payments retrieved successfully", payments));
    }
}

