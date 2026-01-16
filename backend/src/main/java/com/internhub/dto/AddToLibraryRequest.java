package com.internhub.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddToLibraryRequest {

    @NotNull(message = "Internship ID is required")
    private Long internshipId;

    @NotNull(message = "Document ID is required")
    private Long documentId;

    private String title;
    private String description;
    private String keywords;
    private Boolean isPublic = true;
    private Boolean isFeatured = false;
}
