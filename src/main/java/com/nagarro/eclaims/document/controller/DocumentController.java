package com.nagarro.eclaims.document.controller;

import com.nagarro.eclaims.common.response.ApiResponse;
import com.nagarro.eclaims.document.dto.DocumentListResponse;
import com.nagarro.eclaims.document.dto.DocumentResponse;
import com.nagarro.eclaims.document.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/claims/{claimId}/documents")
    @PreAuthorize("hasAuthority('DOCUMENT_UPLOAD')")
    public ResponseEntity<ApiResponse<DocumentResponse>> uploadDocument(
            @PathVariable UUID claimId,
            @RequestParam("documentType") String documentType,
            @RequestParam("file") MultipartFile file,
            Principal principal) throws IOException {

        log.info("Document upload request for claim: {} by user: {}", claimId, principal.getName());

        UUID userId = UUID.fromString(principal.getName());

        DocumentResponse response = documentService.uploadDocument(
                claimId,
                documentType,
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSize(),
                file.getInputStream(),
                userId
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Document uploaded successfully", response));
    }

    @GetMapping("/claims/{claimId}/documents")
    @PreAuthorize("hasAuthority('DOCUMENT_VIEW')")
    public ResponseEntity<ApiResponse<Page<DocumentListResponse>>> listDocuments(
            @PathVariable UUID claimId,
            Pageable pageable) {

        log.info("Listing documents for claim: {}", claimId);

        Page<DocumentListResponse> documents = documentService.listClaimDocuments(claimId, pageable);

        return ResponseEntity.ok(ApiResponse.success("Documents retrieved successfully", documents));
    }

    @GetMapping("/documents/{documentId}")
    @PreAuthorize("hasAuthority('DOCUMENT_VIEW')")
    public ResponseEntity<ApiResponse<DocumentResponse>> getDocument(
            @PathVariable UUID documentId) {

        log.info("Retrieving document: {}", documentId);

        DocumentResponse response = documentService.getDocument(documentId);

        return ResponseEntity.ok(ApiResponse.success("Document retrieved successfully", response));
    }

    @GetMapping("/documents/{documentId}/download")
    @PreAuthorize("hasAuthority('DOCUMENT_VIEW')")
    public ResponseEntity<?> downloadDocument(
            @PathVariable UUID documentId,
            Principal principal) {

        log.info("Download request for document: {} by user: {}", documentId, principal.getName());

        UUID userId = UUID.fromString(principal.getName());

        try {
            documentService.downloadDocument(documentId, userId);
            return ResponseEntity.ok("File download initiated");
        } catch (Exception e) {
            log.error("Error downloading document: {}", documentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new com.nagarro.eclaims.common.response.ApiError("DOCUMENT_DOWNLOAD_ERROR", "Error downloading document"));
        }
    }

    @DeleteMapping("/documents/{documentId}")
    @PreAuthorize("hasAuthority('DOCUMENT_DELETE')")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(
            @PathVariable UUID documentId,
            Principal principal) {

        log.info("Delete request for document: {} by user: {}", documentId, principal.getName());

        UUID userId = UUID.fromString(principal.getName());

        documentService.deleteDocument(documentId, userId);

        return ResponseEntity.ok(ApiResponse.success("Document deleted successfully", null));
    }
}

