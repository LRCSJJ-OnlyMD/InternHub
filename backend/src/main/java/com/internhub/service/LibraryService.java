package com.internhub.service;

import com.internhub.dto.*;
import com.internhub.exception.ResourceNotFoundException;
import com.internhub.exception.UnauthorizedException;
import com.internhub.model.*;
import com.internhub.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing the document library. Handles uploading validated
 * internship reports to Cloudinary and provides search/browse functionality.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LibraryService {

    private final LibraryDocumentRepository libraryRepository;
    private final InternshipRepository internshipRepository;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final SectorRepository sectorRepository;
    private final CloudinaryService cloudinaryService;

    /**
     * Add a validated internship report to the library
     */
    @Transactional
    public LibraryDocumentDTO addToLibrary(Long userId, AddToLibraryRequest request) throws IOException {
        // Verify the internship exists and is validated
        Internship internship = internshipRepository.findById(request.getInternshipId())
                .orElseThrow(() -> new ResourceNotFoundException("Internship not found"));

        if (internship.getStatus() != InternshipStatus.VALIDATED) {
            throw new IllegalStateException("Only validated internships can be added to the library");
        }

        // Verify the user is the instructor who validated or admin
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() != Role.ADMIN
                && (internship.getInstructor() == null || !internship.getInstructor().getId().equals(userId))) {
            throw new UnauthorizedException("Only the assigned instructor or admin can add to library");
        }

        // Check if already in library
        if (libraryRepository.findByInternshipId(request.getInternshipId()).isPresent()) {
            throw new IllegalStateException("This internship report is already in the library");
        }

        // Get the original document
        Document originalDoc = documentRepository.findById(request.getDocumentId())
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        // Upload to Cloudinary
        String publicId = "intern_" + internship.getId() + "_" + System.currentTimeMillis();
        // Note: In production, you'd get the file from storage and upload to Cloudinary
        // For now, we'll store the reference

        // Create library document
        LibraryDocument libraryDoc = new LibraryDocument();
        libraryDoc.setInternship(internship);
        libraryDoc.setOriginalDocument(originalDoc);
        libraryDoc.setAuthorName(internship.getStudent().getFirstName() + " "
                + internship.getStudent().getLastName());
        libraryDoc.setAuthorEmail(internship.getStudent().getEmail());
        libraryDoc.setValidatedBy(user);
        libraryDoc.setTitle(request.getTitle() != null ? request.getTitle() : internship.getTitle());
        libraryDoc.setDescription(request.getDescription() != null
                ? request.getDescription() : internship.getDescription());
        libraryDoc.setCompanyName(internship.getCompanyName());
        libraryDoc.setSector(internship.getSector());
        libraryDoc.setAcademicYear(calculateAcademicYear(internship.getStartDate().getYear()));
        libraryDoc.setKeywords(request.getKeywords());

        // File info from original document
        libraryDoc.setFileName(originalDoc.getFileName());
        libraryDoc.setOriginalFileName(originalDoc.getOriginalFileName());
        libraryDoc.setFileSize(originalDoc.getFileSize());
        libraryDoc.setContentType(originalDoc.getContentType());
        libraryDoc.setFileFormat(extractFileFormat(originalDoc.getOriginalFileName()));

        // Cloudinary info (placeholder - real implementation would upload file)
        libraryDoc.setCloudinaryPublicId(publicId);
        libraryDoc.setCloudinaryUrl(originalDoc.getFilePath());  // Temporary
        libraryDoc.setCloudinarySecureUrl(originalDoc.getFilePath());  // Temporary

        libraryDoc.setStatus(LibraryDocument.LibraryDocumentStatus.ACTIVE);
        libraryDoc.setIsPublic(request.getIsPublic() != null ? request.getIsPublic() : true);
        libraryDoc.setIsFeatured(request.getIsFeatured() != null ? request.getIsFeatured() : false);
        libraryDoc.setValidatedAt(LocalDateTime.now());

        libraryDoc = libraryRepository.save(libraryDoc);
        log.info("Document added to library: {} for internship {}", libraryDoc.getId(), internship.getId());

        return toDTO(libraryDoc);
    }

    /**
     * Upload a document directly to library (with Cloudinary)
     */
    @Transactional
    public LibraryDocumentDTO uploadToLibrary(Long userId, Long internshipId,
            MultipartFile file,
            String title, String description,
            String keywords) throws IOException {
        Internship internship = internshipRepository.findById(internshipId)
                .orElseThrow(() -> new ResourceNotFoundException("Internship not found"));

        if (internship.getStatus() != InternshipStatus.VALIDATED) {
            throw new IllegalStateException("Only validated internships can be added to the library");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Upload to Cloudinary
        String publicId = "intern_report_" + internshipId + "_" + System.currentTimeMillis();
        Map<String, Object> uploadResult = cloudinaryService.uploadToLibrary(file, publicId);

        // Create library document
        LibraryDocument libraryDoc = new LibraryDocument();
        libraryDoc.setInternship(internship);
        libraryDoc.setAuthorName(internship.getStudent().getFirstName() + " "
                + internship.getStudent().getLastName());
        libraryDoc.setAuthorEmail(internship.getStudent().getEmail());
        libraryDoc.setValidatedBy(user);
        libraryDoc.setTitle(title != null ? title : internship.getTitle());
        libraryDoc.setDescription(description != null ? description : internship.getDescription());
        libraryDoc.setCompanyName(internship.getCompanyName());
        libraryDoc.setSector(internship.getSector());
        libraryDoc.setAcademicYear(calculateAcademicYear(internship.getStartDate().getYear()));
        libraryDoc.setKeywords(keywords);

        // File info
        libraryDoc.setFileName(file.getOriginalFilename());
        libraryDoc.setOriginalFileName(file.getOriginalFilename());
        libraryDoc.setFileSize(file.getSize());
        libraryDoc.setContentType(file.getContentType());
        libraryDoc.setFileFormat(extractFileFormat(file.getOriginalFilename()));

        // Cloudinary info
        libraryDoc.setCloudinaryPublicId((String) uploadResult.get("public_id"));
        libraryDoc.setCloudinaryUrl((String) uploadResult.get("url"));
        libraryDoc.setCloudinarySecureUrl((String) uploadResult.get("secure_url"));

        libraryDoc.setStatus(LibraryDocument.LibraryDocumentStatus.ACTIVE);
        libraryDoc.setValidatedAt(LocalDateTime.now());

        libraryDoc = libraryRepository.save(libraryDoc);
        log.info("Document uploaded to library via Cloudinary: {}", libraryDoc.getId());

        return toDTO(libraryDoc);
    }

    /**
     * Get all public documents with pagination
     */
    @Transactional(readOnly = true)
    public Page<LibraryDocumentDTO> getAllDocuments(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return libraryRepository.findAllPublicDocuments(pageable).map(this::toDTO);
    }

    /**
     * Get documents by sector
     */
    @Transactional(readOnly = true)
    public Page<LibraryDocumentDTO> getDocumentsBySector(Long sectorId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return libraryRepository.findBySectorId(sectorId, pageable).map(this::toDTO);
    }

    /**
     * Get documents by academic year
     */
    @Transactional(readOnly = true)
    public Page<LibraryDocumentDTO> getDocumentsByYear(String year, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return libraryRepository.findByAcademicYear(year, pageable).map(this::toDTO);
    }

    /**
     * Search documents
     */
    @Transactional(readOnly = true)
    public Page<LibraryDocumentDTO> searchDocuments(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return libraryRepository.searchDocuments(query, pageable).map(this::toDTO);
    }

    /**
     * Get a single document by ID
     */
    @Transactional
    public LibraryDocumentDTO getDocument(Long id) {
        LibraryDocument doc = libraryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        // Increment view count
        libraryRepository.incrementViewCount(id);
        doc.incrementViewCount();  // Update local copy for response

        return toDTO(doc);
    }

    /**
     * Get download URL and increment download count
     */
    @Transactional
    public String getDownloadUrl(Long id) {
        LibraryDocument doc = libraryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        libraryRepository.incrementDownloadCount(id);

        return doc.getCloudinarySecureUrl() != null
                ? doc.getCloudinarySecureUrl() : doc.getCloudinaryUrl();
    }

    /**
     * Get featured documents
     */
    @Transactional(readOnly = true)
    public List<LibraryDocumentDTO> getFeaturedDocuments() {
        return libraryRepository.findFeaturedDocuments()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get most popular (downloaded) documents
     */
    @Transactional(readOnly = true)
    public List<LibraryDocumentDTO> getMostPopular(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return libraryRepository.findMostDownloaded(pageable)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get library statistics
     */
    @Transactional(readOnly = true)
    public LibraryStatisticsDTO getStatistics() {
        Object result = libraryRepository.getTotalStatistics();
        Object[] stats;

        // Handle both List<Object[]> and Object[] return types
        if (result instanceof List) {
            List<?> resultList = (List<?>) result;
            if (resultList.isEmpty()) {
                stats = new Object[]{0L, 0L, 0L};
            } else {
                stats = (Object[]) resultList.get(0);
            }
        } else if (result instanceof Object[]) {
            stats = (Object[]) result;
        } else {
            stats = new Object[]{0L, 0L, 0L};
        }

        // Count by sector
        Map<String, Long> bySector = new HashMap<>();
        for (Object[] row : libraryRepository.countBySector()) {
            Long sectorId = ((Number) row[0]).longValue();
            Long count = ((Number) row[1]).longValue();
            sectorRepository.findById(sectorId).ifPresent(s
                    -> bySector.put(s.getName(), count));
        }

        // Count by year
        Map<String, Long> byYear = new LinkedHashMap<>();
        for (Object[] row : libraryRepository.countByYear()) {
            byYear.put((String) row[0], ((Number) row[1]).longValue());
        }

        // Get featured and recent
        List<LibraryDocumentDTO> featured = getFeaturedDocuments();
        List<LibraryDocumentDTO> recent = getAllDocuments(0, 5).getContent();
        List<LibraryDocumentDTO> popular = getMostPopular(5);

        return LibraryStatisticsDTO.builder()
                .totalDocuments(((Number) stats[0]).longValue())
                .totalViews(((Number) stats[1]).longValue())
                .totalDownloads(((Number) stats[2]).longValue())
                .documentsBySector(bySector)
                .documentsByYear(byYear)
                .featuredDocuments(featured)
                .recentDocuments(recent)
                .popularDocuments(popular)
                .build();
    }

    /**
     * Get all available academic years
     */
    @Transactional(readOnly = true)
    public List<String> getAvailableYears() {
        return libraryRepository.findAllAcademicYears();
    }

    /**
     * Toggle featured status (admin only)
     */
    @Transactional
    public LibraryDocumentDTO toggleFeatured(Long id, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() != Role.ADMIN) {
            throw new UnauthorizedException("Only admins can feature documents");
        }

        LibraryDocument doc = libraryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        doc.setIsFeatured(!doc.getIsFeatured());
        doc = libraryRepository.save(doc);

        return toDTO(doc);
    }

    /**
     * Remove document from library (soft delete)
     */
    @Transactional
    public void removeFromLibrary(Long id, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() != Role.ADMIN) {
            throw new UnauthorizedException("Only admins can remove documents from library");
        }

        LibraryDocument doc = libraryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        doc.setStatus(LibraryDocument.LibraryDocumentStatus.REMOVED);
        libraryRepository.save(doc);

        log.info("Document {} removed from library by user {}", id, userId);
    }

    // Helper methods
    private String calculateAcademicYear(int startYear) {
        int currentMonth = LocalDateTime.now().getMonthValue();
        int currentYear = Year.now().getValue();

        // If internship started before September, it's previous academic year
        if (startYear < currentYear || (startYear == currentYear && currentMonth < 9)) {
            return (startYear - 1) + "-" + startYear;
        }
        return startYear + "-" + (startYear + 1);
    }

    private String extractFileFormat(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "unknown";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    private LibraryDocumentDTO toDTO(LibraryDocument doc) {
        return LibraryDocumentDTO.builder()
                .id(doc.getId())
                .internshipId(doc.getInternship().getId())
                .internshipTitle(doc.getInternship().getTitle())
                .authorName(doc.getAuthorName())
                .authorEmail(doc.getAuthorEmail())
                .validatedById(doc.getValidatedBy() != null ? doc.getValidatedBy().getId() : null)
                .validatedByName(doc.getValidatedBy() != null
                        ? doc.getValidatedBy().getFirstName() + " " + doc.getValidatedBy().getLastName() : null)
                .title(doc.getTitle())
                .description(doc.getDescription())
                .companyName(doc.getCompanyName())
                .sectorId(doc.getSector().getId())
                .sectorName(doc.getSector().getName())
                .academicYear(doc.getAcademicYear())
                .keywords(doc.getKeywords())
                .fileName(doc.getFileName())
                .originalFileName(doc.getOriginalFileName())
                .fileSize(doc.getFileSize())
                .contentType(doc.getContentType())
                .fileFormat(doc.getFileFormat())
                .downloadUrl(doc.getCloudinarySecureUrl())
                .previewUrl(doc.getCloudinaryUrl())
                .status(doc.getStatus())
                .isPublic(doc.getIsPublic())
                .isFeatured(doc.getIsFeatured())
                .viewCount(doc.getViewCount())
                .downloadCount(doc.getDownloadCount())
                .uploadedAt(doc.getUploadedAt())
                .validatedAt(doc.getValidatedAt())
                .build();
    }
}
