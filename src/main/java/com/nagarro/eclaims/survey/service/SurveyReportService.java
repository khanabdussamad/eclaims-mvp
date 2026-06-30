package com.nagarro.eclaims.survey.service;

import com.nagarro.eclaims.assignment.entity.ClaimAssignment;
import com.nagarro.eclaims.assignment.enums.AssignmentRole;
import com.nagarro.eclaims.assignment.repository.ClaimAssignmentRepository;
import com.nagarro.eclaims.claim.entity.Claim;
import com.nagarro.eclaims.claim.repository.ClaimRepository;
import com.nagarro.eclaims.common.exception.AccessDeniedBusinessException;
import com.nagarro.eclaims.common.exception.BusinessException;
import com.nagarro.eclaims.common.exception.ResourceNotFoundException;
import com.nagarro.eclaims.survey.dto.SubmitSurveyReportRequest;
import com.nagarro.eclaims.survey.dto.SurveyReportResponse;
import com.nagarro.eclaims.survey.dto.SurveyReportSummaryResponse;
import com.nagarro.eclaims.survey.entity.SurveyReport;
import com.nagarro.eclaims.survey.repository.SurveyReportRepository;
import com.nagarro.eclaims.user.entity.User;
import com.nagarro.eclaims.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class SurveyReportService {

    private final SurveyReportRepository surveyReportRepository;
    private final ClaimRepository claimRepository;
    private final UserRepository userRepository;
    private final ClaimAssignmentRepository claimAssignmentRepository;

    public SurveyReportService(SurveyReportRepository surveyReportRepository,
                             ClaimRepository claimRepository,
                             UserRepository userRepository,
                             ClaimAssignmentRepository claimAssignmentRepository) {
        this.surveyReportRepository = surveyReportRepository;
        this.claimRepository = claimRepository;
        this.userRepository = userRepository;
        this.claimAssignmentRepository = claimAssignmentRepository;
    }

    public SurveyReportResponse submitSurveyReport(UUID claimId, SubmitSurveyReportRequest request,
                                                   UUID surveyorId) {
        log.info("Submitting survey report for claim: {} by surveyor: {}", claimId, surveyorId);

        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim", claimId.toString()));

        User surveyor = userRepository.findById(surveyorId)
                .orElseThrow(() -> new ResourceNotFoundException("User", surveyorId.toString()));

        // Validate surveyor is assigned to this claim
        ClaimAssignment assignment = claimAssignmentRepository
                .findActiveByClaimIdAndRole(claimId, AssignmentRole.SURVEYOR)
                .orElseThrow(() -> new BusinessException("NOT_ASSIGNED",
                        "No surveyor assigned to this claim"));

        if (!assignment.getAssignedUser().getId().equals(surveyorId)) {
            throw new AccessDeniedBusinessException("SURVEY_ACCESS",
                    "You are not the assigned surveyor for this claim");
        }

        // Check if survey already exists
        if (surveyReportRepository.findByClaimId(claimId).isPresent()) {
            throw new BusinessException("SURVEY_EXISTS", "Survey report already exists for this claim");
        }

        SurveyReport report = SurveyReport.builder()
                .claim(claim)
                .surveyorUser(surveyor)
                .damageSeverity(request.damageSeverity())
                .damageAssessment(request.damageAssessment())
                .repairRecommendations(request.repairRecommendations())
                .estimatedRepairCost(request.estimatedRepairCost())
                .observations(request.observations())
                .surveyStatus("SUBMITTED")
                .surveyDate(Instant.now())
                .submittedAt(Instant.now())
                .surveyNotes(request.surveyNotes())
                .build();

        report = surveyReportRepository.save(report);

        log.info("Survey report submitted successfully for claim: {}", claimId);

        return mapToResponse(report);
    }

    public SurveyReportResponse getSurveyReport(UUID claimId) {
        log.info("Getting survey report for claim: {}", claimId);

        SurveyReport report = surveyReportRepository.findByClaimId(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("SurveyReport",
                        "No survey report found for claim: " + claimId));

        return mapToResponse(report);
    }

    public Page<SurveyReportSummaryResponse> getSurveyorClaimsQueue(UUID surveyorId, Pageable pageable) {
        log.info("Fetching survey queue for surveyor: {}", surveyorId);

        Page<SurveyReport> reports = surveyReportRepository.findSubmittedBySurveyor(surveyorId, pageable);

        List<SurveyReportSummaryResponse> content = reports.getContent().stream()
                .map(report -> new SurveyReportSummaryResponse(
                        report.getId().toString(),
                        report.getClaim().getClaimNumber(),
                        report.getClaim().getCustomer().getFirstName() + " " +
                        report.getClaim().getCustomer().getLastName(),
                        report.getDamageSeverity(),
                        report.getEstimatedRepairCost(),
                        report.getSubmittedAt()
                ))
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, reports.getTotalElements());
    }

    public Page<SurveyReportResponse> getSurveyorReports(UUID surveyorId, Pageable pageable) {
        log.info("Fetching survey reports for surveyor: {}", surveyorId);

        Page<SurveyReport> reports = surveyReportRepository.findBySurveyor(surveyorId, pageable);

        List<SurveyReportResponse> content = reports.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, reports.getTotalElements());
    }

    public List<SurveyReport> getPendingSurveysForSurveyor(UUID surveyorId) {
        log.info("Fetching pending surveys for surveyor: {}", surveyorId);

        return surveyReportRepository.findPendingForSurveyor(surveyorId);
    }

    private SurveyReportResponse mapToResponse(SurveyReport report) {
        return new SurveyReportResponse(
                report.getId().toString(),
                report.getClaim().getId().toString(),
                report.getClaim().getClaimNumber(),
                report.getSurveyorUser().getFullName(),
                report.getDamageSeverity(),
                report.getDamageAssessment(),
                report.getRepairRecommendations(),
                report.getEstimatedRepairCost(),
                report.getObservations(),
                report.getSurveyStatus(),
                report.getSurveyDate(),
                report.getSubmittedAt()
        );
    }
}

