package com.nagarro.eclaims.audit.service;

import com.nagarro.eclaims.audit.dto.AuditLogResponse;
import com.nagarro.eclaims.audit.dto.AuditTrailResponse;
import com.nagarro.eclaims.audit.entity.AuditLog;
import com.nagarro.eclaims.audit.repository.AuditLogRepository;
import com.nagarro.eclaims.common.exception.ResourceNotFoundException;
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
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public AuditService(AuditLogRepository auditLogRepository,
                       UserRepository userRepository) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    public AuditLog logAction(String actionType, String entityType, String entityId,
                             UUID performedByUserId, String performedByRole,
                             String description, String changedValues,
                             String ipAddress, String userAgent) {
        log.debug("Logging audit action: {} on {} {}", actionType, entityType, entityId);

        User performedByUser = null;
        if (performedByUserId != null) {
            performedByUser = userRepository.findById(performedByUserId).orElse(null);
        }

        AuditLog auditLog = AuditLog.builder()
                .actionType(actionType)
                .entityType(entityType)
                .relatedEntityId(entityId)
                .performedByUser(performedByUser)
                .performedByRole(performedByRole)
                .description(description)
                .changedValues(changedValues)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .successful(true)
                .build();

        return auditLogRepository.save(auditLog);
    }

    public AuditLog logFailedAction(String actionType, String entityType, String entityId,
                                   UUID performedByUserId, String performedByRole,
                                   String description, String failureReason,
                                   String ipAddress, String userAgent) {
        log.warn("Logging failed audit action: {} on {} {}", actionType, entityType, entityId);

        User performedByUser = null;
        if (performedByUserId != null) {
            performedByUser = userRepository.findById(performedByUserId).orElse(null);
        }

        AuditLog auditLog = AuditLog.builder()
                .actionType(actionType)
                .entityType(entityType)
                .relatedEntityId(entityId)
                .performedByUser(performedByUser)
                .performedByRole(performedByRole)
                .description(description)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .successful(false)
                .failureReason(failureReason)
                .build();

        return auditLogRepository.save(auditLog);
    }

    public Page<AuditLogResponse> getClaimAuditTrail(String claimId, Pageable pageable) {
        log.info("Fetching audit trail for claim: {}", claimId);

        Page<AuditLog> auditLogs = auditLogRepository.findByRelatedEntityId(claimId, pageable);

        List<AuditLogResponse> content = auditLogs.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, auditLogs.getTotalElements());
    }

    public Page<AuditLogResponse> getUserAuditTrail(UUID userId, Pageable pageable) {
        log.info("Fetching audit trail for user: {}", userId);

        Page<AuditLog> auditLogs = auditLogRepository.findByPerformedByUser(userId, pageable);

        List<AuditLogResponse> content = auditLogs.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, auditLogs.getTotalElements());
    }

    public AuditTrailResponse getDetailedClaimAuditTrail(String claimId, Pageable pageable) {
        log.info("Fetching detailed audit trail for claim: {}", claimId);

        Page<AuditLog> auditLogs = auditLogRepository.findByRelatedEntityId(claimId, pageable);

        List<AuditLogResponse> actions = auditLogs.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return new AuditTrailResponse(
                claimId,
                claimId,
                auditLogs.getTotalElements(),
                actions
        );
    }

    public List<AuditLog> getFailedOperations() {
        log.info("Fetching failed audit operations");
        return auditLogRepository.findFailedOperations();
    }

    public List<AuditLogResponse> getActionsByType(String actionType) {
        log.info("Fetching audit logs for action type: {}", actionType);

        return auditLogRepository.findByActionType(actionType).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<AuditLogResponse> getAuditLogsForDateRange(Instant startDate, Instant endDate) {
        log.info("Fetching audit logs for date range: {} to {}", startDate, endDate);

        return auditLogRepository.findByDateRange(startDate, endDate).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private AuditLogResponse mapToResponse(AuditLog auditLog) {
        return new AuditLogResponse(
                auditLog.getId().toString(),
                auditLog.getActionType(),
                auditLog.getEntityType(),
                auditLog.getRelatedEntityId(),
                auditLog.getPerformedByUser() != null ? auditLog.getPerformedByUser().getFullName() : "SYSTEM",
                auditLog.getPerformedByRole(),
                auditLog.getDescription(),
                auditLog.getSuccessful(),
                auditLog.getCreatedAt()
        );
    }
}

