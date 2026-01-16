package com.internhub.controller;

import com.internhub.dto.ChatConversationDTO;
import com.internhub.dto.ChatMessageDTO;
import com.internhub.dto.SendMessageRequest;
import com.internhub.dto.StartConversationRequest;
import com.internhub.model.User;
import com.internhub.repository.UserRepository;
import com.internhub.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * Controller for handling chat functionality via REST and WebSocket. Provides
 * WhatsApp-like messaging between students and instructors.
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;
    private final UserRepository userRepository;

    // ===================== REST Endpoints =====================
    /**
     * Get all conversations for the current user
     */
    @GetMapping("/conversations")
    public ResponseEntity<List<ChatConversationDTO>> getConversations(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = extractUserId(userDetails);
        List<ChatConversationDTO> conversations = chatService.getUserConversations(userId);
        return ResponseEntity.ok(conversations);
    }

    /**
     * Get or create a conversation with a specific user for an internship
     */
    @PostMapping("/conversations/start")
    public ResponseEntity<ChatConversationDTO> startConversation(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody StartConversationRequest request) {
        Long userId = extractUserId(userDetails);
        ChatConversationDTO conversation = chatService.getOrCreateConversation(userId, request);
        return ResponseEntity.ok(conversation);
    }

    /**
     * Get messages for a conversation with pagination
     */
    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<Page<ChatMessageDTO>> getMessages(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Long userId = extractUserId(userDetails);
        Page<ChatMessageDTO> messages = chatService.getConversationMessages(userId, conversationId, page, size);
        return ResponseEntity.ok(messages);
    }

    /**
     * Send a text message via REST
     */
    @PostMapping("/messages")
    public ResponseEntity<ChatMessageDTO> sendMessage(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody SendMessageRequest request) {
        Long userId = extractUserId(userDetails);
        ChatMessageDTO message = chatService.sendMessage(userId, request);
        return ResponseEntity.ok(message);
    }

    /**
     * Mark messages as read
     */
    @PostMapping("/conversations/{conversationId}/read")
    public ResponseEntity<Void> markAsRead(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long conversationId) {
        Long userId = extractUserId(userDetails);
        chatService.markMessagesAsRead(userId, conversationId);
        return ResponseEntity.ok().build();
    }

    /**
     * Delete a message (soft delete)
     */
    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long messageId) {
        Long userId = extractUserId(userDetails);
        chatService.deleteMessage(userId, messageId);
        return ResponseEntity.ok().build();
    }

    /**
     * Search messages in a conversation
     */
    @GetMapping("/conversations/{conversationId}/search")
    public ResponseEntity<List<ChatMessageDTO>> searchMessages(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long conversationId,
            @RequestParam String query) {
        Long userId = extractUserId(userDetails);
        List<ChatMessageDTO> messages = chatService.searchMessages(userId, conversationId, query);
        return ResponseEntity.ok(messages);
    }

    /**
     * Get total unread count for the user
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Integer>> getUnreadCount(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = extractUserId(userDetails);
        int count = chatService.getTotalUnreadCount(userId);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    // ===================== WebSocket Endpoints =====================
    /**
     * Handle sending a message via WebSocket Client sends to: /app/chat/send
     */
    @MessageMapping("/chat/send")
    public void handleSendMessage(
            @Payload SendMessageRequest request,
            SimpMessageHeaderAccessor headerAccessor) {
        Long userId = extractUserIdFromWebSocket(headerAccessor);
        if (userId != null) {
            chatService.sendMessage(userId, request);
        }
    }

    /**
     * Handle typing indicator via WebSocket Client sends to:
     * /app/chat/{conversationId}/typing
     */
    @MessageMapping("/chat/{conversationId}/typing")
    public void handleTyping(
            @DestinationVariable Long conversationId,
            @Payload Map<String, Boolean> payload,
            SimpMessageHeaderAccessor headerAccessor) {
        Long userId = extractUserIdFromWebSocket(headerAccessor);
        if (userId != null) {
            boolean isTyping = payload != null && Boolean.TRUE.equals(payload.get("isTyping"));
            chatService.sendTypingIndicator(userId, conversationId, isTyping);
        }
    }

    /**
     * Handle mark as read via WebSocket Client sends to:
     * /app/chat/{conversationId}/read
     */
    @MessageMapping("/chat/{conversationId}/read")
    public void handleMarkAsRead(
            @DestinationVariable Long conversationId,
            SimpMessageHeaderAccessor headerAccessor) {
        Long userId = extractUserIdFromWebSocket(headerAccessor);
        if (userId != null) {
            chatService.markMessagesAsRead(userId, conversationId);
        }
    }

    // ===================== Helper Methods =====================
    private Long extractUserId(UserDetails userDetails) {
        // Get user ID from principal - username is the email
        if (userDetails != null) {
            String email = userDetails.getUsername();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
            return user.getId();
        }
        throw new RuntimeException("Unable to extract user ID from authentication");
    }

    private Long extractUserIdFromWebSocket(SimpMessageHeaderAccessor headerAccessor) {
        // Get user ID from WebSocket session attributes
        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
        if (sessionAttributes != null) {
            Object userId = sessionAttributes.get("userId");
            if (userId != null) {
                return (Long) userId;
            }
        }
        log.warn("Unable to extract user ID from WebSocket session");
        return null;
    }
}
