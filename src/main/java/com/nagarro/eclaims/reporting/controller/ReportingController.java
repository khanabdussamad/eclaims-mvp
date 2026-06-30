package com.nagarro.eclaims.reporting.controller;

import com.nagarro.eclaims.common.response.ApiResponse;
import com.nagarro.eclaims.reporting.dto.ClaimsSummaryResponse;
import com.nagarro.eclaims.reporting.dto.ClaimsAgeingResponse;
import com.nagarro.eclaims.reporting.dto.ClaimsByRegionResponse;
import com.nagarro.eclaims.reporting.dto.ClaimProcessingTimeResponse;
import com.nagarro.eclaims.reporting.dto.ClaimsByRoleResponse;
import com.nagarro.eclaims.reporting.dto.FraudIndicatorsResponse;
import com.nagarro.eclaims.reporting.service.ReportingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/reports")
@Tag(name = "Reporting", description = "Claims analytics and reporting")
@SecurityRequirement(name = "bearer-jwt")
public class ReportingController {

    private final ReportingService reportingService;

    public ReportingController(ReportingService reportingService) {
        this.reportingService = reportingService;
    }

    @GetMapping("/claims-summary")
    @PreAuthorize("hasAuthority('REPORT_VIEW')")
    @Operation(summary = "Get claims summary",
               description = "Retrieve overall claims processing summary and statistics")
    public ResponseEntity<ApiResponse<ClaimsSummaryResponse>> getClaimsSummary() {
        log.info("Fetching claims summary report");

        ClaimsSummaryResponse report = reportingService.getClaimsSummary();

        return ResponseEntity.ok(
                ApiResponse.success("Claims summary retrieved successfully", report)
        );
    }

    @GetMapping("/claims-ageing")
    @PreAuthorize("hasAuthority('REPORT_VIEW')")
    @Operation(summary = "Get claims ageing report",
               description = "Retrieve claims aged by processing time")
    public ResponseEntity<ApiResponse<ClaimsAgeingResponse>> getClaimsAgeing() {
        log.info("Fetching claims ageing report");

        ClaimsAgeingResponse report = reportingService.getClaimsAgeing();

        return ResponseEntity.ok(
                ApiResponse.success("Claims ageing retrieved successfully", report)
        );
    }

    @GetMapping("/claims-by-region")
    @PreAuthorize("hasAuthority('REPORT_VIEW')")
    @Operation(summary = "Get claims by region",
               description = "Retrieve claims statistics broken down by region")
    public ResponseEntity<ApiResponse<List<ClaimsByRegionResponse>>> getClaimsByRegion() {
        log.info("Fetching claims by region report");

        List<ClaimsByRegionResponse> report = reportingService.getClaimsByRegion();

        return ResponseEntity.ok(
                ApiResponse.success("Claims by region retrieved successfully", report)
        );
    }

    @GetMapping("/claims-processing-time")
    @PreAuthorize("hasAuthority('REPORT_VIEW')")
    @Operation(summary = "Get processing time metrics",
               description = "Retrieve claim processing time metrics by stage")
    public ResponseEntity<ApiResponse<ClaimProcessingTimeResponse>> getClaimProcessingTime() {
        log.info("Fetching processing time report");

        ClaimProcessingTimeResponse report = reportingService.getClaimProcessingTime();

        return ResponseEntity.ok(
                ApiResponse.success("Processing time metrics retrieved successfully", report)
        );
    }

    @GetMapping("/claims-by-role")
    @PreAuthorize("hasAuthority('REPORT_VIEW')")
    @Operation(summary = "Get claims by role",
               description = "Retrieve claims statistics broken down by assigned role")
    public ResponseEntity<ApiResponse<List<ClaimsByRoleResponse>>> getClaimsByRole() {
        log.info("Fetching claims by role report");

        List<ClaimsByRoleResponse> report = reportingService.getClaimsByRole();

        return ResponseEntity.ok(
                ApiResponse.success("Claims by role retrieved successfully", report)
        );
    }

    @GetMapping("/fraud-indicators")
    @PreAuthorize("hasAuthority('REPORT_VIEW')")
    @Operation(summary = "Get fraud indicators",
               description = "Retrieve fraud detection indicators and risk metrics")
    public ResponseEntity<ApiResponse<FraudIndicatorsResponse>> getFraudIndicators() {
        log.info("Fetching fraud indicators report");

        FraudIndicatorsResponse report = reportingService.getFraudIndicators();

        return ResponseEntity.ok(
                ApiResponse.success("Fraud indicators retrieved successfully", report)
        );
    }
}

