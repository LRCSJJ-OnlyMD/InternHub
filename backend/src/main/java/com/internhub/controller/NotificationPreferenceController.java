package com.internhub.controller;

import com.internhub.dto.NotificationPreferenceRequest;
import com.internhub.dto.NotificationPreferenceResponse;
import com.internhub.model.NotificationPreference.NotificationType;
import com.internhub.service.NotificationPreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notification-preferences")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NotificationPreferenceController {

    private final NotificationPreferenceService preferenceService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<NotificationPreferenceResponse>> getUserPreferences(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        List<NotificationPreferenceResponse> preferences = preferenceService.getUserPreferences(userId);
        return ResponseEntity.ok(preferences);
    }

    @GetMapping("/{notificationType}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<NotificationPreferenceResponse> getPreference(
            @PathVariable NotificationType notificationType,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        NotificationPreferenceResponse preference = preferenceService.getPreference(userId, notificationType);
        return ResponseEntity.ok(preference);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<NotificationPreferenceResponse> createOrUpdatePreference(
            @RequestBody NotificationPreferenceRequest request,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        NotificationPreferenceResponse preference = preferenceService.createOrUpdatePreference(userId, request);
        return ResponseEntity.ok(preference);
    }

    @PostMapping("/reset")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> resetToDefaults(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        preferenceService.resetToDefaults(userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{notificationType}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deletePreference(
            @PathVariable NotificationType notificationType,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        preferenceService.deletePreference(userId, notificationType);
        return ResponseEntity.noContent().build();
    }
}
