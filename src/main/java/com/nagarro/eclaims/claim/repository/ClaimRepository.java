package com.nagarro.eclaims.claim.repository;

import com.nagarro.eclaims.claim.entity.Claim;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, UUID> {
    Optional<Claim> findByClaimNumber(String claimNumber);
    List<Claim> findByCustomerId(UUID customerId);
    List<Claim> findByPolicyId(UUID policyId);
    Page<Claim> findByCustomerId(UUID customerId, Pageable pageable);
    Page<Claim> findByCurrentStatus(String status, Pageable pageable);

    // Reporting queries
    @Query("SELECT COUNT(c) FROM Claim c WHERE c.currentStatus = :status")
    Long countByCurrentStatus(@Param("status") String status);

    @Query("SELECT COUNT(c) FROM Claim c WHERE c.submittedAt BETWEEN :start AND :end")
    Long countBySubmittedAtBetween(@Param("start") Instant start, @Param("end") Instant end);

    @Query("SELECT COUNT(c) FROM Claim c WHERE c.submittedAt < :date")
    Long countBySubmittedAtBefore(@Param("date") Instant date);

    @Query("SELECT COUNT(c) FROM Claim c WHERE c.currentStatus NOT IN :statuses")
    Long countByCurrentStatusNotIn(@Param("statuses") List<String> statuses);

    @Query("SELECT COUNT(c) FROM Claim c WHERE c.policy.coverageLimit > :amount")
    Long countByPolicyCoverageLimitGreaterThan(@Param("amount") BigDecimal amount);
}

