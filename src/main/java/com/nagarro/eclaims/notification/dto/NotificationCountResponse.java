package com.nagarro.eclaims.notification.dto;

public record NotificationCountResponse(
        Long unreadCount,
        Long totalCount
) {}

