package com.nagarro.eclaims.workflow.service;

import com.nagarro.eclaims.claim.entity.Claim;
import com.nagarro.eclaims.common.exception.BusinessException;
import com.nagarro.eclaims.workflow.entity.WorkflowTransition;
import com.nagarro.eclaims.workflow.enums.WorkflowPrecondition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
@Service
public class WorkflowValidationService {

    public void validatePreconditions(Claim claim, WorkflowTransition transition) {
        if (transition.getPreconditions() == null || transition.getPreconditions().isEmpty()) {
            return; // No preconditions to validate
        }

        log.debug("Validating preconditions for transition from {} to {}",
                transition.getFromStatus(), transition.getToStatus());

        for (String preconditionStr : transition.getPreconditions()) {
            try {
                WorkflowPrecondition precondition = WorkflowPrecondition.valueOf(preconditionStr);
                validateSinglePrecondition(claim, precondition);
            } catch (IllegalArgumentException e) {
                log.warn("Unknown precondition: {}", preconditionStr);
            }
        }
    }

    private void validateSinglePrecondition(Claim claim, WorkflowPrecondition precondition) {
        switch (precondition) {
            case SURVEYOR_ASSIGNED:
                // This will be validated when survey module is implemented
                log.debug("SURVEYOR_ASSIGNED precondition check (will be enhanced)");
                break;

            case SURVEY_COMPLETED:
                // This will be validated when survey module is implemented
                log.debug("SURVEY_COMPLETED precondition check (will be enhanced)");
                break;

            case ADJUSTOR_ASSIGNED:
                // This will be validated when adjudication module is implemented
                log.debug("ADJUSTOR_ASSIGNED precondition check (will be enhanced)");
                break;

            case REPAIR_WORKSHOP_ASSIGNED:
                // This will be validated when workshop module is implemented
                log.debug("REPAIR_WORKSHOP_ASSIGNED precondition check (will be enhanced)");
                break;

            case PAYMENT_COMPLETED:
                // This will be validated when payment module is implemented
                log.debug("PAYMENT_COMPLETED precondition check (will be enhanced)");
                break;

            case DOCUMENTS_UPLOADED:
                // This will be validated when document module is implemented
                log.debug("DOCUMENTS_UPLOADED precondition check (will be enhanced)");
                break;

            case APPROVAL_REQUIRED:
                // Approve amounts must be set on decision
                log.debug("APPROVAL_REQUIRED precondition check (will be enhanced)");
                break;

            case FINAL_INVOICE_RECEIVED:
                // This will be validated when workshop module is implemented
                log.debug("FINAL_INVOICE_RECEIVED precondition check (will be enhanced)");
                break;

            default:
                log.warn("Unknown precondition: {}", precondition);
        }
    }

    public void validateTransitionAllowed(Claim claim, WorkflowTransition transition) {
        // Additional business logic validations
        String currentStatus = claim.getCurrentStatus();
        String newStatus = transition.getToStatus();

        // Prevent multiple simultaneous assignments
        // This is handled by assignment module

        log.debug("Transition validation passed for {} -> {}", currentStatus, newStatus);
    }
}

