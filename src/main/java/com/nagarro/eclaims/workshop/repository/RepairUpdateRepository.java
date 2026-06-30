package com.nagarro.eclaims.workshop.repository;

import com.nagarro.eclaims.workshop.entity.RepairUpdate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RepairUpdateRepository extends JpaRepository<RepairUpdate, UUID> {
    List<RepairUpdate> findByClaimIdOrderByCreatedAtDesc(UUID claimId);

    Optional<RepairUpdate> findFirstByClaimIdOrderByCreatedAtDesc(UUID claimId);

    Page<RepairUpdate> findByClaimId(UUID claimId, Pageable pageable);
}

