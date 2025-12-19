package com.internhub.service.impl;

import com.internhub.dto.NotificationPreferenceRequest;
import com.internhub.dto.NotificationPreferenceResponse;
import com.internhub.exception.ResourceNotFoundException;
import com.internhub.model.NotificationPreference;
import com.internhub.model.NotificationPreference.NotificationType;
import com.internhub.model.User;
import com.internhub.repository.NotificationPreferenceRepository;
import com.internhub.repository.UserRepository;
import com.internhub.service.NotificationPreferenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationPreferenceServiceImpl implements NotificationPreferenceService {

    private final NotificationPreferenceRepository preferenceRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<NotificationPreferenceResponse> getUserPreferences(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        List<NotificationPreference> preferences = preferenceRepository.findByUserId(userId);

        // If no preferences exist, create defaults
        if (preferences.isEmpty()) {
            preferences = createDefaultPreferences(user);
        }

        return preferences.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationPreferenceResponse getPreference(Long userId, NotificationType notificationType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        NotificationPreference preference = preferenceRepository
                .findByUserIdAndNotificationType(userId, notificationType)
                .orElseGet(() -> createDefaultPreference(user, notificationType));

        return mapToResponse(preference);
    }

    @Override
    @Transactional
    public NotificationPreferenceResponse createOrUpdatePreference(Long userId, NotificationPreferenceRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        NotificationPreference preference = preferenceRepository
                .findByUserIdAndNotificationType(userId, request.getNotificationType())
                .orElse(new NotificationPreference());

        preference.setUser(user);
        preference.setNotificationType(request.getNotificationType());
        preference.setEmailEnabled(request.getEmailEnabled() != null ? request.getEmailEnabled() : true);
        preference.setPushEnabled(request.getPushEnabled() != null ? request.getPushEnabled() : true);
        preference.setInAppEnabled(request.getInAppEnabled() != null ? request.getInAppEnabled() : true);

        preference = preferenceRepository.save(preference);
        log.info("Updated notification preference for user {} and type {}", userId, request.getNotificationType());

        return mapToResponse(preference);
    }

    @Override
    @Transactional
    public void resetToDefaults(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Delete existing preferences
        List<NotificationPreference> existingPreferences = preferenceRepository.findByUserId(userId);
        preferenceRepository.deleteAll(existingPreferences);

        // Create default preferences
        createDefaultPreferences(user);
        log.info("Reset notification preferences to defaults for user {}", userId);
    }

    @Override
    @Transactional
    public void deletePreference(Long userId, NotificationType notificationType) {
        preferenceRepository.deleteByUserIdAndNotificationType(userId, notificationType);
        log.info("Deleted notification preference for user {} and type {}", userId, notificationType);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isChannelEnabled(Long userId, NotificationType notificationType, String channel) {
        NotificationPreference preference = preferenceRepository
                .findByUserIdAndNotificationType(userId, notificationType)
                .orElse(null);

        if (preference == null) {
            return true; // Default to enabled if no preference exists
        }

        return switch (channel.toLowerCase()) {
            case "email" ->
                preference.getEmailEnabled();
            case "push" ->
                preference.getPushEnabled();
            case "in_app" ->
                preference.getInAppEnabled();
            default ->
                false;
        };
    }

    private List<NotificationPreference> createDefaultPreferences(User user) {
        List<NotificationPreference> defaults = List.of(
                createPreference(user, NotificationType.INTERNSHIP_STATUS_CHANGE, true, true, true),
                createPreference(user, NotificationType.INTERNSHIP_ASSIGNED, true, true, true),
                createPreference(user, NotificationType.INTERNSHIP_VALIDATED, true, true, true),
                createPreference(user, NotificationType.INTERNSHIP_REJECTED, true, true, true),
                createPreference(user, NotificationType.NEW_COMMENT, true, true, true),
                createPreference(user, NotificationType.DEADLINE_REMINDER, true, true, true),
                createPreference(user, NotificationType.REPORT_UPLOADED, true, true, true),
                createPreference(user, NotificationType.SYSTEM_ANNOUNCEMENT, true, true, true)
        );

        return preferenceRepository.saveAll(defaults);
    }

    private NotificationPreference createDefaultPreference(User user, NotificationType notificationType) {
        NotificationPreference preference = createPreference(user, notificationType, true, true, true);
        return preferenceRepository.save(preference);
    }

    private NotificationPreference createPreference(User user, NotificationType type,
            boolean email, boolean push, boolean inApp) {
        NotificationPreference preference = new NotificationPreference();
        preference.setUser(user);
        preference.setNotificationType(type);
        preference.setEmailEnabled(email);
        preference.setPushEnabled(push);
        preference.setInAppEnabled(inApp);
        return preference;
    }

    private NotificationPreferenceResponse mapToResponse(NotificationPreference preference) {
        NotificationPreferenceResponse response = new NotificationPreferenceResponse();
        response.setId(preference.getId());
        response.setNotificationType(preference.getNotificationType());
        response.setEmailEnabled(preference.getEmailEnabled());
        response.setPushEnabled(preference.getPushEnabled());
        response.setInAppEnabled(preference.getInAppEnabled());
        response.setCreatedAt(preference.getCreatedAt());
        response.setUpdatedAt(preference.getUpdatedAt());
        return response;
    }
}
