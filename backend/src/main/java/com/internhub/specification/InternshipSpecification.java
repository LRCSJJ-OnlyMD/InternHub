package com.internhub.specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.internhub.model.Internship;
import com.internhub.model.InternshipStatus;

import jakarta.persistence.criteria.Predicate;

/**
 * JPA Specification for dynamic, multi-criteria Internship search. Follows
 * Open/Closed Principle (OCP) - easily extensible for new search criteria.
 *
 * Supports filtering by: - Sector ID - Status - Company name (partial match) -
 * Student ID - Instructor ID - Date range (start/end dates)
 */
public class InternshipSpecification {

    /**
     * Private constructor to prevent instantiation (utility class).
     */
    private InternshipSpecification() {
    }

    /**
     * Build dynamic specification based on search criteria. Only non-null
     * criteria are applied.
     */
    public static Specification<Internship> buildSpecification(
            Long sectorId,
            InternshipStatus status,
            String companyName,
            Long studentId,
            Long instructorId,
            LocalDate startDateFrom,
            LocalDate startDateTo,
            LocalDate endDateFrom,
            LocalDate endDateTo
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by sector
            if (sectorId != null) {
                predicates.add(criteriaBuilder.equal(root.get("sector").get("id"), sectorId));
            }

            // Filter by status
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            // Filter by company name (case-insensitive partial match)
            if (companyName != null && !companyName.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("companyName")),
                        "%" + companyName.toLowerCase() + "%"
                ));
            }

            // Filter by student
            if (studentId != null) {
                predicates.add(criteriaBuilder.equal(root.get("student").get("id"), studentId));
            }

            // Filter by instructor
            if (instructorId != null) {
                predicates.add(criteriaBuilder.equal(root.get("instructor").get("id"), instructorId));
            }

            // Filter by start date range
            if (startDateFrom != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("startDate"), startDateFrom));
            }
            if (startDateTo != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("startDate"), startDateTo));
            }

            // Filter by end date range
            if (endDateFrom != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("endDate"), endDateFrom));
            }
            if (endDateTo != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("endDate"), endDateTo));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    /**
     * Enhanced build specification with additional filters (title,
     * student/instructor names).
     */
    public static Specification<Internship> buildEnhancedSpecification(
            Long sectorId,
            InternshipStatus status,
            String companyName,
            String title,
            Long studentId,
            Long instructorId,
            String studentName,
            String instructorName,
            LocalDate startDateFrom,
            LocalDate startDateTo,
            LocalDate endDateFrom,
            LocalDate endDateTo
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by sector
            if (sectorId != null) {
                predicates.add(criteriaBuilder.equal(root.get("sector").get("id"), sectorId));
            }

            // Filter by status
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            // Filter by company name (case-insensitive partial match)
            if (companyName != null && !companyName.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("companyName")),
                        "%" + companyName.toLowerCase() + "%"
                ));
            }

            // Filter by title (case-insensitive partial match)
            if (title != null && !title.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("title")),
                        "%" + title.toLowerCase() + "%"
                ));
            }

            // Filter by student
            if (studentId != null) {
                predicates.add(criteriaBuilder.equal(root.get("student").get("id"), studentId));
            }

            // Filter by student name (firstName or lastName)
            if (studentName != null && !studentName.trim().isEmpty()) {
                String pattern = "%" + studentName.toLowerCase() + "%";
                Predicate firstNameMatch = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("student").get("firstName")), pattern);
                Predicate lastNameMatch = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("student").get("lastName")), pattern);
                predicates.add(criteriaBuilder.or(firstNameMatch, lastNameMatch));
            }

            // Filter by instructor
            if (instructorId != null) {
                predicates.add(criteriaBuilder.equal(root.get("instructor").get("id"), instructorId));
            }

            // Filter by instructor name (firstName or lastName)
            if (instructorName != null && !instructorName.trim().isEmpty()) {
                String pattern = "%" + instructorName.toLowerCase() + "%";
                Predicate firstNameMatch = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("instructor").get("firstName")), pattern);
                Predicate lastNameMatch = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("instructor").get("lastName")), pattern);
                predicates.add(criteriaBuilder.or(firstNameMatch, lastNameMatch));
            }

            // Filter by start date range
            if (startDateFrom != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("startDate"), startDateFrom));
            }
            if (startDateTo != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("startDate"), startDateTo));
            }

            // Filter by end date range
            if (endDateFrom != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("endDate"), endDateFrom));
            }
            if (endDateTo != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("endDate"), endDateTo));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    /**
     * Specification to find internships by sector ID.
     */
    public static Specification<Internship> hasSector(Long sectorId) {
        return (root, query, criteriaBuilder)
                -> criteriaBuilder.equal(root.get("sector").get("id"), sectorId);
    }

    /**
     * Specification to find internships by status.
     */
    public static Specification<Internship> hasStatus(InternshipStatus status) {
        return (root, query, criteriaBuilder)
                -> criteriaBuilder.equal(root.get("status"), status);
    }

    /**
     * Specification to find internships by student ID.
     */
    public static Specification<Internship> belongsToStudent(Long studentId) {
        return (root, query, criteriaBuilder)
                -> criteriaBuilder.equal(root.get("student").get("id"), studentId);
    }

    /**
     * Specification to find internships by instructor ID.
     */
    public static Specification<Internship> assignedToInstructor(Long instructorId) {
        return (root, query, criteriaBuilder)
                -> criteriaBuilder.equal(root.get("instructor").get("id"), instructorId);
    }

    /**
     * Specification to find internships by company name (partial,
     * case-insensitive).
     */
    public static Specification<Internship> companyNameContains(String companyName) {
        return (root, query, criteriaBuilder)
                -> criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("companyName")),
                        "%" + companyName.toLowerCase() + "%"
                );
    }
}
