package com.nagarro.eclaims.workshop.repository;

import com.nagarro.eclaims.workshop.entity.Workshop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkshopRepository extends JpaRepository<Workshop, UUID> {
    Page<Workshop> findByActiveAndZipCode(Boolean active, String zipCode, Pageable pageable);

    Page<Workshop> findByActive(Boolean active, Pageable pageable);

    Optional<Workshop> findByPartnerCode(String partnerCode);

    List<Workshop> findByActive(Boolean active);
}

