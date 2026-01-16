package com.internhub.dto;

import com.internhub.model.LibraryDocument.LibraryDocumentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LibraryDocumentDTO {

    private Long id;
    private Long internshipId;
    private String internshipTitle;

    // Author info
    private String authorName;
    private String authorEmail;
    private Long validatedById;
    private String validatedByName;

    // Document metadata
    private String title;
    private String description;
    private String companyName;
    private Long sectorId;
    private String sectorName;
    private String academicYear;
    private String keywords;

    // File info
    private String fileName;
    private String originalFileName;
    private Long fileSize;
    private String contentType;
    private String fileFormat;
    private String downloadUrl;
    private String previewUrl;

    // Status
    private LibraryDocumentStatus status;
    private Boolean isPublic;
    private Boolean isFeatured;

    // Statistics
    private Integer viewCount;
    private Integer downloadCount;

    // Timestamps
    private LocalDateTime uploadedAt;
    private LocalDateTime validatedAt;
}
