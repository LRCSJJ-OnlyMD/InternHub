package com.internhub.dto;

import com.internhub.model.Document.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponse {

    private Long id;
    private Long internshipId;
    private String internshipTitle;
    private Long uploadedById;
    private String uploadedByName;
    private String fileName;
    private String originalFileName;
    private Long fileSize;
    private String contentType;
    private DocumentType documentType;
    private Integer version;
    private String description;
    private Boolean isLatestVersion;
    private Long previousVersionId;
    private LocalDateTime createdAt;
    private String downloadUrl;
}
