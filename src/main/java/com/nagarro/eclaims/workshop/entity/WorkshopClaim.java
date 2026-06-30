package com.nagarro.eclaims.workshop.entity;

import com.nagarro.eclaims.claim.entity.Claim;
import com.nagarro.eclaims.common.entity.BaseEntity;
import com.nagarro.eclaims.user.entity.Customer;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "workshop_claims", indexes = {
    @Index(name = "idx_workshop_claims_claim_id", columnList = "claim_id"),
    @Index(name = "idx_workshop_claims_workshop_id", columnList = "workshop_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkshopClaim extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "claim_id", nullable = false, unique = true)
    private Claim claim;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workshop_id", nullable = false)
    private Workshop workshop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_by_customer_id", nullable = false)
    private Customer selectedByCustomer;

    @Column(name = "selected_at", nullable = false, updatable = false)
    private Instant selectedAt;

    @Column(name = "status", nullable = false, length = 64)
    @Builder.Default
    private String status = "SELECTED"; // SELECTED, IN_PROGRESS, COMPLETED

    @PrePersist
    protected void onCreate() {
        if (selectedAt == null) {
            selectedAt = Instant.now();
        }
    }
}

