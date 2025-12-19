package com.internhub.dto;

import com.internhub.model.NotificationPreference.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferenceRequest {

    private NotificationType notificationType;
    private Boolean emailEnabled;
    private Boolean pushEnabled;
    private Boolean inAppEnabled;
}
