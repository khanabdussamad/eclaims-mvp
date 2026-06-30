package com.nagarro.eclaims.claim.entity;

import com.nagarro.eclaims.common.entity.BaseEntity;
import com.nagarro.eclaims.policy.entity.Policy;
import com.nagarro.eclaims.user.entity.Customer;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "claims")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Claim extends BaseEntity {

    @Column(nullable = false, unique = true, length = 64)
    private String claimNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false, length = 32)
    private String claimType; // ACCIDENT, THEFT, FIRE, FLOOD, VANDALISM, OTHER

    @Column(nullable = false, length = 64)
    private String currentStatus; // ClaimStatus enum

    @Column(nullable = false)
    private LocalDate incidentDate;

    @Column(nullable = false)
    private LocalTime incidentTime;

    @Column(nullable = false, length = 255)
    private String incidentAddressLine1;

    @Column(nullable = false, length = 128)
    private String incidentCity;

    @Column(nullable = false, length = 128)
    private String incidentState;

    @Column(nullable = false, length = 32)
    private String incidentZipCode;

    @Column(nullable = false, length = 128)
    private String incidentCountry;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Boolean vehicleDrivable;

    @Column(nullable = false)
    private Boolean policeReportAvailable;

    @Column(nullable = false)
    private Instant submittedAt;

    @Column
    private Instant closedAt;
}

