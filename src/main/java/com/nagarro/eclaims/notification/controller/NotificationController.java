package com.nagarro.eclaims.notification.controller;

import com.nagarro.eclaims.common.response.ApiResponse;
import com.nagarro.eclaims.common.response.PageResponse;
import com.nagarro.eclaims.notification.dto.NotificationResponse;
import com.nagarro.eclaims.notification.dto.MarkNotificationAsReadRequest;
import com.nagarro.eclaims.notification.dto.NotificationCountResponse;
import com.nagarro.eclaims.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notifications", description = "Notification management")
@SecurityRequirement(name = "bearer-jwt")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('NOTIFICATION_VIEW')")
    @Operation(summary = "Get user notifications",
               description = "Retrieve paginated notifications for the authenticated user")
    public ResponseEntity<ApiResponse<PageResponse<NotificationResponse>>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction sortDirection,
            Principal principal) {
        log.info("Fetching notifications for user: {}", principal.getName());

        UUID userId = UUID.fromString(principal.getName());
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Page<NotificationResponse> notifications = notificationService.getNotificationsForUser(userId, pageable);

        PageResponse<NotificationResponse> pageResponse = new PageResponse<>(
                notifications.getContent(),
                notifications.getNumber(),
                notifications.getSize(),
                notifications.getTotalElements(),
                notifications.getTotalPages(),
                notifications.isLast()
        );

        return ResponseEntity.ok(
                ApiResponse.success("Notifications retrieved successfully", pageResponse)
        );
    }

    @PatchMapping("/{notificationId}/read")
    @PreAuthorize("hasAuthority('NOTIFICATION_VIEW')")
    @Operation(summary = "Mark notification as read",
               description = "Update the read status of a notification")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(
            @PathVariable UUID notificationId,
            @RequestBody MarkNotificationAsReadRequest request) {
        log.info("Marking notification {} as read", notificationId);

        NotificationResponse response = notificationService.markAsRead(notificationId, request);

        return ResponseEntity.ok(
                ApiResponse.success("Notification updated successfully", response)
        );
    }

    @GetMapping("/count")
    @PreAuthorize("hasAuthority('NOTIFICATION_VIEW')")
    @Operation(summary = "Get notification count",
               description = "Get unread and total notification count for the authenticated user")
    public ResponseEntity<ApiResponse<NotificationCountResponse>> getNotificationCount(Principal principal) {
        log.info("Getting notification count for user: {}", principal.getName());

        UUID userId = UUID.fromString(principal.getName());
        NotificationCountResponse count = notificationService.getNotificationCount(userId);

        return ResponseEntity.ok(
                ApiResponse.success("Notification count retrieved successfully", count)
        );
    }
}

