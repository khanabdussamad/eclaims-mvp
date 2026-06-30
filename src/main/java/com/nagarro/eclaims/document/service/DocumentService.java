package com.nagarro.eclaims.document.service;

import com.nagarro.eclaims.audit.service.AuditService;
import com.nagarro.eclaims.claim.entity.Claim;
import com.nagarro.eclaims.claim.repository.ClaimRepository;
import com.nagarro.eclaims.document.dto.DocumentListResponse;
import com.nagarro.eclaims.document.dto.DocumentResponse;
import com.nagarro.eclaims.document.entity.Document;
import com.nagarro.eclaims.document.repository.DocumentRepository;
import com.nagarro.eclaims.document.repository.DocumentVersionRepository;
import com.nagarro.eclaims.user.entity.User;
import com.nagarro.eclaims.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentVersionRepository documentVersionRepository;
    private final ClaimRepository claimRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    private static final String[] ALLOWED_TYPES = {"application/pdf", "image/jpeg", "image/png", "application/msword",
                                                      "application/vnd.openxmlformats-officedocument.wordprocessingml.document"};
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    public DocumentResponse uploadDocument(UUID claimId, String documentType, String originalFileName,
                                          String contentType, long fileSize, InputStream fileContent,
                                          UUID uploadedByUserId) {
        log.info("Uploading document for claim: {}, type: {}", claimId, documentType);

        // Validate claim exists
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new IllegalArgumentException("Claim not found: " + claimId));

        // Validate user exists
        User uploadedBy = userRepository.findById(uploadedByUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + uploadedByUserId));

        // Validate file size
        if (fileSize > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum allowed: " + MAX_FILE_SIZE);
        }

        // Validate content type
        boolean isAllowedType = java.util.Arrays.asList(ALLOWED_TYPES).contains(contentType);
        if (!isAllowedType) {
            throw new IllegalArgumentException("Content type not allowed: " + contentType);
        }

        // Store file in MinIO (mock for now)
        String storageKey = generateStorageKey(claimId, originalFileName);
        String storageBucket = "eclaims-documents";

        // Create document
        Document document = Document.builder()
                .claim(claim)
                .uploadedBy(uploadedBy)
                .documentType(documentType)
                .originalFileName(originalFileName)
                .contentType(contentType)
                .fileSize(fileSize)
                .storageProvider("MINIO")
                .storageBucket(storageBucket)
                .storageKey(storageKey)
                .version(1)
                .active(true)
                .build();

        document = documentRepository.save(document);

        // Audit log
        String roleCode = uploadedBy.getRoles().isEmpty() ? "USER" : uploadedBy.getRoles().iterator().next().getCode();
        auditService.logAction("DOCUMENT_UPLOADED", "DOCUMENT", document.getId().toString(),
                uploadedBy.getId(), roleCode,
                "Uploaded document: " + originalFileName, "", "", "");

        log.info("Document uploaded successfully: {}", document.getId());

        return mapToDocumentResponse(document);
    }

    @Transactional(readOnly = true)
    public DocumentResponse getDocument(UUID documentId) {
        log.info("Retrieving document: {}", documentId);

        Document document = documentRepository.findByIdAndActive(documentId, true)
                .orElseThrow(() -> new IllegalArgumentException("Document not found: " + documentId));

        return mapToDocumentResponse(document);
    }

    @Transactional(readOnly = true)
    public Page<DocumentListResponse> listClaimDocuments(UUID claimId, Pageable pageable) {
        log.info("Listing documents for claim: {}", claimId);

        Page<Document> documents = documentRepository.findByClaimIdAndActive(claimId, true, pageable);

        List<DocumentListResponse> responses = documents.getContent().stream()
                .map(doc -> new DocumentListResponse(
                        doc.getId().toString(),
                        doc.getDocumentType(),
                        doc.getOriginalFileName(),
                        doc.getFileSize(),
                        doc.getUploadedBy().getFullName(),
                        doc.getUploadedAt()
                ))
                .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, documents.getTotalElements());
    }

    public void deleteDocument(UUID documentId, UUID userId) {
        log.info("Deleting document: {}", documentId);

        Document document = documentRepository.findByIdAndActive(documentId, true)
                .orElseThrow(() -> new IllegalArgumentException("Document not found: " + documentId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // Soft delete
        document.setActive(false);
        documentRepository.save(document);

        // Audit log
        String roleCode = user.getRoles().isEmpty() ? "USER" : user.getRoles().iterator().next().getCode();
        auditService.logAction("DOCUMENT_DELETED", "DOCUMENT", document.getId().toString(),
                userId, roleCode,
                "Deleted document: " + document.getOriginalFileName(), "", "", "");

        log.info("Document deleted successfully: {}", documentId);
    }

    @Transactional(readOnly = true)
    public InputStream downloadDocument(UUID documentId, UUID userId) {
        log.info("Downloading document: {}", documentId);

        Document document = documentRepository.findByIdAndActive(documentId, true)
                .orElseThrow(() -> new IllegalArgumentException("Document not found: " + documentId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // Audit log
        String roleCode = user.getRoles().isEmpty() ? "USER" : user.getRoles().iterator().next().getCode();
        auditService.logAction("DOCUMENT_DOWNLOADED", "DOCUMENT", document.getId().toString(),
                userId, roleCode,
                "Downloaded document: " + document.getOriginalFileName(), "", "", "");

        // Return mock file stream (in production, fetch from MinIO)
        return new java.io.ByteArrayInputStream(new byte[0]);
    }

    private String generateStorageKey(UUID claimId, String fileName) {
        return "claims/" + claimId + "/" + UUID.randomUUID() + "/" + fileName;
    }

    private DocumentResponse mapToDocumentResponse(Document document) {
        return new DocumentResponse(
                document.getId().toString(),
                document.getClaim().getId().toString(),
                document.getDocumentType(),
                document.getOriginalFileName(),
                document.getContentType(),
                document.getFileSize(),
                document.getUploadedBy().getFullName(),
                document.getVersion(),
                document.getUploadedAt(),
                "/api/v1/documents/" + document.getId() + "/download"
        );
    }
}

