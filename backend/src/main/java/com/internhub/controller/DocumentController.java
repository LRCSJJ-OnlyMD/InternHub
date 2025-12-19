package com.internhub.controller;

import com.internhub.dto.DocumentHistoryResponse;
import com.internhub.dto.DocumentResponse;
import com.internhub.model.Document.DocumentType;
import com.internhub.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/upload")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DocumentResponse> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("internshipId") Long internshipId,
            @RequestParam("documentType") DocumentType documentType,
            @RequestParam(value = "description", required = false) String description,
            Authentication authentication) throws IOException {

        Long userId = Long.parseLong(authentication.getName());
        DocumentResponse response = documentService.uploadDocument(
                internshipId, file, documentType, description, userId);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/upload-version")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DocumentResponse> uploadNewVersion(
            @RequestParam("file") MultipartFile file,
            @RequestParam("internshipId") Long internshipId,
            @RequestParam("originalFileName") String originalFileName,
            @RequestParam(value = "description", required = false) String description,
            Authentication authentication) throws IOException {

        Long userId = Long.parseLong(authentication.getName());
        DocumentResponse response = documentService.uploadNewVersion(
                internshipId, originalFileName, file, description, userId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/internship/{internshipId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<DocumentResponse>> getInternshipDocuments(
            @PathVariable Long internshipId) {
        List<DocumentResponse> documents = documentService.getInternshipDocuments(internshipId);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/internship/{internshipId}/latest")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<DocumentResponse>> getLatestDocuments(
            @PathVariable Long internshipId) {
        List<DocumentResponse> documents = documentService.getLatestDocuments(internshipId);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/{documentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DocumentResponse> getDocument(@PathVariable Long documentId) {
        DocumentResponse document = documentService.getDocumentById(documentId);
        return ResponseEntity.ok(document);
    }

    @GetMapping("/internship/{internshipId}/history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DocumentHistoryResponse> getDocumentHistory(
            @PathVariable Long internshipId,
            @RequestParam("fileName") String fileName) {
        DocumentHistoryResponse history = documentService.getDocumentHistory(internshipId, fileName);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/{documentId}/download")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long documentId) throws IOException {
        Resource resource = documentService.downloadDocument(documentId);
        DocumentResponse document = documentService.getDocumentById(documentId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(document.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + document.getOriginalFileName() + "\"")
                .body(resource);
    }

    @DeleteMapping("/{documentId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable Long documentId,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        documentService.deleteDocument(documentId, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/internship/{internshipId}/all-versions")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<Void> deleteAllVersions(
            @PathVariable Long internshipId,
            @RequestParam("fileName") String fileName,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        documentService.deleteAllVersions(internshipId, fileName, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/validate-file-type")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Boolean> validateFileType(@RequestParam("contentType") String contentType) {
        boolean valid = documentService.validateFileType(contentType);
        return ResponseEntity.ok(valid);
    }

    @GetMapping("/validate-file-size")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Boolean> validateFileSize(@RequestParam("size") Long size) {
        boolean valid = documentService.validateFileSize(size);
        return ResponseEntity.ok(valid);
    }
}
