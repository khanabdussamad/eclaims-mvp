package com.nagarro.eclaims.workshop.repository;

import com.nagarro.eclaims.workshop.entity.WorkshopClaim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkshopClaimRepository extends JpaRepository<WorkshopClaim, UUID> {
    Optional<WorkshopClaim> findByClaimId(UUID claimId);
}

