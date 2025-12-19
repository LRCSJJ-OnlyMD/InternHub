package com.internhub.service;

import java.io.IOException;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import com.internhub.dto.DocumentHistoryResponse;
import com.internhub.dto.DocumentResponse;
import com.internhub.model.Document.DocumentType;

public interface DocumentService {

    DocumentResponse uploadDocument(Long internshipId, MultipartFile file,
            DocumentType documentType, String description,
            Long userId) throws IOException;

    DocumentResponse uploadNewVersion(Long internshipId, String originalFileName,
            MultipartFile file, String description,
            Long userId) throws IOException;

    List<DocumentResponse> getInternshipDocuments(Long internshipId);

    List<DocumentResponse> getLatestDocuments(Long internshipId);

    DocumentResponse getDocumentById(Long documentId);

    DocumentHistoryResponse getDocumentHistory(Long internshipId, String originalFileName);

    Resource downloadDocument(Long documentId) throws IOException;

    void deleteDocument(Long documentId, Long userId);

    void deleteAllVersions(Long internshipId, String originalFileName, Long userId);

    boolean validateFileType(String contentType);

    boolean validateFileSize(Long fileSize);
}
