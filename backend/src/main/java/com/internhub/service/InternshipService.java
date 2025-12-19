package com.internhub.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;

import com.internhub.dto.BulkOperationRequest;
import com.internhub.dto.BulkOperationResponse;
import com.internhub.dto.InternshipRequest;
import com.internhub.dto.InternshipResponse;
import com.internhub.dto.InternshipSearchRequest;
import com.internhub.dto.RefusalRequest;
import com.internhub.model.Internship;
import com.internhub.model.InternshipStatus;

/**
 * Service interface for Internship operations. Follows Dependency Inversion
 * Principle (DIP) - depend on abstraction, not concrete implementation. Follows
 * Interface Segregation Principle (ISP) - focused, cohesive interface. Follows
 * Single Responsibility Principle (SRP) - manages internship business logic
 * only.
 *
 * This interface can be implemented differently (e.g., mock for testing)
 * without changing clients.
 */
public interface InternshipService {

    // Student operations
    /**
     * Create a new internship (DRAFT status).
     *
     * @param request Internship data
     * @param studentId ID of the student creating the internship
     * @return Created internship response
     */
    InternshipResponse createInternship(InternshipRequest request, Long studentId);

    /**
     * Update an existing internship (only if DRAFT or REFUSED).
     *
     * @param id Internship ID
     * @param request Updated internship data
     * @param studentId ID of the student (for authorization check)
     * @return Updated internship response
     */
    InternshipResponse updateInternship(Long id, InternshipRequest request, Long studentId);

    /**
     * Submit internship for validation (DRAFT/REFUSED -> PENDING_VALIDATION).
     *
     * @param id Internship ID
     * @param studentId ID of the student (for authorization check)
     * @return Updated internship response
     */
    InternshipResponse submitInternship(Long id, Long studentId);

    /**
     * Get all internships for a student.
     *
     * @param studentId Student ID
     * @return List of internship responses
     */
    List<InternshipResponse> getStudentInternships(Long studentId);

    // Instructor operations
    /**
     * Get pending validation internships for instructor's sectors.
     *
     * @param instructorId Instructor ID
     * @return List of pending internship responses
     */
    List<InternshipResponse> getPendingInternshipsForInstructor(Long instructorId);

    /**
     * Get validated internships where the instructor is assigned.
     *
     * @param instructorId Instructor ID
     * @return List of validated internship responses
     */
    List<InternshipResponse> getValidatedInternshipsForInstructor(Long instructorId);

    /**
     * Get available internships for claiming - PENDING with no instructor in
     * instructor's sectors.
     *
     * @param instructorId Instructor ID
     * @return List of available internship responses
     */
    List<InternshipResponse> getAvailableInternshipsForInstructor(Long instructorId);

    /**
     * Claim an unassigned internship and assign to instructor.
     *
     * @param id Internship ID
     * @param instructorId ID of the instructor claiming
     * @return Updated internship response
     */
    InternshipResponse claimInternship(Long id, Long instructorId);

    /**
     * Validate an internship (PENDING_VALIDATION -> VALIDATED). Assigns the
     * instructor as encadrant.
     *
     * @param id Internship ID
     * @param instructorId ID of the validating instructor
     * @return Updated internship response
     */
    InternshipResponse validateInternship(Long id, Long instructorId);

    /**
     * Refuse an internship (PENDING_VALIDATION -> REFUSED).
     *
     * @param id Internship ID
     * @param refusalRequest Refusal comment
     * @param instructorId ID of the refusing instructor
     * @return Updated internship response
     */
    InternshipResponse refuseInternship(Long id, RefusalRequest refusalRequest, Long instructorId);

    // Admin operations
    /**
     * Get all internships (admin view).
     *
     * @return List of all internship responses
     */
    List<InternshipResponse> getAllInternships();

    /**
     * Get internship by ID.
     *
     * @param id Internship ID
     * @return Internship response
     */
    InternshipResponse getInternshipById(Long id);

    /**
     * Delete an internship (admin only).
     *
     * @param id Internship ID
     */
    void deleteInternship(Long id);

    /**
     * Reassign internship to different instructor (admin only).
     *
     * @param id Internship ID
     * @param instructorId New instructor ID
     * @return Updated internship response
     */
    InternshipResponse reassignInstructor(Long id, Long instructorId);

    // Search operations
    /**
     * Search internships with dynamic criteria.
     *
     * @param sectorId Filter by sector
     * @param status Filter by status
     * @param companyName Filter by company name (partial match)
     * @param studentId Filter by student
     * @param instructorId Filter by instructor
     * @param startDateFrom Filter by start date from
     * @param startDateTo Filter by start date to
     * @param endDateFrom Filter by end date from
     * @param endDateTo Filter by end date to
     * @return List of matching internship responses
     */
    List<InternshipResponse> searchInternships(
            Long sectorId,
            InternshipStatus status,
            String companyName,
            Long studentId,
            Long instructorId,
            LocalDate startDateFrom,
            LocalDate startDateTo,
            LocalDate endDateFrom,
            LocalDate endDateTo
    );

    /**
     * Search internships using JPA Specification.
     *
     * @param spec JPA Specification
     * @return List of matching internship responses
     */
    List<InternshipResponse> searchInternships(Specification<Internship> spec);

    /**
     * Enhanced search with pagination and sorting.
     *
     * @param searchRequest Search criteria with pagination
     * @return Page of matching internship responses
     */
    Page<InternshipResponse> searchInternshipsEnhanced(InternshipSearchRequest searchRequest);

    /**
     * Perform bulk operations on multiple internships.
     *
     * @param request Bulk operation request with internship IDs and operation
     * type
     * @param userId ID of the user performing the operation (for authorization)
     * @return Bulk operation response with results
     */
    BulkOperationResponse performBulkOperation(BulkOperationRequest request, Long userId);
}
