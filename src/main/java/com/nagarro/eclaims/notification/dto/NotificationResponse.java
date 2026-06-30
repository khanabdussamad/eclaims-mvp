package com.nagarro.eclaims.notification.dto;

import java.time.Instant;

public record NotificationResponse(
        String id,
        String title,
        String message,
        String notificationType,
        String relatedEntityType,
        String relatedEntityId,
        Boolean isRead,
        String actionUrl,
        Instant createdAt,
        Instant readAt
) {}

