package com.internhub.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.internhub.model.Internship;
import com.internhub.model.InternshipStatus;
import com.internhub.model.User;

/**
 * Repository for Internship entity. Extends JpaSpecificationExecutor for
 * dynamic, multi-criteria search. Follows ISP - provides only necessary query
 * methods.
 */
@Repository
public interface InternshipRepository extends JpaRepository<Internship, Long>,
        JpaSpecificationExecutor<Internship> {

    /**
     * Find all internships for a specific student.
     */
    List<Internship> findByStudent(User student);

    /**
     * Find all internships for a specific student by ID.
     */
    List<Internship> findByStudentId(Long studentId);

    /**
     * Find all internships assigned to a specific instructor.
     */
    List<Internship> findByInstructor(User instructor);

    /**
     * Find all internships by instructor ID and status.
     */
    List<Internship> findByInstructorIdAndStatus(Long instructorId, InternshipStatus status);

    /**
     * Find all internships by status.
     */
    List<Internship> findByStatus(InternshipStatus status);

    /**
     * Find internship by title and company name (for seeding checks).
     */
    java.util.Optional<Internship> findByTitleAndCompanyName(String title, String companyName);

    /**
     * Find pending validation internships for sectors assigned to an
     * instructor. Used by instructors to view internships they can validate.
     */
    @Query("SELECT i FROM Internship i WHERE i.status = :status "
            + "AND i.sector IN (SELECT s FROM User u JOIN u.sectors s WHERE u.id = :instructorId)")
    List<Internship> findPendingInternshipsForInstructor(
            @Param("status") InternshipStatus status,
            @Param("instructorId") Long instructorId
    );

    /**
     * Statistics: Count internships grouped by status.
     */
    @Query("SELECT i.status, COUNT(i) FROM Internship i GROUP BY i.status")
    List<Object[]> countByStatus();

    /**
     * Statistics: Count internships grouped by sector.
     */
    @Query("SELECT i.sector.name, COUNT(i) FROM Internship i GROUP BY i.sector.name")
    List<Object[]> countBySector();

    /**
     * Statistics: Count internships grouped by status and sector.
     */
    @Query("SELECT i.sector.name, i.status, COUNT(i) FROM Internship i "
            + "GROUP BY i.sector.name, i.status")
    List<Object[]> countByStatusAndSector();

    /**
     * Enhanced Statistics: Get total count by status.
     */
    Long countByStatus(InternshipStatus status);

    /**
     * Enhanced Statistics: Get average duration of completed internships.
     */
    @Query("SELECT AVG(CAST((i.endDate - i.startDate) AS double)) FROM Internship i WHERE i.status = 'COMPLETED'")
    Double getAverageDuration();

    /**
     * Enhanced Statistics: Count distinct students with internships.
     */
    @Query("SELECT COUNT(DISTINCT i.student.id) FROM Internship i")
    Long countDistinctStudents();

    /**
     * Enhanced Statistics: Count distinct instructors with internships.
     */
    @Query("SELECT COUNT(DISTINCT i.instructor.id) FROM Internship i WHERE i.instructor IS NOT NULL")
    Long countDistinctInstructors();

    /**
     * Enhanced Statistics: Top companies by internship count.
     */
    @Query("SELECT i.companyName, COUNT(i) FROM Internship i GROUP BY i.companyName ORDER BY COUNT(i) DESC")
    List<Object[]> getTopCompaniesByCount();

    /**
     * Enhanced Statistics: Count internships by instructor.
     */
    @Query("SELECT i.instructor.firstName, i.instructor.lastName, COUNT(i) FROM Internship i "
            + "WHERE i.instructor IS NOT NULL GROUP BY i.instructor.id, i.instructor.firstName, i.instructor.lastName "
            + "ORDER BY COUNT(i) DESC")
    List<Object[]> getTopInstructorsByCount();

    /**
     * Instructor Statistics: Count internships by instructor and status.
     */
    @Query("SELECT COUNT(i) FROM Internship i WHERE i.instructor.id = :instructorId AND i.status = :status")
    Long countByInstructorIdAndStatus(@Param("instructorId") Long instructorId, @Param("status") InternshipStatus status);

    /**
     * Instructor Statistics: Count all internships by instructor.
     */
    @Query("SELECT COUNT(i) FROM Internship i WHERE i.instructor.id = :instructorId")
    Long countByInstructorId(@Param("instructorId") Long instructorId);

    /**
     * Instructor Statistics: Find all internships by instructor.
     */
    @Query("SELECT i FROM Internship i WHERE i.instructor.id = :instructorId")
    List<Internship> findByInstructorId(@Param("instructorId") Long instructorId);

    /**
     * Instructor Statistics: Count distinct students supervised.
     */
    @Query("SELECT COUNT(DISTINCT i.student.id) FROM Internship i WHERE i.instructor.id = :instructorId")
    Long countDistinctStudentsByInstructor(@Param("instructorId") Long instructorId);

    /**
     * Student Statistics: Count distinct sectors explored.
     */
    @Query("SELECT DISTINCT i.sector.name FROM Internship i WHERE i.student.id = :studentId")
    List<String> getDistinctSectorsByStudent(@Param("studentId") Long studentId);

    /**
     * Student Statistics: Get total days interned.
     */
    @Query("SELECT SUM(CAST((i.endDate - i.startDate) AS integer)) FROM Internship i "
            + "WHERE i.student.id = :studentId AND i.status = 'COMPLETED'")
    Integer getTotalDaysInternedByStudent(@Param("studentId") Long studentId);

    /**
     * Find internships created between two dates (for export filtering).
     */
    List<Internship> findByCreatedAtBetween(LocalDateTime fromDate, LocalDateTime toDate);
}
