package com.internhub.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.internhub.model.NotificationPreference;
import com.internhub.model.NotificationPreference.NotificationType;

@Repository
public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Long> {

    List<NotificationPreference> findByUserId(Long userId);

    Optional<NotificationPreference> findByUserIdAndNotificationType(Long userId, NotificationType notificationType);

    void deleteByUserIdAndNotificationType(Long userId, NotificationType notificationType);
}
