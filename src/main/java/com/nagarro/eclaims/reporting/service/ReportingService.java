package com.nagarro.eclaims.reporting.service;

import com.nagarro.eclaims.claim.repository.ClaimRepository;
import com.nagarro.eclaims.reporting.dto.ClaimsSummaryResponse;
import com.nagarro.eclaims.reporting.dto.ClaimsAgeingResponse;
import com.nagarro.eclaims.reporting.dto.ClaimsByRegionResponse;
import com.nagarro.eclaims.reporting.dto.ClaimProcessingTimeResponse;
import com.nagarro.eclaims.reporting.dto.ClaimsByRoleResponse;
import com.nagarro.eclaims.reporting.dto.FraudIndicatorsResponse;
import com.nagarro.eclaims.adjudication.repository.AdjustorDecisionRepository;
import com.nagarro.eclaims.assignment.repository.ClaimAssignmentRepository;
import com.nagarro.eclaims.survey.repository.SurveyReportRepository;
import com.nagarro.eclaims.payment.repository.PaymentRepository;
import com.nagarro.eclaims.claim.entity.ClaimStatusHistory;
import com.nagarro.eclaims.claim.repository.ClaimStatusHistoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
public class ReportingService {

    private final ClaimRepository claimRepository;
    private final ClaimStatusHistoryRepository claimStatusHistoryRepository;
    private final AdjustorDecisionRepository adjustorDecisionRepository;
    private final ClaimAssignmentRepository claimAssignmentRepository;
    private final SurveyReportRepository surveyReportRepository;
    private final PaymentRepository paymentRepository;

    public ReportingService(ClaimRepository claimRepository,
                          ClaimStatusHistoryRepository claimStatusHistoryRepository,
                          AdjustorDecisionRepository adjustorDecisionRepository,
                          ClaimAssignmentRepository claimAssignmentRepository,
                          SurveyReportRepository surveyReportRepository,
                          PaymentRepository paymentRepository) {
        this.claimRepository = claimRepository;
        this.claimStatusHistoryRepository = claimStatusHistoryRepository;
        this.adjustorDecisionRepository = adjustorDecisionRepository;
        this.claimAssignmentRepository = claimAssignmentRepository;
        this.surveyReportRepository = surveyReportRepository;
        this.paymentRepository = paymentRepository;
    }

    public ClaimsSummaryResponse getClaimsSummary() {
        log.info("Generating claims summary report");

        Long totalClaims = claimRepository.count();
        Long submittedClaims = claimRepository.countByCurrentStatus("SUBMITTED");
        Long approvedClaims = claimRepository.countByCurrentStatus("APPROVED");
        Long rejectedClaims = claimRepository.countByCurrentStatus("REJECTED");
        Long closedClaims = claimRepository.countByCurrentStatus("CLOSED");

        BigDecimal totalApprovedAmount = adjustorDecisionRepository
                .findByDecision("APPROVED")
                .stream()
                .map(decision -> decision.getApprovedAmount() != null ? decision.getApprovedAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Double avgProcessingTime = calculateAverageProcessingTime();

        return new ClaimsSummaryResponse(
                totalClaims,
                submittedClaims,
                approvedClaims,
                rejectedClaims,
                closedClaims,
                totalApprovedAmount,
                avgProcessingTime
        );
    }

    public ClaimsAgeingResponse getClaimsAgeing() {
        log.info("Generating claims ageing report");

        Instant now = Instant.now();
        Instant sevenDaysAgo = now.minus(7, ChronoUnit.DAYS);
        Instant thirtyDaysAgo = now.minus(30, ChronoUnit.DAYS);
        Instant sixtyDaysAgo = now.minus(60, ChronoUnit.DAYS);

        Long lessThan7 = claimRepository.countBySubmittedAtBetween(thirtyDaysAgo, sevenDaysAgo);
        Long between7And30 = claimRepository.countBySubmittedAtBetween(thirtyDaysAgo, now);
        Long between30And60 = claimRepository.countBySubmittedAtBetween(sixtyDaysAgo, thirtyDaysAgo);
        Long moreThan60 = claimRepository.countBySubmittedAtBefore(sixtyDaysAgo);
        Long pending = claimRepository.countByCurrentStatusNotIn(List.of("CLOSED", "CANCELLED"));

        return new ClaimsAgeingResponse(
                lessThan7,
                between7And30,
                between30And60,
                moreThan60,
                pending
        );
    }

    public List<ClaimsByRegionResponse> getClaimsByRegion() {
        log.info("Generating claims by region report");

        // This would require region mapping in the database
        // For now, returning aggregated data
        return List.of();
    }

    public ClaimProcessingTimeResponse getClaimProcessingTime() {
        log.info("Generating claim processing time report");

        Double submissionToAssignment = calculateTimeByTransition("SUBMITTED", "CASE_ASSIGNED");
        Double assignmentToSurvey = calculateTimeByTransition("CASE_ASSIGNED", "SURVEY_SUBMITTED");
        Double surveyToAdjudication = calculateTimeByTransition("SURVEY_SUBMITTED", "ADJUSTOR_REVIEW");
        Double adjudicationToPayment = calculateTimeByTransition("APPROVED", "PAYMENT_COMPLETED");
        Double totalProcessing = calculateAverageProcessingTime();

        return new ClaimProcessingTimeResponse(
                submissionToAssignment,
                assignmentToSurvey,
                surveyToAdjudication,
                adjudicationToPayment,
                totalProcessing
        );
    }

    public List<ClaimsByRoleResponse> getClaimsByRole() {
        log.info("Generating claims by role report");

        // This would require analysis of assignments by role
        return List.of();
    }

    public FraudIndicatorsResponse getFraudIndicators() {
        log.info("Generating fraud indicators report");

        Long highValueClaims = claimRepository.countByPolicyCoverageLimitGreaterThan(BigDecimal.valueOf(50000));
        Long frequentResubmissions = claimRepository.count(); // Placeholder logic
        Long rejectedForFraud = claimRepository.countByCurrentStatus("REJECTED");
        Long manualReview = claimRepository.countByCurrentStatus("NEEDS_MORE_INFO");

        Double fraudPercentage = (double) rejectedForFraud / (double) claimRepository.count() * 100;

        return new FraudIndicatorsResponse(
                highValueClaims,
                frequentResubmissions,
                rejectedForFraud,
                manualReview,
                fraudPercentage
        );
    }

    private Double calculateAverageProcessingTime() {
        // Calculate time from SUBMITTED to CLOSED
        var claims = claimRepository.findAll();
        if (claims.isEmpty()) {
            return 0.0;
        }

        long totalDays = 0;
        for (var claim : claims) {
            if (claim.getSubmittedAt() != null && claim.getClosedAt() != null) {
                long days = ChronoUnit.DAYS.between(
                        claim.getSubmittedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDate(),
                        claim.getClosedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                );
                totalDays += days;
            }
        }

        return (double) totalDays / claims.size();
    }

    private Double calculateTimeByTransition(String fromStatus, String toStatus) {
        // Get all claims with these transitions and calculate average time
        var claims = claimRepository.findAll();
        if (claims.isEmpty()) {
            return 0.0;
        }

        long totalDays = 0;
        int count = 0;

        for (var claim : claims) {
            List<ClaimStatusHistory> history = claimStatusHistoryRepository.findByClaimIdOrderByCreatedAtDesc(claim.getId());
            ClaimStatusHistory from = null, to = null;

            for (ClaimStatusHistory h : history) {
                if (fromStatus.equals(h.getToStatus())) from = h;
                if (toStatus.equals(h.getToStatus())) to = h;
            }

            if (from != null && to != null && to.getCreatedAt().isAfter(from.getCreatedAt())) {
                long days = ChronoUnit.DAYS.between(
                        from.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDate(),
                        to.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                );
                totalDays += days;
                count++;
            }
        }

        return count > 0 ? (double) totalDays / count : 0.0;
    }
}

