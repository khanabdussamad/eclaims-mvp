package com.nagarro.eclaims.assignment.repository;

import com.nagarro.eclaims.assignment.entity.ClaimAssignment;
import com.nagarro.eclaims.assignment.enums.AssignmentRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClaimAssignmentRepository extends JpaRepository<ClaimAssignment, UUID> {

    @Query("SELECT ca FROM ClaimAssignment ca WHERE ca.claim.id = :claimId AND ca.isActive = true")
    List<ClaimAssignment> findActiveByClaimId(@Param("claimId") UUID claimId);

    @Query("SELECT ca FROM ClaimAssignment ca WHERE ca.claim.id = :claimId " +
           "AND ca.assignmentRole = :role AND ca.isActive = true")
    Optional<ClaimAssignment> findActiveByClaimIdAndRole(@Param("claimId") UUID claimId,
                                                          @Param("role") AssignmentRole role);

    @Query("SELECT ca FROM ClaimAssignment ca WHERE ca.assignedUser.id = :userId " +
           "AND ca.assignmentRole = :role AND ca.isActive = true " +
           "AND ca.completedAt IS NULL ORDER BY ca.assignedAt DESC")
    Page<ClaimAssignment> findActiveAssignmentsByUserAndRole(@Param("userId") UUID userId,
                                                               @Param("role") AssignmentRole role,
                                                               Pageable pageable);

    @Query("SELECT ca FROM ClaimAssignment ca WHERE ca.assignedUser.id = :userId AND ca.isActive = true")
    List<ClaimAssignment> findActiveAssignmentsByUser(@Param("userId") UUID userId);

    @Query("SELECT COUNT(ca) FROM ClaimAssignment ca WHERE ca.assignedUser.id = :userId " +
           "AND ca.isActive = true AND ca.completedAt IS NULL")
    Long countActiveAssignmentsByUser(@Param("userId") UUID userId);

    @Query("SELECT ca FROM ClaimAssignment ca WHERE ca.assignmentRole = :role AND ca.isActive = true " +
           "AND ca.completedAt IS NULL ORDER BY ca.createdAt")
    List<ClaimAssignment> findUncompletedByRole(@Param("role") AssignmentRole role);
}


