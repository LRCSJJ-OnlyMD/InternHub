package com.internhub.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LibraryStatisticsDTO {

    private Long totalDocuments;
    private Long totalViews;
    private Long totalDownloads;
    private Map<String, Long> documentsBySector;
    private Map<String, Long> documentsByYear;
    private List<LibraryDocumentDTO> featuredDocuments;
    private List<LibraryDocumentDTO> recentDocuments;
    private List<LibraryDocumentDTO> popularDocuments;
}
