package com.internhub.controller;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.internhub.dto.InternshipResponse;
import com.internhub.dto.InternshipSearchRequest;
import com.internhub.model.InternshipStatus;
import com.internhub.service.InternshipService;

/**
 * REST Controller for internship search operations. Provides enhanced search
 * functionality with multiple filters and pagination.
 */
@RestController
@RequestMapping("/api/internships")
@CrossOrigin(origins = "*", maxAge = 3600)
public class InternshipSearchController {

    private final InternshipService internshipService;

    public InternshipSearchController(InternshipService internshipService) {
        this.internshipService = internshipService;
    }

    /**
     * Enhanced search endpoint with multiple filters and pagination. GET
     * /api/internships/search
     *
     * Accessible by all authenticated users. Results filtered by role
     * permissions in frontend/business logic.
     */
    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<InternshipResponse>> searchInternships(
            @RequestParam(required = false) Long sectorId,
            @RequestParam(required = false) InternshipStatus status,
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long instructorId,
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) String instructorName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDateTo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        InternshipSearchRequest searchRequest = new InternshipSearchRequest();
        searchRequest.setSectorId(sectorId);
        searchRequest.setStatus(status);
        searchRequest.setCompanyName(companyName);
        searchRequest.setTitle(title);
        searchRequest.setStudentId(studentId);
        searchRequest.setInstructorId(instructorId);
        searchRequest.setStudentName(studentName);
        searchRequest.setInstructorName(instructorName);
        searchRequest.setStartDateFrom(startDateFrom);
        searchRequest.setStartDateTo(startDateTo);
        searchRequest.setEndDateFrom(endDateFrom);
        searchRequest.setEndDateTo(endDateTo);
        searchRequest.setPage(page);
        searchRequest.setSize(size);
        searchRequest.setSortBy(sortBy);
        searchRequest.setSortDirection(sortDirection);

        Page<InternshipResponse> results = internshipService.searchInternshipsEnhanced(searchRequest);
        return ResponseEntity.ok(results);
    }

    /**
     * POST version for complex search (when query params are too many). POST
     * /api/internships/search
     */
    @PostMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<InternshipResponse>> searchInternshipsPost(
            @RequestBody InternshipSearchRequest searchRequest) {
        Page<InternshipResponse> results = internshipService.searchInternshipsEnhanced(searchRequest);
        return ResponseEntity.ok(results);
    }
}
