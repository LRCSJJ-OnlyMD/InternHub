package com.internhub.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.internhub.model.Comment;
import com.internhub.model.Internship;

/**
 * Repository for Comment entity operations.
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * Find all top-level comments (no parent) for an internship. Ordered by
     * creation date descending (newest first).
     */
    @Query("SELECT c FROM Comment c WHERE c.internship.id = :internshipId AND c.parentComment IS NULL ORDER BY c.createdAt DESC")
    List<Comment> findTopLevelCommentsByInternshipId(@Param("internshipId") Long internshipId);

    /**
     * Find all replies to a specific comment. Ordered by creation date
     * ascending (oldest first for conversation flow).
     */
    @Query("SELECT c FROM Comment c WHERE c.parentComment.id = :parentCommentId ORDER BY c.createdAt ASC")
    List<Comment> findRepliesByParentCommentId(@Param("parentCommentId") Long parentCommentId);

    /**
     * Count total comments (including replies) for an internship.
     */
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.internship.id = :internshipId")
    long countByInternshipId(@Param("internshipId") Long internshipId);

    /**
     * Find all comments by a specific user.
     */
    @Query("SELECT c FROM Comment c WHERE c.user.id = :userId ORDER BY c.createdAt DESC")
    List<Comment> findByUserId(@Param("userId") Long userId);

    /**
     * Delete all comments for an internship (cascade).
     */
    void deleteByInternship(Internship internship);

    /**
     * Count replies for a specific comment.
     */
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.parentComment.id = :parentCommentId")
    long countRepliesByParentCommentId(@Param("parentCommentId") Long parentCommentId);
}
