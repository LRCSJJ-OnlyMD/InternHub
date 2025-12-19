package com.internhub.service;

import java.util.List;

import com.internhub.dto.NotificationPreferenceRequest;
import com.internhub.dto.NotificationPreferenceResponse;
import com.internhub.model.NotificationPreference.NotificationType;

public interface NotificationPreferenceService {

    List<NotificationPreferenceResponse> getUserPreferences(Long userId);

    NotificationPreferenceResponse getPreference(Long userId, NotificationType notificationType);

    NotificationPreferenceResponse createOrUpdatePreference(Long userId, NotificationPreferenceRequest request);

    void resetToDefaults(Long userId);

    void deletePreference(Long userId, NotificationType notificationType);

    boolean isChannelEnabled(Long userId, NotificationType notificationType, String channel);
}
