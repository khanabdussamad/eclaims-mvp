package com.nagarro.eclaims.audit.entity;

import com.nagarro.eclaims.common.entity.BaseEntity;
import com.nagarro.eclaims.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_claim", columnList = "entity_id"),
        @Index(name = "idx_audit_user", columnList = "actor_user_id"),
        @Index(name = "idx_audit_action", columnList = "action"),
        @Index(name = "idx_audit_timestamp", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog extends BaseEntity {

    @Column(name = "action", nullable = false, length = 128)
    private String actionType; // CREATE, UPDATE, DELETE, APPROVE, REJECT, ASSIGN, etc.

    @Column(name = "entity_type", nullable = false, length = 64)
    private String entityType; // CLAIM, PAYMENT, ASSIGNMENT, SURVEY, etc.

    @Column(name = "entity_id")
    private UUID relatedEntityId; // UUID of the related entity

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_user_id")
    private User performedByUser;

    @Column(name = "actor_role", length = 64)
    private String performedByRole;

    @Column(name = "old_value_json", columnDefinition = "JSONB")
    private String changedValues; // JSON of before/after values

    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @Column(name = "user_agent", length = 512)
    private String userAgent;

    @Column(name = "new_value_json", columnDefinition = "JSONB")
    private String newValueJson;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "successful")
    private Boolean successful = true;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;
}

