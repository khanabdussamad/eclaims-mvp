package com.nagarro.eclaims.notification.entity;

import com.nagarro.eclaims.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "notification_events", indexes = {
        @Index(name = "idx_notif_event_type", columnList = "event_type"),
        @Index(name = "idx_notif_event_entity", columnList = "related_entity_type,related_entity_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationEvent extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String eventType; // CLAIM_SUBMITTED, CLAIM_ASSIGNED, CLAIM_APPROVED, etc.

    @Column(length = 100)
    private String relatedEntityType;

    @Column(length = 36)
    private String relatedEntityId;

    @Column(columnDefinition = "TEXT")
    private String eventData; // JSON payload

    @Column(nullable = false)
    private Boolean processed = false;

    @Column
    private Instant processedAt;

    @Column(length = 500)
    private String errorMessage;

    @Column(nullable = false)
    private Integer retryCount = 0;

    @Column
    private Instant nextRetryAt;
}

