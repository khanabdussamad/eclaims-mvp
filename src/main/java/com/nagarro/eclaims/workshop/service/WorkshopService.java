package com.nagarro.eclaims.workshop.service;

import com.nagarro.eclaims.audit.service.AuditService;
import com.nagarro.eclaims.claim.entity.Claim;
import com.nagarro.eclaims.claim.repository.ClaimRepository;
import com.nagarro.eclaims.notification.service.NotificationService;
import com.nagarro.eclaims.user.entity.Customer;
import com.nagarro.eclaims.user.entity.User;
import com.nagarro.eclaims.user.repository.CustomerRepository;
import com.nagarro.eclaims.user.repository.UserRepository;
import com.nagarro.eclaims.workshop.dto.FinalInvoiceResponse;
import com.nagarro.eclaims.workshop.dto.RepairUpdateRequest;
import com.nagarro.eclaims.workshop.dto.RepairUpdateResponse;
import com.nagarro.eclaims.workshop.dto.SelectWorkshopRequest;
import com.nagarro.eclaims.workshop.dto.SubmitFinalInvoiceRequest;
import com.nagarro.eclaims.workshop.dto.SubmitWorkOrderRequest;
import com.nagarro.eclaims.workshop.dto.WorkOrderResponse;
import com.nagarro.eclaims.workshop.dto.WorkshopResponse;
import com.nagarro.eclaims.workshop.entity.FinalInvoice;
import com.nagarro.eclaims.workshop.entity.RepairUpdate;
import com.nagarro.eclaims.workshop.entity.Workshop;
import com.nagarro.eclaims.workshop.entity.WorkOrder;
import com.nagarro.eclaims.workshop.entity.WorkshopClaim;
import com.nagarro.eclaims.workshop.repository.FinalInvoiceRepository;
import com.nagarro.eclaims.workshop.repository.RepairUpdateRepository;
import com.nagarro.eclaims.workshop.repository.WorkOrderRepository;
import com.nagarro.eclaims.workshop.repository.WorkshopClaimRepository;
import com.nagarro.eclaims.workshop.repository.WorkshopRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class WorkshopService {

    private final WorkshopRepository workshopRepository;
    private final WorkshopClaimRepository workshopClaimRepository;
    private final WorkOrderRepository workOrderRepository;
    private final RepairUpdateRepository repairUpdateRepository;
    private final FinalInvoiceRepository finalInvoiceRepository;
    private final ClaimRepository claimRepository;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final AuditService auditService;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public Page<WorkshopResponse> searchWorkshops(String zipCode, Pageable pageable) {
        log.info("Searching workshops with zipCode: {}", zipCode);

        Page<Workshop> workshops;
        if (zipCode != null && !zipCode.isEmpty()) {
            workshops = workshopRepository.findByActiveAndZipCode(true, zipCode, pageable);
        } else {
            workshops = workshopRepository.findByActive(true, pageable);
        }

        List<WorkshopResponse> responses = workshops.getContent().stream()
                .map(this::mapToWorkshopResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, workshops.getTotalElements());
    }

    @Transactional(readOnly = true)
    public Page<WorkshopResponse> getAllWorkshops(Pageable pageable) {
        log.info("Fetching all workshops");

        Page<Workshop> workshops = workshopRepository.findByActive(true, pageable);

        List<WorkshopResponse> responses = workshops.getContent().stream()
                .map(this::mapToWorkshopResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, workshops.getTotalElements());
    }

    public WorkshopResponse selectWorkshop(UUID claimId, SelectWorkshopRequest request, UUID customerId) {
        log.info("Selecting workshop for claim: {}", claimId);

        // Validate claim exists and is in correct status
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new IllegalArgumentException("Claim not found: " + claimId));

        // Validate claim is APPROVED before workshop selection
        if (!"APPROVED".equals(claim.getCurrentStatus())) {
            throw new IllegalArgumentException("Claim must be in APPROVED status for workshop selection");
        }

        // Check if workshop already selected for this claim
        if (workshopClaimRepository.findByClaimId(claimId).isPresent()) {
            throw new IllegalArgumentException("Workshop already selected for this claim");
        }

        // Validate workshop exists
        Workshop workshop = workshopRepository.findById(request.workshopId())
                .orElseThrow(() -> new IllegalArgumentException("Workshop not found: " + request.workshopId()));

        // Validate customer
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerId));

        // Create workshop claim mapping
        WorkshopClaim workshopClaim = WorkshopClaim.builder()
                .claim(claim)
                .workshop(workshop)
                .selectedByCustomer(customer)
                .status("SELECTED")
                .build();

        workshopClaim = workshopClaimRepository.save(workshopClaim);

        // Audit log
        User user = userRepository.findById(customerId).orElse(null);
        if (user != null) {
            String roleCode = user.getRoles().isEmpty() ? "USER" : user.getRoles().iterator().next().getCode();
            auditService.logAction("WORKSHOP_SELECTED", "WORKSHOP_CLAIM", workshopClaim.getId().toString(),
                    customerId, roleCode,
                    "Selected workshop: " + workshop.getName(), "", "", "");
        }

        // Send notification
        notificationService.createNotification(claimId, "Workshop Selected",
                "Workshop " + workshop.getName() + " has been selected for your claim",
                "WORKSHOP_SELECTED", "CLAIM", claimId.toString(), "/claims/" + claimId);

        log.info("Workshop selected successfully for claim: {}", claimId);

        return mapToWorkshopResponse(workshop);
    }

    public WorkOrderResponse submitWorkOrder(UUID claimId, SubmitWorkOrderRequest request, UUID workshopUserId) {
        log.info("Submitting work order for claim: {}", claimId);

        // Validate claim exists
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new IllegalArgumentException("Claim not found: " + claimId));

        // Validate workshop is selected for this claim
        WorkshopClaim workshopClaim = workshopClaimRepository.findByClaimId(claimId)
                .orElseThrow(() -> new IllegalArgumentException("No workshop selected for this claim"));

        // Create work order
        WorkOrder workOrder = WorkOrder.builder()
                .claim(claim)
                .workshop(workshopClaim.getWorkshop())
                .estimateAmount(request.estimateAmount())
                .description(request.description())
                .build();

        workOrder = workOrderRepository.save(workOrder);

        // Update workshop claim status
        workshopClaim.setStatus("IN_PROGRESS");
        workshopClaimRepository.save(workshopClaim);

        // Audit log
        User user = userRepository.findById(workshopUserId).orElse(null);
        if (user != null) {
            String roleCode = user.getRoles().isEmpty() ? "USER" : user.getRoles().iterator().next().getCode();
            auditService.logAction("WORK_ORDER_SUBMITTED", "WORK_ORDER", workOrder.getId().toString(),
                    workshopUserId, roleCode,
                    "Submitted work order for claim", "", "", "");
        }

        log.info("Work order submitted successfully: {}", workOrder.getId());

        return mapToWorkOrderResponse(workOrder);
    }

    public RepairUpdateResponse submitRepairUpdate(UUID claimId, RepairUpdateRequest request, UUID workshopUserId) {
        log.info("Submitting repair update for claim: {}", claimId);

        // Validate claim exists
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new IllegalArgumentException("Claim not found: " + claimId));

        // Validate workshop is assigned to this claim
        WorkshopClaim workshopClaim = workshopClaimRepository.findByClaimId(claimId)
                .orElseThrow(() -> new IllegalArgumentException("No workshop assigned to this claim"));

        // Create repair update
        RepairUpdate repairUpdate = RepairUpdate.builder()
                .claim(claim)
                .workshop(workshopClaim.getWorkshop())
                .repairStatus(request.repairStatus())
                .progressPercentage(request.progressPercentage())
                .expectedDeliveryDate(request.expectedDeliveryDate())
                .remarks(request.remarks())
                .build();

        repairUpdate = repairUpdateRepository.save(repairUpdate);

        // Audit log
        User user = userRepository.findById(workshopUserId).orElse(null);
        if (user != null) {
            String roleCode = user.getRoles().isEmpty() ? "USER" : user.getRoles().iterator().next().getCode();
            auditService.logAction("REPAIR_UPDATE_SUBMITTED", "REPAIR_UPDATE", repairUpdate.getId().toString(),
                    workshopUserId, roleCode,
                    "Submitted repair update: " + request.repairStatus(), "", "", "");
        }

        // Send notification to customer
        notificationService.createNotification(claimId, "Repair Status Update",
                "Your repair status has been updated to: " + request.repairStatus(),
                "REPAIR_STATUS_UPDATED", "CLAIM", claimId.toString(), "/claims/" + claimId);

        log.info("Repair update submitted successfully: {}", repairUpdate.getId());

        return mapToRepairUpdateResponse(repairUpdate);
    }

    public FinalInvoiceResponse submitFinalInvoice(UUID claimId, SubmitFinalInvoiceRequest request, UUID workshopUserId) {
        log.info("Submitting final invoice for claim: {}", claimId);

        // Validate claim exists
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new IllegalArgumentException("Claim not found: " + claimId));

        // Validate workshop is assigned
        WorkshopClaim workshopClaim = workshopClaimRepository.findByClaimId(claimId)
                .orElseThrow(() -> new IllegalArgumentException("No workshop assigned to this claim"));

        // Check if final invoice already exists
        if (finalInvoiceRepository.findByClaimId(claimId).isPresent()) {
            throw new IllegalArgumentException("Final invoice already submitted for this claim");
        }

        // Create final invoice
        FinalInvoice finalInvoice = FinalInvoice.builder()
                .claim(claim)
                .workshop(workshopClaim.getWorkshop())
                .invoiceNumber(request.invoiceNumber())
                .invoiceAmount(request.invoiceAmount())
                .build();

        finalInvoice = finalInvoiceRepository.save(finalInvoice);

        // Update claim status to PAYMENT_PENDING
        claim.setCurrentStatus("PAYMENT_PENDING");
        claimRepository.save(claim);

        // Audit log
        User user = userRepository.findById(workshopUserId).orElse(null);
        if (user != null) {
            String roleCode = user.getRoles().isEmpty() ? "USER" : user.getRoles().iterator().next().getCode();
            auditService.logAction("FINAL_INVOICE_SUBMITTED", "FINAL_INVOICE", finalInvoice.getId().toString(),
                    workshopUserId, roleCode,
                    "Submitted final invoice for claim", "", "", "");
        }

        // Send notification
        notificationService.createNotification(claimId, "Final Invoice Submitted",
                "The repair work is complete. Invoice: " + request.invoiceNumber(),
                "REPAIR_COMPLETED", "CLAIM", claimId.toString(), "/claims/" + claimId);

        log.info("Final invoice submitted successfully: {}", finalInvoice.getId());

        return mapToFinalInvoiceResponse(finalInvoice);
    }

    @Transactional(readOnly = true)
    public RepairUpdateResponse getLatestRepairUpdate(UUID claimId) {
        log.info("Fetching latest repair update for claim: {}", claimId);

        RepairUpdate repairUpdate = repairUpdateRepository.findFirstByClaimIdOrderByCreatedAtDesc(claimId)
                .orElseThrow(() -> new IllegalArgumentException("No repair updates found for claim: " + claimId));

        return mapToRepairUpdateResponse(repairUpdate);
    }

    private WorkshopResponse mapToWorkshopResponse(Workshop workshop) {
        return new WorkshopResponse(
                workshop.getId().toString(),
                workshop.getName(),
                workshop.getPartnerCode(),
                workshop.getCity(),
                workshop.getState(),
                workshop.getZipCode(),
                workshop.getPhone(),
                workshop.getEmail()
        );
    }

    private WorkOrderResponse mapToWorkOrderResponse(WorkOrder workOrder) {
        return new WorkOrderResponse(
                workOrder.getId().toString(),
                workOrder.getClaim().getId().toString(),
                workOrder.getWorkshop().getName(),
                workOrder.getEstimateAmount(),
                workOrder.getCurrency(),
                workOrder.getDescription(),
                workOrder.getSubmittedAt()
        );
    }

    private RepairUpdateResponse mapToRepairUpdateResponse(RepairUpdate repairUpdate) {
        return new RepairUpdateResponse(
                repairUpdate.getId().toString(),
                repairUpdate.getClaim().getId().toString(),
                repairUpdate.getRepairStatus(),
                repairUpdate.getProgressPercentage(),
                repairUpdate.getExpectedDeliveryDate(),
                repairUpdate.getRemarks(),
                repairUpdate.getCreatedAt()
        );
    }

    private FinalInvoiceResponse mapToFinalInvoiceResponse(FinalInvoice finalInvoice) {
        return new FinalInvoiceResponse(
                finalInvoice.getId().toString(),
                finalInvoice.getClaim().getId().toString(),
                finalInvoice.getWorkshop().getName(),
                finalInvoice.getInvoiceNumber(),
                finalInvoice.getInvoiceAmount(),
                finalInvoice.getCurrency(),
                finalInvoice.getSubmittedAt()
        );
    }
}

