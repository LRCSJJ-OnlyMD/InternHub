package com.internhub.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

/**
 * Core entity representing an internship/stage. Manages the complete internship
 * lifecycle with status transitions. Follows SRP - focused solely on internship
 * data and relationships.
 *
 * Relationships: - ManyToOne with Student (owner) - ManyToOne with Instructor
 * (encadrant/supervisor) - ManyToOne with Sector
 */
@Entity
@Table(name = "internships")
public class Internship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "company_name", nullable = false, length = 200)
    private String companyName;

    @Column(name = "company_address", length = 500)
    private String companyAddress;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InternshipStatus status = InternshipStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id")
    private User instructor;  // Encadrant/supervisor, assigned upon validation

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sector_id", nullable = false)
    private Sector sector;

    @Column(name = "refusal_comment", columnDefinition = "TEXT")
    private String refusalComment;  // Mandatory if status is REFUSED

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;  // When status changed to PENDING_VALIDATION

    @Column(name = "validated_at")
    private LocalDateTime validatedAt;  // When status changed to VALIDATED

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Constructors
    public Internship() {
    }

    public Internship(String title, String companyName, LocalDate startDate, LocalDate endDate,
            User student, Sector sector) {
        this.title = title;
        this.companyName = companyName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.student = student;
        this.sector = sector;
        this.status = InternshipStatus.DRAFT;
    }

    // Business methods for status transitions (Open/Closed Principle - extensible behavior)
    /**
     * Submit internship for validation. Transition: DRAFT or REFUSED ->
     * PENDING_VALIDATION
     */
    public void submit() {
        if (this.status == InternshipStatus.DRAFT || this.status == InternshipStatus.REFUSED) {
            this.status = InternshipStatus.PENDING_VALIDATION;
            this.submittedAt = LocalDateTime.now();
            this.refusalComment = null;  // Clear previous refusal comment
        } else {
            throw new IllegalStateException("Cannot submit internship with status: " + this.status);
        }
    }

    /**
     * Validate internship and assign instructor. Transition: PENDING_VALIDATION
     * -> VALIDATED
     */
    public void validate(User instructor) {
        if (this.status != InternshipStatus.PENDING_VALIDATION) {
            throw new IllegalStateException("Cannot validate internship with status: " + this.status);
        }
        this.status = InternshipStatus.VALIDATED;
        this.instructor = instructor;
        this.validatedAt = LocalDateTime.now();
        this.refusalComment = null;
    }

    /**
     * Refuse internship with mandatory comment. Transition: PENDING_VALIDATION
     * -> REFUSED
     */
    public void refuse(String refusalComment) {
        if (this.status != InternshipStatus.PENDING_VALIDATION) {
            throw new IllegalStateException("Cannot refuse internship with status: " + this.status);
        }
        if (refusalComment == null || refusalComment.trim().isEmpty()) {
            throw new IllegalArgumentException("Refusal comment is mandatory");
        }
        this.status = InternshipStatus.REFUSED;
        this.refusalComment = refusalComment;
    }

    /**
     * Check if internship can be modified by student. Only DRAFT and REFUSED
     * statuses allow modification.
     */
    public boolean isModifiable() {
        return this.status == InternshipStatus.DRAFT || this.status == InternshipStatus.REFUSED;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyAddress() {
        return companyAddress;
    }

    public void setCompanyAddress(String companyAddress) {
        this.companyAddress = companyAddress;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public InternshipStatus getStatus() {
        return status;
    }

    public void setStatus(InternshipStatus status) {
        this.status = status;
    }

    public User getStudent() {
        return student;
    }

    public void setStudent(User student) {
        this.student = student;
    }

    public User getInstructor() {
        return instructor;
    }

    public void setInstructor(User instructor) {
        this.instructor = instructor;
    }

    public Sector getSector() {
        return sector;
    }

    public void setSector(Sector sector) {
        this.sector = sector;
    }

    public String getRefusalComment() {
        return refusalComment;
    }

    public void setRefusalComment(String refusalComment) {
        this.refusalComment = refusalComment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public LocalDateTime getValidatedAt() {
        return validatedAt;
    }

    public void setValidatedAt(LocalDateTime validatedAt) {
        this.validatedAt = validatedAt;
    }
}
