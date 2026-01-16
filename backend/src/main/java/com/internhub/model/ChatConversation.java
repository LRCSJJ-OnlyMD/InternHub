package com.internhub.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a chat conversation between a student and an instructor. Each
 * conversation is linked to a specific internship for context.
 */
@Entity
@Table(name = "chat_conversations", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"student_id", "instructor_id", "internship_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatConversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id", nullable = false)
    private User instructor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "internship_id")
    private Internship internship;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    @Column(name = "student_unread_count", nullable = false)
    private Integer studentUnreadCount = 0;

    @Column(name = "instructor_unread_count", nullable = false)
    private Integer instructorUnreadCount = 0;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sentAt ASC")
    private List<ChatMessage> messages = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Get the other participant in the conversation
     */
    public User getOtherParticipant(Long userId) {
        if (student.getId().equals(userId)) {
            return instructor;
        }
        return student;
    }

    /**
     * Check if user is participant
     */
    public boolean isParticipant(Long userId) {
        return student.getId().equals(userId) || instructor.getId().equals(userId);
    }

    /**
     * Get unread count for a specific user
     */
    public Integer getUnreadCountForUser(Long userId) {
        if (student.getId().equals(userId)) {
            return studentUnreadCount;
        }
        return instructorUnreadCount;
    }

    /**
     * Increment unread count for recipient
     */
    public void incrementUnreadCount(Long senderId) {
        if (student.getId().equals(senderId)) {
            instructorUnreadCount++;
        } else {
            studentUnreadCount++;
        }
    }

    /**
     * Reset unread count when user reads messages
     */
    public void resetUnreadCount(Long userId) {
        if (student.getId().equals(userId)) {
            studentUnreadCount = 0;
        } else {
            instructorUnreadCount = 0;
        }
    }
}
