package com.internhub.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.internhub.config.JwtTokenProvider;
import com.internhub.dto.DocumentResponse;
import com.internhub.dto.InternshipResponse;
import com.internhub.dto.RefusalRequest;
import com.internhub.model.Document;
import com.internhub.service.DocumentService;
import com.internhub.service.InternshipService;

import jakarta.validation.Valid;

/**
 * REST Controller for Instructor operations. All methods protected with
 *
 * @PreAuthorize for RBAC. Thin controller - delegates to service layer.
 *
 * Endpoints: - GET /api/instructor/internships/pending - Get pending
 * internships for my sectors - POST /api/instructor/internships/{id}/validate -
 * Validate internship - POST /api/instructor/internships/{id}/refuse - Refuse
 * internship
 */
@RestController
@RequestMapping("/api/instructor/internships")
@PreAuthorize("hasAuthority('INSTRUCTOR')")
public class InstructorController {

    private final InternshipService internshipService;
    private final DocumentService documentService;
    private final JwtTokenProvider jwtTokenProvider;

    public InstructorController(InternshipService internshipService,
            DocumentService documentService,
            JwtTokenProvider jwtTokenProvider) {
        this.internshipService = internshipService;
        this.documentService = documentService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * Get pending validation internships for my sectors. Only shows
     * PENDING_VALIDATION status for assigned sectors.
     */
    @GetMapping("/pending")
    public ResponseEntity<List<InternshipResponse>> getPendingInternships(
            @RequestHeader("Authorization") String token) {

        Long instructorId = extractUserIdFromToken(token);
        List<InternshipResponse> internships
                = internshipService.getPendingInternshipsForInstructor(instructorId);

        return ResponseEntity.ok(internships);
    }

    /**
     * Get available internships for claiming - PENDING with no assigned
     * instructor in my sectors.
     */
    @GetMapping("/available")
    public ResponseEntity<List<InternshipResponse>> getAvailableInternships(
            @RequestHeader("Authorization") String token) {

        Long instructorId = extractUserIdFromToken(token);
        List<InternshipResponse> internships
                = internshipService.getAvailableInternshipsForInstructor(instructorId);

        return ResponseEntity.ok(internships);
    }

    /**
     * Claim an unassigned internship and assign it to current instructor.
     */
    @PostMapping("/{id}/claim")
    public ResponseEntity<InternshipResponse> claimInternship(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {

        Long instructorId = extractUserIdFromToken(token);
        InternshipResponse response = internshipService.claimInternship(id, instructorId);

        return ResponseEntity.ok(response);
    }

    /**
     * Get validated internships where I am the assigned instructor.
     */
    @GetMapping("/validated")
    public ResponseEntity<List<InternshipResponse>> getValidatedInternships(
            @RequestHeader("Authorization") String token) {

        Long instructorId = extractUserIdFromToken(token);
        List<InternshipResponse> internships
                = internshipService.getValidatedInternshipsForInstructor(instructorId);

        return ResponseEntity.ok(internships);
    }

    /**
     * Validate internship (PENDING_VALIDATION -> VALIDATED). Assigns current
     * instructor as encadrant.
     */
    @PostMapping("/{id}/validate")
    public ResponseEntity<InternshipResponse> validateInternship(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {

        Long instructorId = extractUserIdFromToken(token);
        InternshipResponse response = internshipService.validateInternship(id, instructorId);

        return ResponseEntity.ok(response);
    }

    /**
     * Refuse internship (PENDING_VALIDATION -> REFUSED). Requires mandatory
     * refusal comment.
     */
    @PostMapping("/{id}/refuse")
    public ResponseEntity<InternshipResponse> refuseInternship(
            @PathVariable Long id,
            @Valid @RequestBody RefusalRequest refusalRequest,
            @RequestHeader("Authorization") String token) {

        Long instructorId = extractUserIdFromToken(token);
        InternshipResponse response = internshipService.refuseInternship(
                id, refusalRequest, instructorId);

        return ResponseEntity.ok(response);
    }

    /**
     * Download internship report for supervised internships using
     * DocumentService.
     */
    @GetMapping("/{id}/report")
    public ResponseEntity<Resource> downloadReport(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) throws IOException {

        Long instructorId = extractUserIdFromToken(token);

        // Get internship and verify instructor is assigned
        InternshipResponse internship = internshipService.getInternshipById(id);
        if (!internship.getInstructorId().equals(instructorId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Find the report document for this internship
        List<DocumentResponse> documents = documentService.getInternshipDocuments(id);
        DocumentResponse reportDoc = documents.stream()
                .filter(doc -> doc.getDocumentType() == Document.DocumentType.REPORT && doc.getIsLatestVersion())
                .findFirst()
                .orElse(null);

        if (reportDoc == null) {
            return ResponseEntity.notFound().build();
        }

        // Download using DocumentService
        Resource resource = documentService.downloadDocument(reportDoc.getId());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + reportDoc.getFileName() + "\"")
                .body(resource);
    }

    /**
     * Extract user ID from JWT token.
     */
    private Long extractUserIdFromToken(String token) {
        String jwt = token.replace("Bearer ", "");
        return jwtTokenProvider.getUserIdFromToken(jwt);
    }
}
