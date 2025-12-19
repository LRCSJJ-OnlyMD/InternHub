package com.internhub.dto;

import com.internhub.model.NotificationPreference.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferenceResponse {

    private Long id;
    private NotificationType notificationType;
    private Boolean emailEnabled;
    private Boolean pushEnabled;
    private Boolean inAppEnabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
