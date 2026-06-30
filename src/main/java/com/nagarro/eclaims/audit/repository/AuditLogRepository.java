package com.nagarro.eclaims.audit.repository;

import com.nagarro.eclaims.audit.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    @Query("SELECT al FROM AuditLog al WHERE al.relatedEntityId = :entityId " +
           "ORDER BY al.createdAt DESC")
    Page<AuditLog> findByRelatedEntityId(@Param("entityId") String entityId, Pageable pageable);

    @Query("SELECT al FROM AuditLog al WHERE al.performedByUser.id = :userId " +
           "ORDER BY al.createdAt DESC")
    Page<AuditLog> findByPerformedByUser(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT al FROM AuditLog al WHERE al.actionType = :actionType " +
           "ORDER BY al.createdAt DESC")
    List<AuditLog> findByActionType(@Param("actionType") String actionType);

    @Query("SELECT al FROM AuditLog al WHERE al.entityType = :entityType " +
           "AND al.relatedEntityId = :entityId ORDER BY al.createdAt DESC")
    List<AuditLog> findByEntityAndId(@Param("entityType") String entityType,
                                     @Param("entityId") String entityId);

    @Query("SELECT al FROM AuditLog al WHERE al.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY al.createdAt DESC")
    List<AuditLog> findByDateRange(@Param("startDate") Instant startDate,
                                    @Param("endDate") Instant endDate);

    @Query("SELECT al FROM AuditLog al WHERE al.successful = false " +
           "ORDER BY al.createdAt DESC")
    List<AuditLog> findFailedOperations();
}

