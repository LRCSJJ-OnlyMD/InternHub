package com.internhub.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.internhub.model.Document;
import com.internhub.model.Document.DocumentType;
import com.internhub.model.Internship;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByInternshipOrderByCreatedAtDesc(Internship internship);

    List<Document> findByInternshipAndIsLatestVersionTrue(Internship internship);

    List<Document> findByInternshipAndDocumentType(Internship internship, DocumentType documentType);

    Optional<Document> findByInternshipAndDocumentTypeAndIsLatestVersionTrue(
            Internship internship, DocumentType documentType);

    @Query("SELECT d FROM Document d WHERE d.originalFileName = :originalFileName "
            + "AND d.internship = :internship ORDER BY d.version DESC")
    List<Document> findVersionHistory(@Param("originalFileName") String originalFileName,
            @Param("internship") Internship internship);

    @Query("SELECT MAX(d.version) FROM Document d WHERE d.originalFileName = :originalFileName "
            + "AND d.internship = :internship")
    Optional<Integer> findLatestVersion(@Param("originalFileName") String originalFileName,
            @Param("internship") Internship internship);

    Long countByInternship(Internship internship);

    @Query("SELECT SUM(d.fileSize) FROM Document d WHERE d.internship = :internship")
    Long sumFileSizeByInternship(@Param("internship") Internship internship);
}
