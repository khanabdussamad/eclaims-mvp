package com.nagarro.eclaims.policy.entity;

import com.nagarro.eclaims.common.entity.BaseEntity;
import com.nagarro.eclaims.user.entity.Customer;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "policies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Policy extends BaseEntity {

    @Column(nullable = false, unique = true, length = 64)
    private String policyNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false, length = 64)
    private String vehicleVin;

    @Column(nullable = false, length = 100)
    private String vehicleMake;

    @Column(nullable = false, length = 100)
    private String vehicleModel;

    @Column(nullable = false)
    private Integer vehicleYear;

    @Column(nullable = false, length = 32)
    private String policyStatus; // ACTIVE, INACTIVE, EXPIRED, CANCELLED

    @Column(nullable = false)
    private LocalDate effectiveDate;

    @Column(nullable = false)
    private LocalDate expiryDate;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal deductibleAmount;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal coverageLimit;
}

