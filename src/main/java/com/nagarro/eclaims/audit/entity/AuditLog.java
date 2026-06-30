package com.nagarro.eclaims.audit.entity;

import com.nagarro.eclaims.common.entity.BaseEntity;
import com.nagarro.eclaims.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_claim", columnList = "related_entity_id"),
        @Index(name = "idx_audit_user", columnList = "performed_by_user_id"),
        @Index(name = "idx_audit_action", columnList = "action_type"),
        @Index(name = "idx_audit_timestamp", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String actionType; // CREATE, UPDATE, DELETE, APPROVE, REJECT, ASSIGN, etc.

    @Column(nullable = false, length = 50)
    private String entityType; // CLAIM, PAYMENT, ASSIGNMENT, SURVEY, etc.

    @Column(nullable = false, length = 36)
    private String relatedEntityId; // UUID of the related entity

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by_user_id")
    private User performedByUser;

    @Column(length = 100)
    private String performedByRole;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String changedValues; // JSON of before/after values

    @Column(columnDefinition = "TEXT")
    private String ipAddress;

    @Column(length = 100)
    private String userAgent;

    @Column(nullable = false)
    private Boolean successful = true;

    @Column(length = 500)
    private String failureReason;
}

