package com.internhub.model;

/**
 * Enumeration representing the lifecycle status of an internship. Adheres to
 * the workflow: DRAFT -> PENDING_VALIDATION -> VALIDATED -> IN_PROGRESS ->
 * COMPLETED or REFUSED
 */
public enum InternshipStatus {
    DRAFT, // Initial state, student can modify
    PENDING, // Alias for PENDING_VALIDATION (for compatibility)
    PENDING_VALIDATION, // Submitted for instructor review
    VALIDATED, // Approved by instructor
    REFUSED, // Rejected by instructor
    REJECTED, // Alias for REFUSED (for compatibility)
    IN_PROGRESS, // Internship is currently active
    COMPLETED // Internship has been completed
}
