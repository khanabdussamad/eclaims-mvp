package com.nagarro.eclaims.workshop.entity;

import com.nagarro.eclaims.claim.entity.Claim;
import com.nagarro.eclaims.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "repair_updates", indexes = {
    @Index(name = "idx_repair_updates_claim_id", columnList = "claim_id"),
    @Index(name = "idx_repair_updates_workshop_id", columnList = "workshop_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepairUpdate extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "claim_id", nullable = false)
    private Claim claim;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workshop_id", nullable = false)
    private Workshop workshop;

    @Column(name = "repair_status", nullable = false, length = 64)
    private String repairStatus; // NOT_STARTED, IN_PROGRESS, PARTS_WAITING, QUALITY_CHECK, COMPLETED, DELIVERED

    @Column(name = "progress_percentage", nullable = false)
    private Integer progressPercentage;

    @Column(name = "expected_delivery_date")
    private LocalDate expectedDeliveryDate;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
}

