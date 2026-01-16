package com.internhub.dto;

import com.internhub.model.ChatMessage.MessageStatus;
import com.internhub.model.ChatMessage.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {

    private Long id;
    private Long conversationId;
    private Long senderId;
    private String senderName;
    private String senderRole;
    private String content;
    private MessageType messageType;
    private String fileName;
    private String fileUrl;
    private Long fileSize;
    private String fileType;
    private MessageStatus status;
    private LocalDateTime sentAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime readAt;
    private Boolean isEdited;
    private Boolean isDeleted;
    private Long replyToId;
    private String replyToContent;  // Preview of replied message
}
