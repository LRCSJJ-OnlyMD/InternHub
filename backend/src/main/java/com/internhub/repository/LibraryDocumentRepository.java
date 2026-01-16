package com.internhub.repository;

import com.internhub.model.LibraryDocument;
import com.internhub.model.LibraryDocument.LibraryDocumentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LibraryDocumentRepository extends JpaRepository<LibraryDocument, Long>,
        JpaSpecificationExecutor<LibraryDocument> {

    /**
     * Find all active public documents
     */
    @Query("SELECT d FROM LibraryDocument d WHERE d.status = 'ACTIVE' AND d.isPublic = true "
            + "ORDER BY d.uploadedAt DESC")
    Page<LibraryDocument> findAllPublicDocuments(Pageable pageable);

    /**
     * Find documents by sector
     */
    @Query("SELECT d FROM LibraryDocument d WHERE d.sector.id = :sectorId "
            + "AND d.status = 'ACTIVE' AND d.isPublic = true ORDER BY d.uploadedAt DESC")
    Page<LibraryDocument> findBySectorId(@Param("sectorId") Long sectorId, Pageable pageable);

    /**
     * Find documents by academic year
     */
    @Query("SELECT d FROM LibraryDocument d WHERE d.academicYear = :year "
            + "AND d.status = 'ACTIVE' AND d.isPublic = true ORDER BY d.uploadedAt DESC")
    Page<LibraryDocument> findByAcademicYear(@Param("year") String year, Pageable pageable);

    /**
     * Search documents by title, description, keywords, author, or company
     */
    @Query("SELECT d FROM LibraryDocument d WHERE d.status = 'ACTIVE' AND d.isPublic = true "
            + "AND (LOWER(d.title) LIKE LOWER(CONCAT('%', :query, '%')) "
            + "OR LOWER(d.description) LIKE LOWER(CONCAT('%', :query, '%')) "
            + "OR LOWER(d.keywords) LIKE LOWER(CONCAT('%', :query, '%')) "
            + "OR LOWER(d.authorName) LIKE LOWER(CONCAT('%', :query, '%')) "
            + "OR LOWER(d.companyName) LIKE LOWER(CONCAT('%', :query, '%'))) "
            + "ORDER BY d.uploadedAt DESC")
    Page<LibraryDocument> searchDocuments(@Param("query") String query, Pageable pageable);

    /**
     * Find featured documents
     */
    @Query("SELECT d FROM LibraryDocument d WHERE d.isFeatured = true "
            + "AND d.status = 'ACTIVE' AND d.isPublic = true ORDER BY d.uploadedAt DESC")
    List<LibraryDocument> findFeaturedDocuments();

    /**
     * Find most downloaded documents
     */
    @Query("SELECT d FROM LibraryDocument d WHERE d.status = 'ACTIVE' AND d.isPublic = true "
            + "ORDER BY d.downloadCount DESC")
    Page<LibraryDocument> findMostDownloaded(Pageable pageable);

    /**
     * Find most viewed documents
     */
    @Query("SELECT d FROM LibraryDocument d WHERE d.status = 'ACTIVE' AND d.isPublic = true "
            + "ORDER BY d.viewCount DESC")
    Page<LibraryDocument> findMostViewed(Pageable pageable);

    /**
     * Find by internship ID (check if already in library)
     */
    Optional<LibraryDocument> findByInternshipId(Long internshipId);

    /**
     * Find by Cloudinary public ID
     */
    Optional<LibraryDocument> findByCloudinaryPublicId(String publicId);

    /**
     * Count documents by sector
     */
    @Query("SELECT d.sector.id, COUNT(d) FROM LibraryDocument d "
            + "WHERE d.status = 'ACTIVE' GROUP BY d.sector.id")
    List<Object[]> countBySector();

    /**
     * Count documents by year
     */
    @Query("SELECT d.academicYear, COUNT(d) FROM LibraryDocument d "
            + "WHERE d.status = 'ACTIVE' GROUP BY d.academicYear ORDER BY d.academicYear DESC")
    List<Object[]> countByYear();

    /**
     * Get all distinct academic years
     */
    @Query("SELECT DISTINCT d.academicYear FROM LibraryDocument d WHERE d.status = 'ACTIVE' "
            + "ORDER BY d.academicYear DESC")
    List<String> findAllAcademicYears();

    /**
     * Increment view count
     */
    @Modifying
    @Query("UPDATE LibraryDocument d SET d.viewCount = d.viewCount + 1 WHERE d.id = :id")
    void incrementViewCount(@Param("id") Long id);

    /**
     * Increment download count
     */
    @Modifying
    @Query("UPDATE LibraryDocument d SET d.downloadCount = d.downloadCount + 1 WHERE d.id = :id")
    void incrementDownloadCount(@Param("id") Long id);

    /**
     * Get total statistics
     */
    @Query("SELECT COUNT(d), COALESCE(SUM(d.viewCount), 0), COALESCE(SUM(d.downloadCount), 0) "
            + "FROM LibraryDocument d WHERE d.status = 'ACTIVE'")
    Object[] getTotalStatistics();

    /**
     * Find documents pending approval
     */
    @Query("SELECT d FROM LibraryDocument d WHERE d.status = 'PENDING' ORDER BY d.uploadedAt DESC")
    List<LibraryDocument> findPendingDocuments();
}
