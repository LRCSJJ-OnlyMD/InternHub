package com.internhub.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Represents a single message in a chat conversation. Supports text messages
 * and file attachments.
 */
@Entity
@Table(name = "chat_messages", indexes = {
    @Index(name = "idx_chat_messages_conversation", columnList = "conversation_id"),
    @Index(name = "idx_chat_messages_sender", columnList = "sender_id"),
    @Index(name = "idx_chat_messages_sent_at", columnList = "sent_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private ChatConversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    private MessageType messageType = MessageType.TEXT;

    // File attachment fields (for documents shared in chat)
    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_url")
    private String fileUrl;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "file_type")
    private String fileType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MessageStatus status = MessageStatus.SENT;

    @Column(name = "sent_at", nullable = false, updatable = false)
    private LocalDateTime sentAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "edited_at")
    private LocalDateTime editedAt;

    @Column(name = "is_edited", nullable = false)
    private Boolean isEdited = false;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    // Reply to another message
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_to_id")
    private ChatMessage replyTo;

    @PrePersist
    protected void onCreate() {
        sentAt = LocalDateTime.now();
    }

    /**
     * Mark message as delivered
     */
    public void markAsDelivered() {
        if (this.status == MessageStatus.SENT) {
            this.status = MessageStatus.DELIVERED;
            this.deliveredAt = LocalDateTime.now();
        }
    }

    /**
     * Mark message as read
     */
    public void markAsRead() {
        if (this.status != MessageStatus.READ) {
            this.status = MessageStatus.READ;
            this.readAt = LocalDateTime.now();
            if (this.deliveredAt == null) {
                this.deliveredAt = LocalDateTime.now();
            }
        }
    }

    /**
     * Edit message content
     */
    public void editContent(String newContent) {
        this.content = newContent;
        this.isEdited = true;
        this.editedAt = LocalDateTime.now();
    }

    /**
     * Soft delete message
     */
    public void softDelete() {
        this.isDeleted = true;
        this.content = "This message was deleted";
    }

    public enum MessageType {
        TEXT,
        FILE,
        IMAGE,
        SYSTEM  // For system messages like "User joined", etc.
    }

    public enum MessageStatus {
        SENT, // Message sent to server
        DELIVERED, // Message delivered to recipient's device
        READ        // Message read by recipient
    }
}
