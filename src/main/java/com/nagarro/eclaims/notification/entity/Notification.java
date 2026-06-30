package com.nagarro.eclaims.notification.entity;

import com.nagarro.eclaims.common.entity.BaseEntity;
import com.nagarro.eclaims.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notification_user", columnList = "recipient_user_id"),
        @Index(name = "idx_notification_read", columnList = "is_read"),
        @Index(name = "idx_notification_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_user_id", nullable = false)
    private User recipientUser;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(length = 50)
    private String notificationType; // EMAIL, IN_APP, SMS

    @Column(length = 100)
    private String relatedEntityType; // CLAIM, PAYMENT, ASSIGNMENT, etc.

    @Column(length = 36)
    private String relatedEntityId; // UUID of the related entity

    @Column(nullable = false)
    private Boolean isRead = false;

    @Column(nullable = false)
    private Boolean isEmailSent = false;

    @Column(length = 500)
    private String actionUrl;

    @Column
    private Instant readAt;

    @Column
    private Instant sentAt;
}

