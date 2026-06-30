package com.nagarro.eclaims.workshop.repository;

import com.nagarro.eclaims.workshop.entity.FinalInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FinalInvoiceRepository extends JpaRepository<FinalInvoice, UUID> {
    Optional<FinalInvoice> findByClaimId(UUID claimId);
}

