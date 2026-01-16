package com.internhub.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.internhub.dto.NotificationDTO;
import com.internhub.model.Notification;
import com.internhub.model.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for sending real-time push notifications via WebSocket. Handles both
 * in-app push notifications and notification broadcasting.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PushNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Send a real-time notification to a specific user via WebSocket.
     *
     * @param userId The ID of the user to receive the notification
     * @param notification The notification DTO to send
     */
    public void sendToUser(Long userId, NotificationDTO notification) {
        try {
            String destination = "/queue/notifications";
            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    destination,
                    notification
            );
            log.debug("Sent push notification to user {}: {}", userId, notification.getTitle());
        } catch (Exception e) {
            log.error("Failed to send push notification to user {}: {}", userId, e.getMessage());
        }
    }

    /**
     * Send a real-time notification to a user from a Notification entity.
     *
     * @param user The user to receive the notification
     * @param notification The notification entity
     */
    public void sendToUser(User user, Notification notification) {
        NotificationDTO dto = new NotificationDTO(notification);
        sendToUser(user.getId(), dto);
    }

    /**
     * Send a notification count update to a specific user.
     *
     * @param userId The ID of the user
     * @param count The new unread count
     */
    public void sendUnreadCountUpdate(Long userId, long count) {
        try {
            Map<String, Object> countUpdate = new HashMap<>();
            countUpdate.put("unreadCount", count);
            countUpdate.put("timestamp", LocalDateTime.now().toString());

            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/notification-count",
                    countUpdate
            );
            log.debug("Sent unread count update to user {}: {}", userId, count);
        } catch (Exception e) {
            log.error("Failed to send unread count update to user {}: {}", userId, e.getMessage());
        }
    }

    /**
     * Broadcast a system notification to all connected users.
     *
     * @param title Notification title
     * @param message Notification message
     * @param type Notification type (INFO, WARNING, SUCCESS, ERROR)
     */
    public void broadcastSystemNotification(String title, String message, String type) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("title", title);
            notification.put("message", message);
            notification.put("type", type);
            notification.put("timestamp", LocalDateTime.now().toString());
            notification.put("isSystem", true);

            messagingTemplate.convertAndSend("/topic/system-notifications", notification);
            log.info("Broadcast system notification: {}", title);
        } catch (Exception e) {
            log.error("Failed to broadcast system notification: {}", e.getMessage());
        }
    }

    /**
     * Send a toast notification to a user (shorter, less intrusive).
     *
     * @param userId The ID of the user
     * @param title Toast title
     * @param message Toast message
     * @param type Toast type (success, info, warning, error)
     * @param duration Duration in milliseconds (default: 5000)
     */
    public void sendToast(Long userId, String title, String message, String type, int duration) {
        try {
            Map<String, Object> toast = new HashMap<>();
            toast.put("title", title);
            toast.put("message", message);
            toast.put("type", type);
            toast.put("duration", duration);
            toast.put("timestamp", LocalDateTime.now().toString());

            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/toasts",
                    toast
            );
            log.debug("Sent toast to user {}: {}", userId, title);
        } catch (Exception e) {
            log.error("Failed to send toast to user {}: {}", userId, e.getMessage());
        }
    }

    /**
     * Send a toast with default duration (5 seconds).
     */
    public void sendToast(Long userId, String title, String message, String type) {
        sendToast(userId, title, message, type, 5000);
    }
}
