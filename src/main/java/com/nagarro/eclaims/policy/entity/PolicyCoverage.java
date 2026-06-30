package com.nagarro.eclaims.policy.entity;

import com.nagarro.eclaims.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "policy_coverages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyCoverage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;

    @Column(nullable = false, length = 64)
    private String coverageType;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal coverageLimit;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal deductibleAmount;
}

