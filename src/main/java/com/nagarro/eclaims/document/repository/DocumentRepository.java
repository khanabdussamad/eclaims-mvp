package com.nagarro.eclaims.document.repository;

import com.nagarro.eclaims.document.entity.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {
    Page<Document> findByClaimIdAndActive(UUID claimId, Boolean active, Pageable pageable);

    List<Document> findByClaimIdAndActive(UUID claimId, Boolean active);

    Optional<Document> findByIdAndActive(UUID id, Boolean active);

    Page<Document> findByUploadedByIdAndActive(UUID userId, Boolean active, Pageable pageable);

    Long countByClaimId(UUID claimId);
}

