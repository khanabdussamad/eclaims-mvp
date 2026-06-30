package com.nagarro.eclaims.adjudication.repository;

import com.nagarro.eclaims.adjudication.entity.AdjustorDecision;
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
public interface AdjustorDecisionRepository extends JpaRepository<AdjustorDecision, UUID> {

    @Query("SELECT ad FROM AdjustorDecision ad WHERE ad.claim.id = :claimId")
    Optional<AdjustorDecision> findByClaimId(@Param("claimId") UUID claimId);

    @Query("SELECT ad FROM AdjustorDecision ad WHERE ad.adjustorUser.id = :adjustorId " +
           "ORDER BY ad.submittedAt DESC")
    Page<AdjustorDecision> findByAdjustor(@Param("adjustorId") UUID adjustorId, Pageable pageable);

    @Query("SELECT ad FROM AdjustorDecision ad WHERE ad.decision = :decision AND ad.submittedAt IS NOT NULL " +
           "ORDER BY ad.submittedAt DESC")
    List<AdjustorDecision> findByDecision(@Param("decision") String decision);

    @Query("SELECT ad FROM AdjustorDecision ad WHERE ad.adjustorUser.id = :adjustorId " +
           "AND ad.submittedAt IS NOT NULL ORDER BY ad.decisionDate DESC")
    List<AdjustorDecision> findSubmittedByAdjustor(@Param("adjustorId") UUID adjustorId);

    @Query("SELECT COUNT(ad) FROM AdjustorDecision ad WHERE ad.adjustorUser.id = :adjustorId " +
           "AND ad.submittedAt IS NULL")
    Long countPendingDecisions(@Param("adjustorId") UUID adjustorId);
}

