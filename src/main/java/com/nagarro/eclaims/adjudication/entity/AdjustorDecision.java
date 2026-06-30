package com.nagarro.eclaims.adjudication.entity;

import com.nagarro.eclaims.claim.entity.Claim;
import com.nagarro.eclaims.common.entity.BaseEntity;
import com.nagarro.eclaims.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "adjustor_decisions", indexes = {
        @Index(name = "idx_decision_claim", columnList = "claim_id"),
        @Index(name = "idx_decision_adjustor", columnList = "adjustor_user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdjustorDecision extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "claim_id", nullable = false)
    private Claim claim;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "adjustor_user_id", nullable = false)
    private User adjustorUser;

    @Column(nullable = false, length = 50)
    private String decision; // APPROVED, REJECTED, NEEDS_MORE_INFO

    @Column(precision = 10, scale = 2)
    private BigDecimal approvedAmount;

    @Column(columnDefinition = "TEXT")
    private String rationale;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @Column(length = 500)
    private String denialReason;

    @Column
    private Instant decisionDate;

    @Column
    private Instant submittedAt;

    @Column(precision = 10, scale = 2)
    private BigDecimal coverageLimit;

    @Column(precision = 10, scale = 2)
    private BigDecimal deductibleAmount;
}

