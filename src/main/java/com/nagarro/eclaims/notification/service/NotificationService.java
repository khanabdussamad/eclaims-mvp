package com.nagarro.eclaims.notification.service;

import com.nagarro.eclaims.common.exception.ResourceNotFoundException;
import com.nagarro.eclaims.notification.dto.NotificationResponse;
import com.nagarro.eclaims.notification.dto.MarkNotificationAsReadRequest;
import com.nagarro.eclaims.notification.dto.NotificationCountResponse;
import com.nagarro.eclaims.notification.entity.Notification;
import com.nagarro.eclaims.notification.entity.NotificationEvent;
import com.nagarro.eclaims.notification.repository.NotificationEventRepository;
import com.nagarro.eclaims.notification.repository.NotificationRepository;
import com.nagarro.eclaims.user.entity.User;
import com.nagarro.eclaims.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationEventRepository notificationEventRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;

    public NotificationService(NotificationRepository notificationRepository,
                             NotificationEventRepository notificationEventRepository,
                             UserRepository userRepository,
                             JavaMailSender mailSender) {
        this.notificationRepository = notificationRepository;
        this.notificationEventRepository = notificationEventRepository;
        this.userRepository = userRepository;
        this.mailSender = mailSender;
    }

    public Notification createNotification(UUID userId, String title, String message, String notificationType,
                                          String relatedEntityType, String relatedEntityId, String actionUrl) {
        log.info("Creating notification for user: {} of type: {}", userId, notificationType);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));

        Notification notification = Notification.builder()
                .recipientUser(user)
                .title(title)
                .message(message)
                .notificationType(notificationType)
                .relatedEntityType(relatedEntityType)
                .relatedEntityId(relatedEntityId)
                .actionUrl(actionUrl)
                .isRead(false)
                .isEmailSent(false)
                .build();

        notification = notificationRepository.save(notification);

        // Send email notification if configured
        if ("EMAIL".equals(notificationType) || "BOTH".equals(notificationType)) {
            sendEmailNotification(user.getEmail(), title, message);
        }

        return notification;
    }

    public NotificationEvent publishNotificationEvent(String eventType, String relatedEntityType,
                                                       String relatedEntityId, String eventData) {
        log.info("Publishing notification event: {} for entity: {}/{}", eventType, relatedEntityType, relatedEntityId);

        NotificationEvent event = NotificationEvent.builder()
                .eventType(eventType)
                .relatedEntityType(relatedEntityType)
                .relatedEntityId(relatedEntityId)
                .eventData(eventData)
                .processed(false)
                .retryCount(0)
                .build();

        return notificationEventRepository.save(event);
    }

    public Page<NotificationResponse> getNotificationsForUser(UUID userId, Pageable pageable) {
        log.info("Fetching notifications for user: {}", userId);

        Page<Notification> notifications = notificationRepository.findByRecipientUserId(userId, pageable);

        List<NotificationResponse> content = notifications.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, notifications.getTotalElements());
    }

    public NotificationResponse markAsRead(UUID notificationId, MarkNotificationAsReadRequest request) {
        log.info("Marking notification {} as read: {}", notificationId, request.isRead());

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", notificationId.toString()));

        notification.setIsRead(request.isRead());
        notification.setReadAt(request.isRead() ? Instant.now() : null);
        notification = notificationRepository.save(notification);

        return mapToResponse(notification);
    }

    public NotificationCountResponse getNotificationCount(UUID userId) {
        log.info("Getting notification count for user: {}", userId);

        Long unreadCount = notificationRepository.countUnreadByRecipientUserId(userId);
        // For total, we'd need to count all notifications for the user
        Page<Notification> allNotifications = notificationRepository.findByRecipientUserId(
                userId, Pageable.ofSize(1)
        );

        return new NotificationCountResponse(unreadCount, allNotifications.getTotalElements());
    }

    public void processNotificationEvents() {
        log.debug("Processing unprocessed notification events");

        List<NotificationEvent> unprocessedEvents = notificationEventRepository
                .findUnprocessedForRetry(Instant.now(), 100);

        for (NotificationEvent event : unprocessedEvents) {
            try {
                handleNotificationEvent(event);
                event.setProcessed(true);
                event.setProcessedAt(Instant.now());
                notificationEventRepository.save(event);
                log.info("Successfully processed notification event: {}", event.getId());
            } catch (Exception e) {
                log.error("Error processing notification event: {}", event.getId(), e);
                event.setRetryCount(event.getRetryCount() + 1);
                event.setErrorMessage(e.getMessage());
                // Schedule retry (exponential backoff)
                event.setNextRetryAt(Instant.now().plusSeconds(60L * event.getRetryCount()));
                notificationEventRepository.save(event);
            }
        }
    }

    private void handleNotificationEvent(NotificationEvent event) {
        log.debug("Handling notification event: {} of type: {}", event.getId(), event.getEventType());
        // This will be implemented with specific handlers for each event type
        // For now, just mark as successfully processed
    }

    private void sendEmailNotification(String toEmail, String subject, String message) {
        try {
            log.debug("Sending email notification to: {}", toEmail);

            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(toEmail);
            mailMessage.setSubject("eClaims Notification: " + subject);
            mailMessage.setText(message);
            mailMessage.setFrom("noreply@eclaims.com");

            mailSender.send(mailMessage);
            log.info("Email notification sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Error sending email notification to {}: {}", toEmail, e.getMessage());
        }
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId().toString(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getNotificationType(),
                notification.getRelatedEntityType(),
                notification.getRelatedEntityId(),
                notification.getIsRead(),
                notification.getActionUrl(),
                notification.getCreatedAt(),
                notification.getReadAt()
        );
    }
}

