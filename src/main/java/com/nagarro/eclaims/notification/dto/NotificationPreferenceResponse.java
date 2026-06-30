package com.nagarro.eclaims.notification.dto;

public record NotificationPreferenceResponse(
        String userId,
        Boolean emailNotificationsEnabled,
        Boolean inAppNotificationsEnabled,
        String notificationFrequency
) {}

