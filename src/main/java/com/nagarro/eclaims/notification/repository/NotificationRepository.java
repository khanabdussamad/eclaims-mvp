package com.nagarro.eclaims.notification.repository;

import com.nagarro.eclaims.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    @Query("SELECT n FROM Notification n WHERE n.recipientUser.id = :userId ORDER BY n.createdAt DESC")
    Page<Notification> findByRecipientUserId(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT n FROM Notification n WHERE n.recipientUser.id = :userId AND n.isRead = false " +
           "ORDER BY n.createdAt DESC")
    List<Notification> findUnreadByRecipientUserId(@Param("userId") UUID userId);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipientUser.id = :userId AND n.isRead = false")
    Long countUnreadByRecipientUserId(@Param("userId") UUID userId);

    @Query("SELECT n FROM Notification n WHERE n.relatedEntityType = :entityType " +
           "AND n.relatedEntityId = :entityId ORDER BY n.createdAt DESC")
    List<Notification> findByRelatedEntity(@Param("entityType") String entityType,
                                          @Param("entityId") String entityId);
}

