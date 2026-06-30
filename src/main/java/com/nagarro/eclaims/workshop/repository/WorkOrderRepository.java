package com.nagarro.eclaims.workshop.repository;

import com.nagarro.eclaims.workshop.entity.WorkOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkOrderRepository extends JpaRepository<WorkOrder, UUID> {
    List<WorkOrder> findByClaimId(UUID claimId);

    Optional<WorkOrder> findFirstByClaimIdOrderBySubmittedAtDesc(UUID claimId);
}

