package com.nagarro.eclaims.payment.service;

import com.nagarro.eclaims.adjudication.repository.AdjustorDecisionRepository;
import com.nagarro.eclaims.claim.entity.Claim;
import com.nagarro.eclaims.claim.repository.ClaimRepository;
import com.nagarro.eclaims.common.exception.BusinessException;
import com.nagarro.eclaims.common.exception.ResourceNotFoundException;
import com.nagarro.eclaims.payment.dto.InitiatePaymentRequest;
import com.nagarro.eclaims.payment.dto.CompletePaymentRequest;
import com.nagarro.eclaims.payment.dto.PaymentResponse;
import com.nagarro.eclaims.payment.dto.PaymentListResponse;
import com.nagarro.eclaims.payment.entity.Payment;
import com.nagarro.eclaims.payment.repository.PaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ClaimRepository claimRepository;
    private final AdjustorDecisionRepository adjustorDecisionRepository;

    public PaymentService(PaymentRepository paymentRepository,
                        ClaimRepository claimRepository,
                        AdjustorDecisionRepository adjustorDecisionRepository) {
        this.paymentRepository = paymentRepository;
        this.claimRepository = claimRepository;
        this.adjustorDecisionRepository = adjustorDecisionRepository;
    }

    public PaymentResponse initiatePayment(UUID claimId, InitiatePaymentRequest request) {
        log.info("Initiating payment for claim: {}", claimId);

        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim", claimId.toString()));

        // Get the approved amount from adjudication
        BigDecimal paymentAmount = adjustorDecisionRepository.findByClaimId(claimId)
                .map(decision -> decision.getApprovedAmount() != null ?
                    decision.getApprovedAmount() : BigDecimal.ZERO)
                .orElseThrow(() -> new BusinessException("NO_ADJUDICATION",
                        "Claim must have an approved adjudication before payment"));

        if (paymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("INVALID_AMOUNT", "Payment amount must be greater than zero");
        }

        // Check if payment already exists for this claim
        paymentRepository.findByClaimIdAndStatus(claimId, "INITIATED")
                .ifPresent(existing -> {
                    throw new BusinessException("PAYMENT_EXISTS",
                            "A payment has already been initiated for this claim");
                });

        BigDecimal deductible = claim.getPolicy().getDeductibleAmount();
        BigDecimal finalPaymentAmount = paymentAmount.subtract(deductible);

        Payment payment = Payment.builder()
                .claim(claim)
                .paymentStatus("INITIATED")
                .paymentAmount(finalPaymentAmount)
                .appliedDeductible(deductible)
                .paymentMethod(request.paymentMethod())
                .initiatedAt(Instant.now())
                .paymentNotes(request.paymentNotes())
                .build();

        payment = paymentRepository.save(payment);

        log.info("Payment initiated successfully for claim: {}", claimId);

        return mapToResponse(payment);
    }

    public PaymentResponse completePaymentMock(UUID claimId, CompletePaymentRequest request) {
        log.info("Completing payment (mock) for claim: {}", claimId);

        Payment payment = paymentRepository.findByClaimIdAndStatus(claimId, "INITIATED")
                .orElseThrow(() -> new BusinessException("NO_INITIATED_PAYMENT",
                        "No initiated payment found for this claim"));

        payment.setPaymentStatus("COMPLETED");
        payment.setCompletedAt(Instant.now());
        payment.setPaymentReference(request.paymentReference());
        payment.setExternalPaymentGatewayId(request.externalTransactionId());
        payment.setExternalTransactionDate(Instant.now());

        payment = paymentRepository.save(payment);

        log.info("Payment completed successfully for claim: {}", claimId);

        return mapToResponse(payment);
    }

    public PaymentResponse getPayment(UUID claimId) {
        log.info("Getting payment for claim: {}", claimId);

        Payment payment = paymentRepository.findByClaimIdAndStatus(claimId, "COMPLETED")
                .orElseThrow(() -> new ResourceNotFoundException("Payment",
                        "No completed payment found for claim: " + claimId));

        return mapToResponse(payment);
    }

    public List<PaymentListResponse> getClaimPayments(UUID claimId) {
        log.info("Getting all payments for claim: {}", claimId);

        return paymentRepository.findByClaimId(claimId).stream()
                .map(payment -> new PaymentListResponse(
                        payment.getClaim().getClaimNumber(),
                        payment.getPaymentAmount(),
                        payment.getPaymentStatus(),
                        payment.getInitiatedAt(),
                        payment.getCompletedAt()
                ))
                .collect(Collectors.toList());
    }

    public List<Payment> getPendingPayments() {
        log.info("Fetching pending payments");
        return paymentRepository.findPendingPayments();
    }

    public void failPayment(UUID paymentId, String failureReason) {
        log.info("Failing payment: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId.toString()));

        payment.setPaymentStatus("FAILED");
        payment.setPaymentNotes(failureReason);
        paymentRepository.save(payment);

        log.info("Payment marked as failed: {}", paymentId);
    }

    private PaymentResponse mapToResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId().toString(),
                payment.getClaim().getId().toString(),
                payment.getClaim().getClaimNumber(),
                payment.getPaymentStatus(),
                payment.getPaymentAmount(),
                payment.getAppliedDeductible(),
                payment.getPaymentMethod(),
                payment.getPaymentReference(),
                payment.getInitiatedAt(),
                payment.getCompletedAt()
        );
    }
}

