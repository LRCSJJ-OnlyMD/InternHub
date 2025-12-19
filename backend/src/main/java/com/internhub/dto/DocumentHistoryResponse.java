package com.internhub.dto;

import com.internhub.model.Document.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentHistoryResponse {

    private String originalFileName;
    private DocumentType documentType;
    private Integer totalVersions;
    private Long latestVersionId;
    private List<DocumentVersionInfo> versions;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DocumentVersionInfo {

        private Long id;
        private Integer version;
        private Long fileSize;
        private String uploadedByName;
        private String description;
        private Boolean isLatestVersion;
        private String createdAt;
    }
}
