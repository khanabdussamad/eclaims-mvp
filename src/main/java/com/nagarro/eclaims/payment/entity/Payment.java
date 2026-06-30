package com.nagarro.eclaims.payment.entity;

import com.nagarro.eclaims.claim.entity.Claim;
import com.nagarro.eclaims.common.entity.BaseEntity;
import com.nagarro.eclaims.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_payment_claim", columnList = "claim_id"),
        @Index(name = "idx_payment_status", columnList = "payment_status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "claim_id", nullable = false)
    private Claim claim;

    @Column(nullable = false, length = 50)
    private String paymentStatus; // INITIATED, PENDING, COMPLETED, FAILED, CANCELLED

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal paymentAmount;

    @Column(precision = 10, scale = 2)
    private BigDecimal appliedDeductible;

    @Column(length = 100)
    private String paymentMethod; // BANK_TRANSFER, CHECK, CREDIT_CARD, etc.

    @Column(length = 100)
    private String paymentReference;

    @Column
    private Instant initiatedAt;

    @Column
    private Instant completedAt;

    @Column(columnDefinition = "TEXT")
    private String paymentNotes;

    @Column(length = 100)
    private String externalPaymentGatewayId;

    @Column
    private Instant externalTransactionDate;
}

