package com.nagarro.eclaims.document.entity;

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
@Table(name = "document_versions", uniqueConstraints = {
    @UniqueConstraint(name = "uq_document_version", columnNames = {"document_id", "version"})
}, indexes = {
    @Index(name = "idx_doc_versions_document_id", columnList = "document_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentVersion extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "storage_key", nullable = false, length = 512)
    private String storageKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_user_id", nullable = false)
    private User uploadedBy;

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private Instant uploadedAt;

    @PrePersist
    protected void onCreate() {
        if (uploadedAt == null) {
            uploadedAt = Instant.now();
        }
    }
}

