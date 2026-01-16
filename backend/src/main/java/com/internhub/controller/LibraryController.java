package com.internhub.controller;

import com.internhub.dto.AddToLibraryRequest;
import com.internhub.dto.LibraryDocumentDTO;
import com.internhub.dto.LibraryStatisticsDTO;
import com.internhub.service.LibraryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Controller for the Document Library functionality. Handles validated
 * internship reports stored in the library.
 */
@RestController
@RequestMapping("/api/library")
@RequiredArgsConstructor
@Slf4j
public class LibraryController {

    private final LibraryService libraryService;

    // ===================== Public Endpoints =====================
    /**
     * Browse all public documents with pagination
     */
    @GetMapping("/documents")
    public ResponseEntity<Page<LibraryDocumentDTO>> getAllDocuments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        Page<LibraryDocumentDTO> documents = libraryService.getAllDocuments(page, size);
        return ResponseEntity.ok(documents);
    }

    /**
     * Search documents by query
     */
    @GetMapping("/search")
    public ResponseEntity<Page<LibraryDocumentDTO>> searchDocuments(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        Page<LibraryDocumentDTO> documents = libraryService.searchDocuments(query, page, size);
        return ResponseEntity.ok(documents);
    }

    /**
     * Get documents by sector
     */
    @GetMapping("/sector/{sectorId}")
    public ResponseEntity<Page<LibraryDocumentDTO>> getDocumentsBySector(
            @PathVariable Long sectorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        Page<LibraryDocumentDTO> documents = libraryService.getDocumentsBySector(sectorId, page, size);
        return ResponseEntity.ok(documents);
    }

    /**
     * Get documents by academic year
     */
    @GetMapping("/year/{academicYear}")
    public ResponseEntity<Page<LibraryDocumentDTO>> getDocumentsByYear(
            @PathVariable String academicYear,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        Page<LibraryDocumentDTO> documents = libraryService.getDocumentsByYear(academicYear, page, size);
        return ResponseEntity.ok(documents);
    }

    /**
     * Get featured documents
     */
    @GetMapping("/featured")
    public ResponseEntity<List<LibraryDocumentDTO>> getFeaturedDocuments() {
        List<LibraryDocumentDTO> documents = libraryService.getFeaturedDocuments();
        return ResponseEntity.ok(documents);
    }

    /**
     * Get popular documents (most downloaded)
     */
    @GetMapping("/popular")
    public ResponseEntity<List<LibraryDocumentDTO>> getPopularDocuments(
            @RequestParam(defaultValue = "10") int limit) {
        List<LibraryDocumentDTO> documents = libraryService.getMostPopular(limit);
        return ResponseEntity.ok(documents);
    }

    /**
     * Get a single document by ID (increments view count)
     */
    @GetMapping("/documents/{documentId}")
    public ResponseEntity<LibraryDocumentDTO> getDocument(@PathVariable Long documentId) {
        LibraryDocumentDTO document = libraryService.getDocument(documentId);
        return ResponseEntity.ok(document);
    }

    /**
     * Get download URL for a document (increments download count)
     */
    @GetMapping("/documents/{documentId}/download")
    public ResponseEntity<Map<String, String>> getDownloadUrl(@PathVariable Long documentId) {
        String downloadUrl = libraryService.getDownloadUrl(documentId);
        return ResponseEntity.ok(Map.of("downloadUrl", downloadUrl));
    }

    /**
     * Get library statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<LibraryStatisticsDTO> getStatistics() {
        LibraryStatisticsDTO stats = libraryService.getStatistics();
        return ResponseEntity.ok(stats);
    }

    /**
     * Get all available academic years
     */
    @GetMapping("/years")
    public ResponseEntity<List<String>> getAvailableYears() {
        List<String> years = libraryService.getAvailableYears();
        return ResponseEntity.ok(years);
    }

    // ===================== Admin/Instructor Endpoints =====================
    /**
     * Add a validated internship report to the library
     */
    @PostMapping("/add")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<LibraryDocumentDTO> addToLibrary(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AddToLibraryRequest request) throws IOException {
        Long userId = extractUserId(userDetails);
        LibraryDocumentDTO document = libraryService.addToLibrary(userId, request);
        return ResponseEntity.ok(document);
    }

    /**
     * Upload a document directly to library with Cloudinary
     */
    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<LibraryDocumentDTO> uploadToLibrary(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long internshipId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String keywords) throws IOException {
        Long userId = extractUserId(userDetails);
        LibraryDocumentDTO document = libraryService.uploadToLibrary(
                userId, internshipId, file, title, description, keywords);
        return ResponseEntity.ok(document);
    }

    /**
     * Toggle featured status of a document
     */
    @PostMapping("/documents/{documentId}/toggle-featured")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LibraryDocumentDTO> toggleFeatured(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long documentId) {
        Long userId = extractUserId(userDetails);
        LibraryDocumentDTO document = libraryService.toggleFeatured(documentId, userId);
        return ResponseEntity.ok(document);
    }

    /**
     * Remove document from library
     */
    @DeleteMapping("/documents/{documentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeFromLibrary(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long documentId) {
        Long userId = extractUserId(userDetails);
        libraryService.removeFromLibrary(documentId, userId);
        return ResponseEntity.ok().build();
    }

    // ===================== Helper Methods =====================
    private Long extractUserId(UserDetails userDetails) {
        if (userDetails != null) {
            String username = userDetails.getUsername();
            try {
                return Long.parseLong(username);
            } catch (NumberFormatException e) {
                log.error("Unable to parse user ID from username: {}", username);
            }
        }
        throw new RuntimeException("Unable to extract user ID from authentication");
    }
}
