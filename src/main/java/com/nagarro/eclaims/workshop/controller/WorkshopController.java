package com.nagarro.eclaims.workshop.controller;

import com.nagarro.eclaims.common.response.ApiResponse;
import com.nagarro.eclaims.workshop.dto.FinalInvoiceResponse;
import com.nagarro.eclaims.workshop.dto.RepairUpdateRequest;
import com.nagarro.eclaims.workshop.dto.RepairUpdateResponse;
import com.nagarro.eclaims.workshop.dto.SelectWorkshopRequest;
import com.nagarro.eclaims.workshop.dto.SubmitFinalInvoiceRequest;
import com.nagarro.eclaims.workshop.dto.SubmitWorkOrderRequest;
import com.nagarro.eclaims.workshop.dto.WorkOrderResponse;
import com.nagarro.eclaims.workshop.dto.WorkshopResponse;
import com.nagarro.eclaims.workshop.service.WorkshopService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class WorkshopController {

    private final WorkshopService workshopService;

    @GetMapping("/workshops")
    @PreAuthorize("hasAuthority('WORKSHOP_SELECT')")
    public ResponseEntity<ApiResponse<Page<WorkshopResponse>>> getWorkshops(
            @RequestParam(required = false) String zipCode,
            Pageable pageable) {

        log.info("Fetching workshops with zipCode filter: {}", zipCode);

        Page<WorkshopResponse> workshops = workshopService.searchWorkshops(zipCode, pageable);

        return ResponseEntity.ok(ApiResponse.success("Workshops retrieved successfully", workshops));
    }

    @PostMapping("/claims/{claimId}/workshop-selection")
    @PreAuthorize("hasAuthority('WORKSHOP_SELECT')")
    public ResponseEntity<ApiResponse<WorkshopResponse>> selectWorkshop(
            @PathVariable UUID claimId,
            @Valid @RequestBody SelectWorkshopRequest request,
            Principal principal) {

        log.info("Workshop selection request for claim: {} by user: {}", claimId, principal.getName());

        UUID customerId = UUID.fromString(principal.getName());

        WorkshopResponse response = workshopService.selectWorkshop(claimId, request, customerId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Workshop selected successfully", response));
    }

    @PostMapping("/workshop/claims/{claimId}/work-orders")
    @PreAuthorize("hasAuthority('WORKSHOP_UPDATE')")
    public ResponseEntity<ApiResponse<WorkOrderResponse>> submitWorkOrder(
            @PathVariable UUID claimId,
            @Valid @RequestBody SubmitWorkOrderRequest request,
            Principal principal) {

        log.info("Work order submission for claim: {} by user: {}", claimId, principal.getName());

        UUID workshopUserId = UUID.fromString(principal.getName());

        WorkOrderResponse response = workshopService.submitWorkOrder(claimId, request, workshopUserId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Work order submitted successfully", response));
    }

    @PostMapping("/workshop/claims/{claimId}/repair-updates")
    @PreAuthorize("hasAuthority('WORKSHOP_UPDATE')")
    public ResponseEntity<ApiResponse<RepairUpdateResponse>> submitRepairUpdate(
            @PathVariable UUID claimId,
            @Valid @RequestBody RepairUpdateRequest request,
            Principal principal) {

        log.info("Repair update submission for claim: {} by user: {}", claimId, principal.getName());

        UUID workshopUserId = UUID.fromString(principal.getName());

        RepairUpdateResponse response = workshopService.submitRepairUpdate(claimId, request, workshopUserId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Repair update submitted successfully", response));
    }

    @PostMapping("/workshop/claims/{claimId}/final-invoice")
    @PreAuthorize("hasAuthority('WORKSHOP_UPDATE')")
    public ResponseEntity<ApiResponse<FinalInvoiceResponse>> submitFinalInvoice(
            @PathVariable UUID claimId,
            @Valid @RequestBody SubmitFinalInvoiceRequest request,
            Principal principal) {

        log.info("Final invoice submission for claim: {} by user: {}", claimId, principal.getName());

        UUID workshopUserId = UUID.fromString(principal.getName());

        FinalInvoiceResponse response = workshopService.submitFinalInvoice(claimId, request, workshopUserId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Final invoice submitted successfully", response));
    }

    @GetMapping("/workshop/claims/{claimId}/latest-repair-update")
    @PreAuthorize("hasAuthority('WORKSHOP_VIEW_ASSIGNED')")
    public ResponseEntity<ApiResponse<RepairUpdateResponse>> getLatestRepairUpdate(
            @PathVariable UUID claimId) {

        log.info("Fetching latest repair update for claim: {}", claimId);

        RepairUpdateResponse response = workshopService.getLatestRepairUpdate(claimId);

        return ResponseEntity.ok(ApiResponse.success("Latest repair update retrieved successfully", response));
    }
}

