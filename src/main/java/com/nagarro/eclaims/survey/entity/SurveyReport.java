package com.nagarro.eclaims.survey.entity;

import com.nagarro.eclaims.claim.entity.Claim;
import com.nagarro.eclaims.common.entity.BaseEntity;
import com.nagarro.eclaims.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.math.BigDecimal;

@Entity
@Table(name = "survey_reports", indexes = {
        @Index(name = "idx_survey_claim", columnList = "claim_id"),
        @Index(name = "idx_survey_surveyor", columnList = "surveyor_user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveyReport extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "claim_id", nullable = false)
    private Claim claim;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "surveyor_user_id", nullable = false)
    private User surveyorUser;

    @Column(nullable = false, length = 50)
    private String damageSeverity; // MINOR, MODERATE, SEVERE, TOTAL_LOSS

    @Column(columnDefinition = "TEXT")
    private String damageAssessment;

    @Column(columnDefinition = "TEXT")
    private String repairRecommendations;

    @Column(precision = 10, scale = 2)
    private BigDecimal estimatedRepairCost;

    @Column(columnDefinition = "TEXT")
    private String observations;

    @Column(length = 50)
    private String surveyStatus; // PENDING, COMPLETED, SUBMITTED

    @Column
    private Instant surveyDate;

    @Column
    private Instant submittedAt;

    @Column(length = 500)
    private String surveyNotes;
}

