package com.nagarro.eclaims.adjudication.service;

import com.nagarro.eclaims.adjudication.dto.SubmitAdjudicationRequest;
import com.nagarro.eclaims.adjudication.dto.AdjudicationResponse;
import com.nagarro.eclaims.adjudication.dto.AdjustorClaimResponse;
import com.nagarro.eclaims.adjudication.entity.AdjustorDecision;
import com.nagarro.eclaims.adjudication.repository.AdjustorDecisionRepository;
import com.nagarro.eclaims.assignment.entity.ClaimAssignment;
import com.nagarro.eclaims.assignment.enums.AssignmentRole;
import com.nagarro.eclaims.assignment.repository.ClaimAssignmentRepository;
import com.nagarro.eclaims.claim.entity.Claim;
import com.nagarro.eclaims.claim.repository.ClaimRepository;
import com.nagarro.eclaims.common.exception.AccessDeniedBusinessException;
import com.nagarro.eclaims.common.exception.BusinessException;
import com.nagarro.eclaims.common.exception.ResourceNotFoundException;
import com.nagarro.eclaims.survey.repository.SurveyReportRepository;
import com.nagarro.eclaims.user.entity.User;
import com.nagarro.eclaims.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class AdjudicationService {

    private final AdjustorDecisionRepository adjustorDecisionRepository;
    private final ClaimRepository claimRepository;
    private final UserRepository userRepository;
    private final ClaimAssignmentRepository claimAssignmentRepository;
    private final SurveyReportRepository surveyReportRepository;

    public AdjudicationService(AdjustorDecisionRepository adjustorDecisionRepository,
                             ClaimRepository claimRepository,
                             UserRepository userRepository,
                             ClaimAssignmentRepository claimAssignmentRepository,
                             SurveyReportRepository surveyReportRepository) {
        this.adjustorDecisionRepository = adjustorDecisionRepository;
        this.claimRepository = claimRepository;
        this.userRepository = userRepository;
        this.claimAssignmentRepository = claimAssignmentRepository;
        this.surveyReportRepository = surveyReportRepository;
    }

    public AdjudicationResponse submitAdjudication(UUID claimId, SubmitAdjudicationRequest request,
                                                   UUID adjustorId) {
        log.info("Submitting adjudication for claim: {} by adjustor: {}", claimId, adjustorId);

        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim", claimId.toString()));

        User adjustor = userRepository.findById(adjustorId)
                .orElseThrow(() -> new ResourceNotFoundException("User", adjustorId.toString()));

        // Validate adjustor is assigned to this claim
        ClaimAssignment assignment = claimAssignmentRepository
                .findActiveByClaimIdAndRole(claimId, AssignmentRole.ADJUSTOR)
                .orElseThrow(() -> new BusinessException("NOT_ASSIGNED",
                        "No adjustor assigned to this claim"));

        if (!assignment.getAssignedUser().getId().equals(adjustorId)) {
            throw new AccessDeniedBusinessException("ADJUDICATION_ACCESS",
                    "You are not the assigned adjustor for this claim");
        }

        // Validate decision
        validateDecision(request);

        // Get policy coverage limits
        BigDecimal coverageLimit = claim.getPolicy().getCoverageLimit();
        BigDecimal deductibleAmount = claim.getPolicy().getDeductibleAmount();

        // Validate approved amount if approved
        if ("APPROVED".equals(request.decision())) {
            validateApprovedAmount(request.approvedAmount(), coverageLimit, deductibleAmount);
        }

        AdjustorDecision decision = AdjustorDecision.builder()
                .claim(claim)
                .adjustorUser(adjustor)
                .decision(request.decision())
                .approvedAmount("APPROVED".equals(request.decision()) ? request.approvedAmount() : null)
                .rationale(request.rationale())
                .remarks(request.remarks())
                .denialReason(request.denialReason())
                .decisionDate(Instant.now())
                .submittedAt(Instant.now())
                .coverageLimit(coverageLimit)
                .deductibleAmount(deductibleAmount)
                .build();

        decision = adjustorDecisionRepository.save(decision);

        log.info("Adjudication submitted successfully for claim: {}", claimId);

        return mapToResponse(decision);
    }

    public AdjudicationResponse getAdjudication(UUID claimId) {
        log.info("Getting adjudication for claim: {}", claimId);

        AdjustorDecision decision = adjustorDecisionRepository.findByClaimId(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("AdjustorDecision",
                        "No adjudication found for claim: " + claimId));

        return mapToResponse(decision);
    }

    public Page<AdjustorClaimResponse> getAdjustorClaimsQueue(UUID adjustorId, Pageable pageable) {
        log.info("Fetching adjudication queue for adjustor: {}", adjustorId);

        Page<AdjustorDecision> decisions = adjustorDecisionRepository.findByAdjustor(adjustorId, pageable);

        List<AdjustorClaimResponse> content = decisions.getContent().stream()
                .map(decision -> new AdjustorClaimResponse(
                        decision.getClaim().getId().toString(),
                        decision.getClaim().getClaimNumber(),
                        decision.getClaim().getCustomer().getFirstName() + " " +
                        decision.getClaim().getCustomer().getLastName(),
                        decision.getClaim().getIncidentZipCode(),
                        decision.getClaim().getPolicy().getCoverageLimit(),
                        "PENDING"
                ))
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, decisions.getTotalElements());
    }

    public Page<AdjudicationResponse> getAdjustorDecisions(UUID adjustorId, Pageable pageable) {
        log.info("Fetching decisions for adjustor: {}", adjustorId);

        Page<AdjustorDecision> decisions = adjustorDecisionRepository.findByAdjustor(adjustorId, pageable);

        List<AdjudicationResponse> content = decisions.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, decisions.getTotalElements());
    }

    private void validateDecision(SubmitAdjudicationRequest request) {
        if (!List.of("APPROVED", "REJECTED", "NEEDS_MORE_INFO").contains(request.decision())) {
            throw new BusinessException("INVALID_DECISION", "Invalid decision type: " + request.decision());
        }
    }

    private void validateApprovedAmount(BigDecimal approvedAmount, BigDecimal coverageLimit,
                                        BigDecimal deductibleAmount) {
        if (approvedAmount == null || approvedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("INVALID_AMOUNT", "Approved amount must be greater than zero");
        }

        BigDecimal maxApprovedAmount = coverageLimit.subtract(deductibleAmount);
        if (approvedAmount.compareTo(maxApprovedAmount) > 0) {
            throw new BusinessException("AMOUNT_EXCEEDS_LIMIT",
                    "Approved amount exceeds coverage limit after deductible");
        }
    }

    private AdjudicationResponse mapToResponse(AdjustorDecision decision) {
        return new AdjudicationResponse(
                decision.getId().toString(),
                decision.getClaim().getId().toString(),
                decision.getClaim().getClaimNumber(),
                decision.getAdjustorUser().getFullName(),
                decision.getDecision(),
                decision.getApprovedAmount(),
                decision.getRationale(),
                decision.getDenialReason(),
                decision.getDecisionDate(),
                decision.getSubmittedAt()
        );
    }
}

