package com.nagarro.eclaims.notification.repository;

import com.nagarro.eclaims.notification.entity.NotificationEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationEventRepository extends JpaRepository<NotificationEvent, UUID> {

    @Query("SELECT ne FROM NotificationEvent ne WHERE ne.eventType = :eventType AND ne.processed = false " +
           "ORDER BY ne.createdAt")
    List<NotificationEvent> findUnprocessedByEventType(@Param("eventType") String eventType);

    @Query("SELECT ne FROM NotificationEvent ne WHERE ne.processed = false AND " +
           "(ne.nextRetryAt IS NULL OR ne.nextRetryAt <= :now) " +
           "ORDER BY ne.createdAt LIMIT :limit")
    List<NotificationEvent> findUnprocessedForRetry(@Param("now") Instant now, @Param("limit") int limit);

    @Query("SELECT ne FROM NotificationEvent ne WHERE ne.relatedEntityType = :entityType " +
           "AND ne.relatedEntityId = :entityId ORDER BY ne.createdAt DESC")
    List<NotificationEvent> findByRelatedEntity(@Param("entityType") String entityType,
                                               @Param("entityId") String entityId);

    @Query("SELECT ne FROM NotificationEvent ne WHERE ne.eventType = :eventType " +
           "ORDER BY ne.createdAt DESC LIMIT :limit")
    List<NotificationEvent> findLatestByEventType(@Param("eventType") String eventType, @Param("limit") int limit);
}

