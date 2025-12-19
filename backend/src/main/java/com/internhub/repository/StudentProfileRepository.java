package com.internhub.repository;

import com.internhub.model.StudentProfile;
import com.internhub.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for StudentProfile entity. Follows ISP - focused interface for
 * student profile operations.
 */
@Repository
public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {

    /**
     * Find student profile by user.
     */
    Optional<StudentProfile> findByUser(User user);

    /**
     * Find student profile by user ID.
     */
    Optional<StudentProfile> findByUserId(Long userId);

    /**
     * Find student profile by student number.
     */
    Optional<StudentProfile> findByStudentNumber(String studentNumber);

    /**
     * Check if student number exists.
     */
    boolean existsByStudentNumber(String studentNumber);
}
