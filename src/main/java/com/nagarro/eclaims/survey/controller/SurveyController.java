package com.nagarro.eclaims.survey.controller;

import com.nagarro.eclaims.common.response.ApiResponse;
import com.nagarro.eclaims.common.response.PageResponse;
import com.nagarro.eclaims.survey.dto.SubmitSurveyReportRequest;
import com.nagarro.eclaims.survey.dto.SurveyReportResponse;
import com.nagarro.eclaims.survey.dto.SurveyReportSummaryResponse;
import com.nagarro.eclaims.survey.service.SurveyReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Survey", description = "Survey and damage assessment management")
@SecurityRequirement(name = "bearer-jwt")
public class SurveyController {

    private final SurveyReportService surveyReportService;

    public SurveyController(SurveyReportService surveyReportService) {
        this.surveyReportService = surveyReportService;
    }

    @GetMapping("/surveyor/claims")
    @PreAuthorize("hasAuthority('SURVEY_VIEW_ASSIGNED')")
    @Operation(summary = "Get surveyor's claim queue",
               description = "Retrieve claims assigned to the authenticated surveyor with submitted surveys")
    public ResponseEntity<ApiResponse<PageResponse<SurveyReportSummaryResponse>>> getSurveyorClaimsQueue(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Principal principal) {
        log.info("Fetching survey queue for surveyor: {}", principal.getName());

        UUID surveyorId = UUID.fromString(principal.getName());
        Pageable pageable = PageRequest.of(page, size);

        Page<SurveyReportSummaryResponse> claims = surveyReportService.getSurveyorClaimsQueue(surveyorId, pageable);

        PageResponse<SurveyReportSummaryResponse> pageResponse = new PageResponse<>(
                claims.getContent(),
                claims.getNumber(),
                claims.getSize(),
                claims.getTotalElements(),
                claims.getTotalPages(),
                claims.isLast()
        );

        return ResponseEntity.ok(
                ApiResponse.success("Surveyor claims retrieved successfully", pageResponse)
        );
    }

    @PostMapping("/claims/{claimId}/survey-reports")
    @PreAuthorize("hasAuthority('SURVEY_SUBMIT')")
    @Operation(summary = "Submit survey report",
               description = "Submit a damage assessment survey report for a claim")
    public ResponseEntity<ApiResponse<SurveyReportResponse>> submitSurveyReport(
            @PathVariable UUID claimId,
            @RequestBody SubmitSurveyReportRequest request,
            Principal principal) {
        log.info("Submitting survey report for claim: {}", claimId);

        UUID surveyorId = UUID.fromString(principal.getName());
        SurveyReportResponse response = surveyReportService.submitSurveyReport(claimId, request, surveyorId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Survey report submitted successfully", response));
    }

    @GetMapping("/claims/{claimId}/survey-reports")
    @PreAuthorize("hasAuthority('DOCUMENT_VIEW')")
    @Operation(summary = "Get survey report for claim",
               description = "Retrieve the survey report for a specific claim")
    public ResponseEntity<ApiResponse<SurveyReportResponse>> getSurveyReport(
            @PathVariable UUID claimId) {
        log.info("Fetching survey report for claim: {}", claimId);

        SurveyReportResponse report = surveyReportService.getSurveyReport(claimId);

        return ResponseEntity.ok(
                ApiResponse.success("Survey report retrieved successfully", report)
        );
    }
}

