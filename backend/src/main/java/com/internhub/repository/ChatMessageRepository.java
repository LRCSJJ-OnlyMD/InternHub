package com.internhub.repository;

import com.internhub.model.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * Get messages for a conversation with pagination (newest first)
     */
    @Query("SELECT m FROM ChatMessage m WHERE m.conversation.id = :conversationId "
            + "AND m.isDeleted = false ORDER BY m.sentAt DESC")
    Page<ChatMessage> findByConversationIdOrderBySentAtDesc(
            @Param("conversationId") Long conversationId, Pageable pageable);

    /**
     * Get messages for a conversation (oldest first - for display)
     */
    @Query("SELECT m FROM ChatMessage m WHERE m.conversation.id = :conversationId "
            + "AND m.isDeleted = false ORDER BY m.sentAt ASC")
    List<ChatMessage> findByConversationIdOrderBySentAtAsc(@Param("conversationId") Long conversationId);

    /**
     * Get recent messages (for infinite scroll loading)
     */
    @Query("SELECT m FROM ChatMessage m WHERE m.conversation.id = :conversationId "
            + "AND m.sentAt < :beforeTime AND m.isDeleted = false ORDER BY m.sentAt DESC")
    Page<ChatMessage> findMessagesBeforeTime(
            @Param("conversationId") Long conversationId,
            @Param("beforeTime") LocalDateTime beforeTime,
            Pageable pageable);

    /**
     * Get unread messages for a user in a conversation
     */
    @Query("SELECT m FROM ChatMessage m WHERE m.conversation.id = :conversationId "
            + "AND m.sender.id != :userId AND m.status != 'READ' AND m.isDeleted = false")
    List<ChatMessage> findUnreadMessages(
            @Param("conversationId") Long conversationId,
            @Param("userId") Long userId);

    /**
     * Count unread messages for a user in a conversation
     */
    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.conversation.id = :conversationId "
            + "AND m.sender.id != :userId AND m.status != 'READ' AND m.isDeleted = false")
    Long countUnreadMessages(
            @Param("conversationId") Long conversationId,
            @Param("userId") Long userId);

    /**
     * Mark messages as delivered
     */
    @Modifying
    @Query("UPDATE ChatMessage m SET m.status = 'DELIVERED', m.deliveredAt = :now "
            + "WHERE m.conversation.id = :conversationId AND m.sender.id != :userId "
            + "AND m.status = 'SENT'")
    int markMessagesAsDelivered(
            @Param("conversationId") Long conversationId,
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now);

    /**
     * Mark messages as read
     */
    @Modifying
    @Query("UPDATE ChatMessage m SET m.status = 'READ', m.readAt = :now, "
            + "m.deliveredAt = COALESCE(m.deliveredAt, :now) "
            + "WHERE m.conversation.id = :conversationId AND m.sender.id != :userId "
            + "AND m.status != 'READ'")
    int markMessagesAsRead(
            @Param("conversationId") Long conversationId,
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now);

    /**
     * Get the last message in a conversation
     */
    @Query("SELECT m FROM ChatMessage m WHERE m.conversation.id = :conversationId "
            + "AND m.isDeleted = false ORDER BY m.sentAt DESC LIMIT 1")
    ChatMessage findLastMessage(@Param("conversationId") Long conversationId);

    /**
     * Search messages in a conversation
     */
    @Query("SELECT m FROM ChatMessage m WHERE m.conversation.id = :conversationId "
            + "AND LOWER(m.content) LIKE LOWER(CONCAT('%', :query, '%')) "
            + "AND m.isDeleted = false ORDER BY m.sentAt DESC")
    List<ChatMessage> searchMessages(
            @Param("conversationId") Long conversationId,
            @Param("query") String query);

    /**
     * Get messages with attachments
     */
    @Query("SELECT m FROM ChatMessage m WHERE m.conversation.id = :conversationId "
            + "AND m.messageType IN ('FILE', 'IMAGE') AND m.isDeleted = false ORDER BY m.sentAt DESC")
    List<ChatMessage> findMessagesWithAttachments(@Param("conversationId") Long conversationId);

    /**
     * Delete old messages (for cleanup job)
     */
    @Modifying
    @Query("DELETE FROM ChatMessage m WHERE m.sentAt < :beforeDate")
    int deleteOldMessages(@Param("beforeDate") LocalDateTime beforeDate);
}
