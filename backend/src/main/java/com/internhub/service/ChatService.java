package com.internhub.service;

import com.internhub.dto.*;
import com.internhub.exception.ResourceNotFoundException;
import com.internhub.exception.UnauthorizedException;
import com.internhub.model.*;
import com.internhub.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing chat conversations and messages. Provides WhatsApp-like
 * messaging functionality between students and instructors.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ChatConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final InternshipRepository internshipRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationService notificationService;

    /**
     * Get all conversations for a user
     */
    @Transactional(readOnly = true)
    public List<ChatConversationDTO> getUserConversations(Long userId) {
        List<ChatConversation> conversations = conversationRepository.findAllByUserId(userId);
        return conversations.stream()
                .map(c -> toConversationDTO(c, userId))
                .collect(Collectors.toList());
    }

    /**
     * Get or create a conversation between two users for an internship
     */
    @Transactional
    public ChatConversationDTO getOrCreateConversation(Long userId, StartConversationRequest request) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        User otherUser = userRepository.findById(request.getOtherUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Other user not found"));

        Internship internship = internshipRepository.findById(request.getInternshipId())
                .orElseThrow(() -> new ResourceNotFoundException("Internship not found"));

        // Validate that one is student and other is instructor
        validateConversationParticipants(currentUser, otherUser, internship);

        // Determine who is student and who is instructor
        User student = currentUser.getRole() == Role.STUDENT ? currentUser : otherUser;
        User instructor = currentUser.getRole() == Role.INSTRUCTOR ? currentUser : otherUser;

        // Find existing or create new conversation
        ChatConversation conversation = conversationRepository
                .findByParticipantsAndInternship(userId, request.getOtherUserId(), request.getInternshipId())
                .orElseGet(() -> {
                    ChatConversation newConversation = new ChatConversation();
                    newConversation.setStudent(student);
                    newConversation.setInstructor(instructor);
                    newConversation.setInternship(internship);
                    return conversationRepository.save(newConversation);
                });

        // Send initial message if provided
        if (request.getInitialMessage() != null && !request.getInitialMessage().isBlank()) {
            SendMessageRequest messageRequest = new SendMessageRequest();
            messageRequest.setConversationId(conversation.getId());
            messageRequest.setContent(request.getInitialMessage());
            sendMessage(userId, messageRequest);
        }

        return toConversationDTO(conversation, userId);
    }

    /**
     * Get messages for a conversation with pagination
     */
    @Transactional(readOnly = true)
    public Page<ChatMessageDTO> getConversationMessages(Long userId, Long conversationId,
            int page, int size) {
        ChatConversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        // Verify user is participant
        if (!conversation.isParticipant(userId)) {
            throw new UnauthorizedException("You are not a participant in this conversation");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<ChatMessage> messages = messageRepository
                .findByConversationIdOrderBySentAtDesc(conversationId, pageable);

        return messages.map(this::toMessageDTO);
    }

    /**
     * Send a new message
     */
    @Transactional
    public ChatMessageDTO sendMessage(Long senderId, SendMessageRequest request) {
        ChatConversation conversation = conversationRepository.findById(request.getConversationId())
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        // Verify sender is participant
        if (!conversation.isParticipant(senderId)) {
            throw new UnauthorizedException("You are not a participant in this conversation");
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Create message
        ChatMessage message = new ChatMessage();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setContent(request.getContent());
        message.setMessageType(request.getMessageType() != null
                ? request.getMessageType() : ChatMessage.MessageType.TEXT);

        // Handle file attachment
        if (request.getFileName() != null) {
            message.setFileName(request.getFileName());
            message.setFileUrl(request.getFileUrl());
            message.setFileSize(request.getFileSize());
            message.setFileType(request.getFileType());
        }

        // Handle reply
        if (request.getReplyToId() != null) {
            ChatMessage replyTo = messageRepository.findById(request.getReplyToId())
                    .orElse(null);
            message.setReplyTo(replyTo);
        }

        message = messageRepository.save(message);

        // Update conversation
        conversation.setLastMessageAt(LocalDateTime.now());
        conversation.incrementUnreadCount(senderId);
        conversationRepository.save(conversation);

        ChatMessageDTO messageDTO = toMessageDTO(message);

        // Send real-time notification via WebSocket
        Long recipientId = conversation.getOtherParticipant(senderId).getId();
        messagingTemplate.convertAndSendToUser(
                recipientId.toString(),
                "/queue/messages",
                messageDTO
        );

        // Send typing stopped indicator
        messagingTemplate.convertAndSendToUser(
                recipientId.toString(),
                "/queue/typing",
                new TypingIndicator(conversation.getId(), senderId, false)
        );

        // Create notification
        notificationService.createChatNotification(
                recipientId,
                sender.getFirstName() + " " + sender.getLastName(),
                conversation.getId()
        );

        log.info("Message sent in conversation {} by user {}", conversation.getId(), senderId);
        return messageDTO;
    }

    /**
     * Mark messages as read
     */
    @Transactional
    public void markMessagesAsRead(Long userId, Long conversationId) {
        ChatConversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        if (!conversation.isParticipant(userId)) {
            throw new UnauthorizedException("You are not a participant in this conversation");
        }

        // Mark messages as read
        int updated = messageRepository.markMessagesAsRead(conversationId, userId, LocalDateTime.now());

        // Reset unread count
        conversation.resetUnreadCount(userId);
        conversationRepository.save(conversation);

        // Notify sender that messages were read
        Long otherUserId = conversation.getOtherParticipant(userId).getId();
        messagingTemplate.convertAndSendToUser(
                otherUserId.toString(),
                "/queue/read-receipt",
                new ReadReceipt(conversationId, userId, LocalDateTime.now())
        );

        log.debug("Marked {} messages as read in conversation {}", updated, conversationId);
    }

    /**
     * Send typing indicator
     */
    public void sendTypingIndicator(Long userId, Long conversationId, boolean isTyping) {
        ChatConversation conversation = conversationRepository.findById(conversationId)
                .orElse(null);

        if (conversation != null && conversation.isParticipant(userId)) {
            Long recipientId = conversation.getOtherParticipant(userId).getId();
            messagingTemplate.convertAndSendToUser(
                    recipientId.toString(),
                    "/queue/typing",
                    new TypingIndicator(conversationId, userId, isTyping)
            );
        }
    }

    /**
     * Get total unread message count for a user
     */
    @Transactional(readOnly = true)
    public int getTotalUnreadCount(Long userId) {
        return conversationRepository.countTotalUnreadMessages(userId);
    }

    /**
     * Search messages in a conversation
     */
    @Transactional(readOnly = true)
    public List<ChatMessageDTO> searchMessages(Long userId, Long conversationId, String query) {
        ChatConversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        if (!conversation.isParticipant(userId)) {
            throw new UnauthorizedException("You are not a participant in this conversation");
        }

        return messageRepository.searchMessages(conversationId, query)
                .stream()
                .map(this::toMessageDTO)
                .collect(Collectors.toList());
    }

    /**
     * Delete a message (soft delete)
     */
    @Transactional
    public void deleteMessage(Long userId, Long messageId) {
        ChatMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));

        if (!message.getSender().getId().equals(userId)) {
            throw new UnauthorizedException("You can only delete your own messages");
        }

        message.softDelete();
        messageRepository.save(message);

        // Notify other participant
        Long otherUserId = message.getConversation().getOtherParticipant(userId).getId();
        messagingTemplate.convertAndSendToUser(
                otherUserId.toString(),
                "/queue/message-deleted",
                messageId
        );
    }

    /**
     * Edit a message
     */
    @Transactional
    public ChatMessageDTO editMessage(Long userId, Long messageId, String newContent) {
        ChatMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));

        if (!message.getSender().getId().equals(userId)) {
            throw new UnauthorizedException("You can only edit your own messages");
        }

        message.editContent(newContent);
        message = messageRepository.save(message);

        ChatMessageDTO dto = toMessageDTO(message);

        // Notify other participant
        Long otherUserId = message.getConversation().getOtherParticipant(userId).getId();
        messagingTemplate.convertAndSendToUser(
                otherUserId.toString(),
                "/queue/message-edited",
                dto
        );

        return dto;
    }

    // Validation helper
    private void validateConversationParticipants(User user1, User user2, Internship internship) {
        boolean hasStudent = user1.getRole() == Role.STUDENT || user2.getRole() == Role.STUDENT;
        boolean hasInstructor = user1.getRole() == Role.INSTRUCTOR || user2.getRole() == Role.INSTRUCTOR;

        if (!hasStudent || !hasInstructor) {
            throw new IllegalArgumentException("Conversation must be between a student and an instructor");
        }

        // Verify the student owns the internship or instructor is assigned
        User student = user1.getRole() == Role.STUDENT ? user1 : user2;
        User instructor = user1.getRole() == Role.INSTRUCTOR ? user1 : user2;

        if (!internship.getStudent().getId().equals(student.getId())) {
            throw new IllegalArgumentException("Student is not the owner of this internship");
        }

        if (internship.getInstructor() != null
                && !internship.getInstructor().getId().equals(instructor.getId())) {
            throw new IllegalArgumentException("Instructor is not assigned to this internship");
        }
    }

    // DTO Mappers
    private ChatConversationDTO toConversationDTO(ChatConversation conversation, Long currentUserId) {
        User otherUser = conversation.getOtherParticipant(currentUserId);
        ChatMessage lastMessage = messageRepository.findLastMessage(conversation.getId());

        return ChatConversationDTO.builder()
                .id(conversation.getId())
                .studentId(conversation.getStudent().getId())
                .studentName(conversation.getStudent().getFirstName() + " "
                        + conversation.getStudent().getLastName())
                .instructorId(conversation.getInstructor().getId())
                .instructorName(conversation.getInstructor().getFirstName() + " "
                        + conversation.getInstructor().getLastName())
                .internshipId(conversation.getInternship() != null
                        ? conversation.getInternship().getId() : null)
                .internshipTitle(conversation.getInternship() != null
                        ? conversation.getInternship().getTitle() : null)
                .lastMessageAt(conversation.getLastMessageAt())
                .unreadCount(conversation.getUnreadCountForUser(currentUserId))
                .lastMessage(lastMessage != null ? toMessageDTO(lastMessage) : null)
                .isActive(conversation.getIsActive())
                .otherUserId(otherUser.getId())
                .otherUserName(otherUser.getFirstName() + " " + otherUser.getLastName())
                .otherUserRole(otherUser.getRole().name())
                .build();
    }

    private ChatMessageDTO toMessageDTO(ChatMessage message) {
        return ChatMessageDTO.builder()
                .id(message.getId())
                .conversationId(message.getConversation().getId())
                .senderId(message.getSender().getId())
                .senderName(message.getSender().getFirstName() + " "
                        + message.getSender().getLastName())
                .senderRole(message.getSender().getRole().name())
                .content(message.getContent())
                .messageType(message.getMessageType())
                .fileName(message.getFileName())
                .fileUrl(message.getFileUrl())
                .fileSize(message.getFileSize())
                .fileType(message.getFileType())
                .status(message.getStatus())
                .sentAt(message.getSentAt())
                .deliveredAt(message.getDeliveredAt())
                .readAt(message.getReadAt())
                .isEdited(message.getIsEdited())
                .isDeleted(message.getIsDeleted())
                .replyToId(message.getReplyTo() != null ? message.getReplyTo().getId() : null)
                .replyToContent(message.getReplyTo() != null
                        ? truncateContent(message.getReplyTo().getContent(), 50) : null)
                .build();
    }

    private String truncateContent(String content, int maxLength) {
        if (content == null) {
            return null;
        }
        if (content.length() <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength) + "...";
    }

    // Inner classes for WebSocket messages
    public record TypingIndicator(Long conversationId, Long userId, boolean isTyping) {

    }

    public record ReadReceipt(Long conversationId, Long userId, LocalDateTime readAt) {

    }
}
