package com.internhub.dto;

import com.internhub.model.ChatMessage.MessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageRequest {

    @NotNull(message = "Conversation ID is required")
    private Long conversationId;

    private String content;

    private MessageType messageType = MessageType.TEXT;

    // For file messages
    private String fileName;
    private String fileUrl;
    private Long fileSize;
    private String fileType;

    // For replies
    private Long replyToId;
}
