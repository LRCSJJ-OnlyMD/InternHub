package com.internhub.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.internhub.dto.NotificationDTO;
import com.internhub.model.Internship;
import com.internhub.model.Notification;
import com.internhub.model.NotificationPreference.NotificationType;
import com.internhub.model.User;
import com.internhub.repository.NotificationRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceService preferenceService;
    private final EmailService emailService;

    public NotificationService(NotificationRepository notificationRepository,
            NotificationPreferenceService preferenceService,
            EmailService emailService) {
        this.notificationRepository = notificationRepository;
        this.preferenceService = preferenceService;
        this.emailService = emailService;
    }

    @Transactional
    public Notification createNotification(User user, String type, String title, String message) {
        return createNotification(user, type, title, message, null, null);
    }

    @Transactional
    public Notification createNotification(User user, String type, String title, String message,
            String entityType, Long entityId) {
        // Map notification type string to enum
        NotificationType notificationType = mapToNotificationType(type);

        // Check if in-app notifications are enabled
        if (preferenceService.isChannelEnabled(user.getId(), notificationType, "in_app")) {
            Notification notification = new Notification(user, type, title, message, entityType, entityId);
            notification = notificationRepository.save(notification);
            log.debug("Created in-app notification for user {}: {}", user.getId(), title);
        }

        // Send email notification if enabled
        if (preferenceService.isChannelEnabled(user.getId(), notificationType, "email")) {
            try {
                sendEmailNotification(user, title, message);
                log.debug("Sent email notification to user {}: {}", user.getId(), title);
            } catch (Exception e) {
                log.error("Failed to send email notification to user {}: {}", user.getId(), e.getMessage());
            }
        }

        // TODO: Send push notification if enabled
        // if (preferenceService.isChannelEnabled(user.getId(), notificationType, "push")) {
        //     sendPushNotification(user, title, message);
        // }
        return notificationRepository.findFirstByUserOrderByCreatedAtDesc(user)
                .orElse(new Notification(user, type, title, message));
    }

    private NotificationType mapToNotificationType(String type) {
        return switch (type) {
            case "INTERNSHIP_STATUS" ->
                NotificationType.INTERNSHIP_STATUS_CHANGE;
            case "CLAIM" ->
                NotificationType.INTERNSHIP_ASSIGNED;
            case "VALIDATE" ->
                NotificationType.INTERNSHIP_VALIDATED;
            case "REFUSE" ->
                NotificationType.INTERNSHIP_REJECTED;
            case "COMMENT_ADDED" ->
                NotificationType.NEW_COMMENT;
            case "DEADLINE" ->
                NotificationType.DEADLINE_REMINDER;
            case "REPORT_UPLOADED" ->
                NotificationType.REPORT_UPLOADED;
            case "ANNOUNCEMENT" ->
                NotificationType.SYSTEM_ANNOUNCEMENT;
            default ->
                NotificationType.SYSTEM_ANNOUNCEMENT;
        };
    }

    private void sendEmailNotification(User user, String title, String message) {
        String subject = "InternHub: " + title;
        String body = String.format(
                "Hello %s %s,\n\n%s\n\nBest regards,\nInternHub Team",
                user.getFirstName(), user.getLastName(), message
        );
        emailService.sendEmail(user.getEmail(), subject, body);
    }

    public Page<NotificationDTO> getUserNotifications(User user, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return notificationRepository.findByUserOrderByCreatedAtDesc(user, pageable)
                .map(NotificationDTO::new);
    }

    public List<NotificationDTO> getUnreadNotifications(User user) {
        return notificationRepository.findByUserAndReadFalseOrderByCreatedAtDesc(user)
                .stream()
                .map(NotificationDTO::new)
                .collect(Collectors.toList());
    }

    public long getUnreadCount(User user) {
        return notificationRepository.countByUserAndReadFalse(user);
    }

    @Transactional
    public void markAsRead(Long notificationId, User user) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            if (notification.getUser().getId().equals(user.getId())) {
                notification.setRead(true);
                notificationRepository.save(notification);
            }
        });
    }

    @Transactional
    public int markAllAsRead(User user) {
        return notificationRepository.markAllAsReadForUser(user);
    }

    @Transactional
    public void deleteNotification(Long notificationId, User user) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            if (notification.getUser().getId().equals(user.getId())) {
                notificationRepository.delete(notification);
            }
        });
    }

    // Helper methods for creating specific notifications
    @Transactional
    public void notifyInternshipStatusChange(Internship internship, String oldStatus, String newStatus) {
        String title = "Internship Status Updated";
        String message = String.format("Your internship '%s' status changed from %s to %s",
                internship.getTitle(), oldStatus, newStatus);
        createNotification(internship.getStudent(), "INTERNSHIP_STATUS", title, message,
                "INTERNSHIP", internship.getId());
    }

    @Transactional
    public void notifyInternshipClaimed(Internship internship) {
        String title = "Internship Claimed";
        String message = String.format("Instructor %s %s has claimed your internship '%s'",
                internship.getInstructor().getFirstName(),
                internship.getInstructor().getLastName(),
                internship.getTitle());
        createNotification(internship.getStudent(), "CLAIM", title, message,
                "INTERNSHIP", internship.getId());
    }

    @Transactional
    public void notifyInternshipValidated(Internship internship, String comment) {
        String title = "Internship Validated";
        String message = String.format("Your internship '%s' has been validated", internship.getTitle());
        if (comment != null && !comment.isEmpty()) {
            message += ". Comment: " + comment;
        }
        createNotification(internship.getStudent(), "VALIDATE", title, message,
                "INTERNSHIP", internship.getId());
    }

    @Transactional
    public void notifyInternshipRefused(Internship internship, String reason) {
        String title = "Internship Refused";
        String message = String.format("Your internship '%s' has been refused. Reason: %s",
                internship.getTitle(), reason);
        createNotification(internship.getStudent(), "REFUSE", title, message,
                "INTERNSHIP", internship.getId());
    }

    @Transactional
    public void notifyInternshipSubmitted(Internship internship) {
        if (internship.getInstructor() != null) {
            String title = "New Internship Submitted";
            String message = String.format("Student %s %s has submitted internship '%s' for validation",
                    internship.getStudent().getFirstName(),
                    internship.getStudent().getLastName(),
                    internship.getTitle());
            createNotification(internship.getInstructor(), "INTERNSHIP_STATUS", title, message,
                    "INTERNSHIP", internship.getId());
        }
    }

    @Transactional
    public void notifyNewComment(User recipient, User commenter, Internship internship, Long commentId, boolean isReply) {
        String title = isReply ? "New Reply" : "New Comment";
        String message = String.format("%s %s %s on internship '%s'",
                commenter.getFirstName(),
                commenter.getLastName(),
                isReply ? "replied to your comment" : "commented",
                internship.getTitle());
        createNotification(recipient, "COMMENT_ADDED", title, message, "INTERNSHIP", internship.getId());
    }
}
