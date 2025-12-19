package com.internhub.service.impl;

import com.internhub.dto.DocumentHistoryResponse;
import com.internhub.dto.DocumentHistoryResponse.DocumentVersionInfo;
import com.internhub.dto.DocumentResponse;
import com.internhub.exception.ResourceNotFoundException;
import com.internhub.model.Document;
import com.internhub.model.Document.DocumentType;
import com.internhub.model.Internship;
import com.internhub.model.User;
import com.internhub.repository.DocumentRepository;
import com.internhub.repository.InternshipRepository;
import com.internhub.repository.UserRepository;
import com.internhub.service.ActivityLogService;
import com.internhub.service.DocumentService;
import com.internhub.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final InternshipRepository internshipRepository;
    private final UserRepository userRepository;
    private final ActivityLogService activityLogService;
    private final NotificationService notificationService;

    @Value("${file.upload-dir:uploads/documents}")
    private String uploadDir;

    @Value("${file.max-size:10485760}") // 10MB default
    private Long maxFileSize;

    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "image/jpeg",
            "image/png",
            "text/plain"
    );

    @Override
    @Transactional
    public DocumentResponse uploadDocument(Long internshipId, MultipartFile file,
            DocumentType documentType, String description,
            Long userId) throws IOException {
        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (!validateFileType(file.getContentType())) {
            throw new IllegalArgumentException("Invalid file type: " + file.getContentType());
        }

        if (!validateFileSize(file.getSize())) {
            throw new IllegalArgumentException("File size exceeds maximum limit of "
                    + (maxFileSize / 1024 / 1024) + "MB");
        }

        // Get internship and user
        Internship internship = internshipRepository.findById(internshipId)
                .orElseThrow(() -> new ResourceNotFoundException("Internship", "id", internshipId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Check if this is a new version of existing document
        String originalFileName = file.getOriginalFilename();
        Integer version = documentRepository.findLatestVersion(originalFileName, internship)
                .map(v -> v + 1)
                .orElse(1);

        // If version > 1, mark previous version as not latest
        if (version > 1) {
            List<Document> previousVersions = documentRepository
                    .findVersionHistory(originalFileName, internship);
            for (Document prev : previousVersions) {
                if (prev.getIsLatestVersion()) {
                    prev.setIsLatestVersion(false);
                    documentRepository.save(prev);
                }
            }
        }

        // Generate unique filename
        String fileExtension = getFileExtension(originalFileName);
        String uniqueFileName = UUID.randomUUID().toString() + "_v" + version + fileExtension;

        // Create upload directory if not exists
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Save file
        Path filePath = uploadPath.resolve(uniqueFileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Create document entity
        Document document = new Document();
        document.setInternship(internship);
        document.setUploadedBy(user);
        document.setFileName(uniqueFileName);
        document.setOriginalFileName(originalFileName);
        document.setFilePath(filePath.toString());
        document.setFileSize(file.getSize());
        document.setContentType(file.getContentType());
        document.setDocumentType(documentType);
        document.setVersion(version);
        document.setDescription(description);
        document.setIsLatestVersion(true);

        // Set previous version reference if exists
        if (version > 1) {
            documentRepository.findVersionHistory(originalFileName, internship).stream()
                    .filter(d -> d.getVersion().equals(version - 1))
                    .findFirst()
                    .ifPresent(document::setPreviousVersion);
        }

        document = documentRepository.save(document);

        // Log activity (user already fetched earlier)
        activityLogService.logActivity(
                user.getEmail(),
                "DOCUMENT_UPLOAD",
                "Document",
                document.getId(),
                String.format("Uploaded document '%s' (version %d) for internship '%s'",
                        originalFileName, version, internship.getTitle())
        );

        // Send notification to instructor if exists
        if (internship.getInstructor() != null && !internship.getInstructor().getId().equals(userId)) {
            notificationService.createNotification(
                    internship.getInstructor(),
                    "REPORT_UPLOADED",
                    "New Document Uploaded",
                    String.format("Student %s %s uploaded '%s' for internship '%s'",
                            user.getFirstName(), user.getLastName(), originalFileName, internship.getTitle()),
                    "DOCUMENT",
                    document.getId()
            );
        }

        log.info("Document uploaded: {} (version {}) for internship {}",
                originalFileName, version, internshipId);

        return mapToResponse(document);
    }

    @Override
    @Transactional
    public DocumentResponse uploadNewVersion(Long internshipId, String originalFileName,
            MultipartFile file, String description,
            Long userId) throws IOException {
        return uploadDocument(internshipId, file,
                getDocumentTypeByFileName(originalFileName, internshipId),
                description, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentResponse> getInternshipDocuments(Long internshipId) {
        Internship internship = internshipRepository.findById(internshipId)
                .orElseThrow(() -> new ResourceNotFoundException("Internship", "id", internshipId));

        return documentRepository.findByInternshipOrderByCreatedAtDesc(internship).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentResponse> getLatestDocuments(Long internshipId) {
        Internship internship = internshipRepository.findById(internshipId)
                .orElseThrow(() -> new ResourceNotFoundException("Internship", "id", internshipId));

        return documentRepository.findByInternshipAndIsLatestVersionTrue(internship).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentResponse getDocumentById(Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", documentId));

        return mapToResponse(document);
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentHistoryResponse getDocumentHistory(Long internshipId, String originalFileName) {
        Internship internship = internshipRepository.findById(internshipId)
                .orElseThrow(() -> new ResourceNotFoundException("Internship", "id", internshipId));

        List<Document> versions = documentRepository.findVersionHistory(originalFileName, internship);

        if (versions.isEmpty()) {
            throw new ResourceNotFoundException("Document", "originalFileName", originalFileName);
        }

        Document latest = versions.stream()
                .filter(Document::getIsLatestVersion)
                .findFirst()
                .orElse(versions.get(0));

        DocumentHistoryResponse response = new DocumentHistoryResponse();
        response.setOriginalFileName(originalFileName);
        response.setDocumentType(latest.getDocumentType());
        response.setTotalVersions(versions.size());
        response.setLatestVersionId(latest.getId());

        List<DocumentVersionInfo> versionInfos = versions.stream()
                .map(doc -> {
                    DocumentVersionInfo info = new DocumentVersionInfo();
                    info.setId(doc.getId());
                    info.setVersion(doc.getVersion());
                    info.setFileSize(doc.getFileSize());
                    info.setUploadedByName(doc.getUploadedBy().getFirstName() + " "
                            + doc.getUploadedBy().getLastName());
                    info.setDescription(doc.getDescription());
                    info.setIsLatestVersion(doc.getIsLatestVersion());
                    info.setCreatedAt(doc.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    return info;
                })
                .collect(Collectors.toList());

        response.setVersions(versionInfos);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public Resource downloadDocument(Long documentId) throws IOException {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", documentId));

        Path filePath = Paths.get(document.getFilePath());
        Resource resource = new UrlResource(filePath.toUri());

        if (resource.exists() && resource.isReadable()) {
            return resource;
        } else {
            throw new IOException("File not found or not readable: " + document.getFileName());
        }
    }

    @Override
    @Transactional
    public void deleteDocument(Long documentId, Long userId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", documentId));

        // Delete file from filesystem
        try {
            Path filePath = Paths.get(document.getFilePath());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.error("Failed to delete file: {}", document.getFilePath(), e);
        }

        // Log activity before deletion
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        activityLogService.logActivity(
                user.getEmail(),
                "DOCUMENT_DELETE",
                "Document",
                documentId,
                String.format("Deleted document '%s' (version %d)",
                        document.getOriginalFileName(), document.getVersion())
        );

        documentRepository.delete(document);
        log.info("Document deleted: {} (version {})", document.getOriginalFileName(), document.getVersion());
    }

    @Override
    @Transactional
    public void deleteAllVersions(Long internshipId, String originalFileName, Long userId) {
        Internship internship = internshipRepository.findById(internshipId)
                .orElseThrow(() -> new ResourceNotFoundException("Internship", "id", internshipId));

        List<Document> versions = documentRepository.findVersionHistory(originalFileName, internship);

        for (Document document : versions) {
            deleteDocument(document.getId(), userId);
        }

        log.info("Deleted all versions of document: {}", originalFileName);
    }

    @Override
    public boolean validateFileType(String contentType) {
        return contentType != null && ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase());
    }

    @Override
    public boolean validateFileSize(Long fileSize) {
        return fileSize != null && fileSize <= maxFileSize;
    }

    private DocumentType getDocumentTypeByFileName(String fileName, Long internshipId) {
        Internship internship = internshipRepository.findById(internshipId)
                .orElseThrow(() -> new ResourceNotFoundException("Internship", "id", internshipId));

        return documentRepository.findVersionHistory(fileName, internship).stream()
                .findFirst()
                .map(Document::getDocumentType)
                .orElse(DocumentType.OTHER);
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }

    private DocumentResponse mapToResponse(Document document) {
        DocumentResponse response = new DocumentResponse();
        response.setId(document.getId());
        response.setInternshipId(document.getInternship().getId());
        response.setInternshipTitle(document.getInternship().getTitle());
        response.setUploadedById(document.getUploadedBy().getId());
        response.setUploadedByName(document.getUploadedBy().getFirstName() + " "
                + document.getUploadedBy().getLastName());
        response.setFileName(document.getFileName());
        response.setOriginalFileName(document.getOriginalFileName());
        response.setFileSize(document.getFileSize());
        response.setContentType(document.getContentType());
        response.setDocumentType(document.getDocumentType());
        response.setVersion(document.getVersion());
        response.setDescription(document.getDescription());
        response.setIsLatestVersion(document.getIsLatestVersion());
        response.setPreviousVersionId(document.getPreviousVersion() != null
                ? document.getPreviousVersion().getId() : null);
        response.setCreatedAt(document.getCreatedAt());
        response.setDownloadUrl("/api/documents/" + document.getId() + "/download");
        return response;
    }
}
