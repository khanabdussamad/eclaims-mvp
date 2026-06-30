package com.nagarro.eclaims.document.entity;

import com.nagarro.eclaims.claim.entity.Claim;
import com.nagarro.eclaims.common.entity.BaseEntity;
import com.nagarro.eclaims.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "documents", indexes = {
    @Index(name = "idx_documents_claim_id", columnList = "claim_id"),
    @Index(name = "idx_documents_uploaded_by", columnList = "uploaded_by_user_id"),
    @Index(name = "idx_documents_active", columnList = "active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "claim_id", nullable = false)
    private Claim claim;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_user_id", nullable = false)
    private User uploadedBy;

    @Column(name = "document_type", nullable = false, length = 64)
    private String documentType; // CLAIM_FORM, POLICE_REPORT, PHOTOS, INVOICE, REPAIR_ESTIMATE, etc.

    @Column(name = "original_file_name", nullable = false, length = 255)
    private String originalFileName;

    @Column(name = "content_type", nullable = false, length = 128)
    private String contentType; // application/pdf, image/jpeg, etc.

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "storage_provider", nullable = false, length = 32)
    private String storageProvider; // MINIO, S3, etc.

    @Column(name = "storage_bucket", nullable = false, length = 128)
    private String storageBucket;

    @Column(name = "storage_key", nullable = false, length = 512)
    private String storageKey; // Path in storage

    @Column(name = "checksum", length = 128)
    private String checksum; // MD5 or SHA256 for integrity verification

    @Column(name = "version", nullable = false)
    @Builder.Default
    private Integer version = 1;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private Instant uploadedAt;

    @PrePersist
    protected void onCreate() {
        if (uploadedAt == null) {
            uploadedAt = Instant.now();
        }
    }
}

