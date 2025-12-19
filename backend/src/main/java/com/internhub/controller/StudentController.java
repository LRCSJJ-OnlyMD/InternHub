package com.internhub.controller;

import com.internhub.config.JwtTokenProvider;
import com.internhub.dto.DocumentResponse;
import com.internhub.dto.InternshipRequest;
import com.internhub.dto.InternshipResponse;
import com.internhub.model.Document;
import com.internhub.service.DocumentService;
import com.internhub.service.InternshipService;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for Student operations. All methods protected with
 *
 * @PreAuthorize for RBAC. Thin controller - delegates business logic to service
 * layer.
 *
 * Endpoints: - POST /api/student/internships - Create internship - PUT
 * /api/student/internships/{id} - Update internship - POST
 * /api/student/internships/{id}/submit - Submit for validation - GET
 * /api/student/internships - Get my internships - POST
 * /api/student/internships/{id}/report - Upload report - GET
 * /api/student/internships/{id}/report - Download report
 */
@RestController
@RequestMapping("/api/student/internships")
@PreAuthorize("hasAuthority('STUDENT')")
public class StudentController {

    private final InternshipService internshipService;
    private final DocumentService documentService;
    private final JwtTokenProvider jwtTokenProvider;

    public StudentController(
            InternshipService internshipService,
            DocumentService documentService,
            JwtTokenProvider jwtTokenProvider) {
        this.internshipService = internshipService;
        this.documentService = documentService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * Create new internship (DRAFT status).
     */
    @PostMapping
    public ResponseEntity<InternshipResponse> createInternship(
            @Valid @RequestBody InternshipRequest request,
            @RequestHeader("Authorization") String token) {

        Long studentId = extractUserIdFromToken(token);
        InternshipResponse response = internshipService.createInternship(request, studentId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update existing internship (only if DRAFT or REFUSED).
     */
    @PutMapping("/{id}")
    public ResponseEntity<InternshipResponse> updateInternship(
            @PathVariable Long id,
            @Valid @RequestBody InternshipRequest request,
            @RequestHeader("Authorization") String token) {

        Long studentId = extractUserIdFromToken(token);
        InternshipResponse response = internshipService.updateInternship(id, request, studentId);

        return ResponseEntity.ok(response);
    }

    /**
     * Submit internship for validation (DRAFT/REFUSED -> PENDING_VALIDATION).
     */
    @PostMapping("/{id}/submit")
    public ResponseEntity<InternshipResponse> submitInternship(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {

        Long studentId = extractUserIdFromToken(token);
        InternshipResponse response = internshipService.submitInternship(id, studentId);

        return ResponseEntity.ok(response);
    }

    /**
     * Get all my internships.
     */
    @GetMapping
    public ResponseEntity<List<InternshipResponse>> getMyInternships(
            @RequestHeader("Authorization") String token) {

        Long studentId = extractUserIdFromToken(token);
        List<InternshipResponse> internships = internshipService.getStudentInternships(studentId);

        return ResponseEntity.ok(internships);
    }

    /**
     * Upload internship report using DocumentService.
     */
    @PostMapping("/{id}/report")
    public ResponseEntity<Map<String, String>> uploadReport(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestHeader("Authorization") String token) {

        Long studentId = extractUserIdFromToken(token);

        // Verify ownership
        InternshipResponse internship = internshipService.getInternshipById(id);
        if (!internship.getStudentId().equals(studentId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Unauthorized: Not your internship"));
        }

        try {
            // Upload document using DocumentService
            DocumentResponse document = documentService.uploadDocument(
                    id,
                    file,
                    Document.DocumentType.REPORT,
                    "Internship Report - " + internship.getTitle(),
                    studentId
            );

            return ResponseEntity.ok(Map.of(
                    "message", "Report uploaded successfully",
                    "documentId", String.valueOf(document.getId()),
                    "filename", document.getFileName()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error uploading report: " + e.getMessage()));
        }
    }

    /**
     * Download internship report using DocumentService.
     */
    @GetMapping("/{id}/report")
    public ResponseEntity<?> downloadReport(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "false") boolean inline,
            @RequestHeader("Authorization") String token) {

        try {
            Long studentId = extractUserIdFromToken(token);

            // Verify ownership
            InternshipResponse internship = internshipService.getInternshipById(id);
            if (!internship.getStudentId().equals(studentId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Unauthorized: Not your internship"));
            }

            // Find the report document for this internship
            List<DocumentResponse> documents = documentService.getInternshipDocuments(id);
            DocumentResponse reportDoc = documents.stream()
                    .filter(doc -> doc.getDocumentType() == Document.DocumentType.REPORT && doc.getIsLatestVersion())
                    .findFirst()
                    .orElse(null);

            if (reportDoc == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "No report uploaded for this internship"));
            }

            // Download using DocumentService
            Resource resource = documentService.downloadDocument(reportDoc.getId());

            String dispositionType = inline ? "inline" : "attachment";

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, dispositionType + "; filename=\"" + reportDoc.getFileName() + "\"")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error retrieving report: " + e.getMessage()));
        }
    }

    /**
     * Extract user ID from JWT token.
     */
    private Long extractUserIdFromToken(String token) {
        String jwt = token.replace("Bearer ", "");
        return jwtTokenProvider.getUserIdFromToken(jwt);
    }
}
