package com.internhub.repository;

import com.internhub.model.ChatConversation;
import com.internhub.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatConversationRepository extends JpaRepository<ChatConversation, Long> {

    /**
     * Find conversation between two users for a specific internship
     */
    @Query("SELECT c FROM ChatConversation c WHERE "
            + "((c.student.id = :userId1 AND c.instructor.id = :userId2) OR "
            + "(c.student.id = :userId2 AND c.instructor.id = :userId1)) "
            + "AND c.internship.id = :internshipId")
    Optional<ChatConversation> findByParticipantsAndInternship(
            @Param("userId1") Long userId1,
            @Param("userId2") Long userId2,
            @Param("internshipId") Long internshipId);

    /**
     * Find all conversations for a user
     */
    @Query("SELECT c FROM ChatConversation c WHERE "
            + "(c.student.id = :userId OR c.instructor.id = :userId) "
            + "AND c.isActive = true "
            + "ORDER BY c.lastMessageAt DESC NULLS LAST")
    List<ChatConversation> findAllByUserId(@Param("userId") Long userId);

    /**
     * Find conversations with unread messages for a user
     */
    @Query("SELECT c FROM ChatConversation c WHERE "
            + "(c.student.id = :userId AND c.studentUnreadCount > 0) OR "
            + "(c.instructor.id = :userId AND c.instructorUnreadCount > 0)")
    List<ChatConversation> findConversationsWithUnreadMessages(@Param("userId") Long userId);

    /**
     * Count total unread messages for a user
     */
    @Query("SELECT COALESCE(SUM(CASE WHEN c.student.id = :userId THEN c.studentUnreadCount "
            + "ELSE c.instructorUnreadCount END), 0) FROM ChatConversation c "
            + "WHERE c.student.id = :userId OR c.instructor.id = :userId")
    Integer countTotalUnreadMessages(@Param("userId") Long userId);

    /**
     * Find all conversations for a specific internship
     */
    List<ChatConversation> findByInternshipId(Long internshipId);

    /**
     * Reset unread count for a user in a conversation
     */
    @Modifying
    @Query("UPDATE ChatConversation c SET c.studentUnreadCount = 0 "
            + "WHERE c.id = :conversationId AND c.student.id = :userId")
    void resetStudentUnreadCount(@Param("conversationId") Long conversationId,
            @Param("userId") Long userId);

    @Modifying
    @Query("UPDATE ChatConversation c SET c.instructorUnreadCount = 0 "
            + "WHERE c.id = :conversationId AND c.instructor.id = :userId")
    void resetInstructorUnreadCount(@Param("conversationId") Long conversationId,
            @Param("userId") Long userId);

    /**
     * Search conversations by participant name
     */
    @Query("SELECT c FROM ChatConversation c WHERE "
            + "(c.student.id = :userId AND (LOWER(c.instructor.firstName) LIKE LOWER(CONCAT('%', :search, '%')) "
            + "OR LOWER(c.instructor.lastName) LIKE LOWER(CONCAT('%', :search, '%')))) "
            + "OR (c.instructor.id = :userId AND (LOWER(c.student.firstName) LIKE LOWER(CONCAT('%', :search, '%')) "
            + "OR LOWER(c.student.lastName) LIKE LOWER(CONCAT('%', :search, '%'))))")
    List<ChatConversation> searchByParticipantName(@Param("userId") Long userId,
            @Param("search") String search);
}
