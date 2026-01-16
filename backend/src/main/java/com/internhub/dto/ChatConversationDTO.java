package com.internhub.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatConversationDTO {

    private Long id;
    private Long studentId;
    private String studentName;
    private Long instructorId;
    private String instructorName;
    private Long internshipId;
    private String internshipTitle;
    private LocalDateTime lastMessageAt;
    private Integer unreadCount;
    private ChatMessageDTO lastMessage;
    private Boolean isActive;

    // For display - the other participant's info
    private Long otherUserId;
    private String otherUserName;
    private String otherUserRole;
}
