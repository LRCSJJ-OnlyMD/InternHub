package com.internhub.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Represents a document stored in the library (Cloudinary). Only validated
 * internship reports are stored here. This creates a permanent archive
 * accessible to future students.
 */
@Entity
@Table(name = "library_documents", indexes = {
    @Index(name = "idx_library_documents_sector", columnList = "sector_id"),
    @Index(name = "idx_library_documents_year", columnList = "academic_year"),
    @Index(name = "idx_library_documents_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LibraryDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Reference to original internship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "internship_id", nullable = false)
    private Internship internship;

    // Reference to original document
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_document_id")
    private Document originalDocument;

    // Author info (denormalized for display without joins)
    @Column(name = "author_name", nullable = false)
    private String authorName;

    @Column(name = "author_email")
    private String authorEmail;

    // Instructor who validated
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "validated_by")
    private User validatedBy;

    // Document metadata
    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sector_id", nullable = false)
    private Sector sector;

    @Column(name = "academic_year", nullable = false)
    private String academicYear;  // e.g., "2025-2026"

    @Column(name = "keywords", length = 500)
    private String keywords;  // Comma-separated for search

    // Cloudinary storage info
    @Column(name = "cloudinary_public_id", nullable = false, unique = true)
    private String cloudinaryPublicId;

    @Column(name = "cloudinary_url", nullable = false, length = 1000)
    private String cloudinaryUrl;

    @Column(name = "cloudinary_secure_url", length = 1000)
    private String cloudinarySecureUrl;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "original_file_name", nullable = false)
    private String originalFileName;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "file_format")
    private String fileFormat;  // pdf, docx, etc.

    // Status and visibility
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private LibraryDocumentStatus status = LibraryDocumentStatus.ACTIVE;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = true;  // Visible to all users

    @Column(name = "is_featured", nullable = false)
    private Boolean isFeatured = false;  // Highlighted/recommended

    // Statistics
    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;

    @Column(name = "download_count", nullable = false)
    private Integer downloadCount = 0;

    // Timestamps
    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    @Column(name = "validated_at")
    private LocalDateTime validatedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Increment view count
     */
    public void incrementViewCount() {
        this.viewCount++;
    }

    /**
     * Increment download count
     */
    public void incrementDownloadCount() {
        this.downloadCount++;
    }

    public enum LibraryDocumentStatus {
        PENDING, // Uploaded but not yet approved for library
        ACTIVE, // Visible in library
        ARCHIVED, // Hidden from library but not deleted
        REMOVED     // Soft deleted
    }
}
