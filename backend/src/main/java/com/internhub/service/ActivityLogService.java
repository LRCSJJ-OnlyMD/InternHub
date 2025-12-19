package com.internhub.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.internhub.dto.ActivityLogDTO;
import com.internhub.model.ActivityLog;
import com.internhub.model.User;
import com.internhub.repository.ActivityLogRepository;
import com.internhub.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Service for managing activity logs and audit trail.
 */
@Service
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;

    @Autowired
    public ActivityLogService(ActivityLogRepository activityLogRepository,
            UserRepository userRepository) {
        this.activityLogRepository = activityLogRepository;
        this.userRepository = userRepository;
    }

    /**
     * Log a user activity.
     */
    @Transactional
    public ActivityLog logActivity(String userEmail, String actionType, String description) {
        User user = userRepository.findByEmail(userEmail).orElse(null);
        ActivityLog log = new ActivityLog(user, actionType, description);
        enrichWithRequestInfo(log);
        return activityLogRepository.save(log);
    }

    /**
     * Log activity with entity reference.
     */
    @Transactional
    public ActivityLog logActivity(String userEmail, String actionType, String entityType,
            Long entityId, String description) {
        User user = userRepository.findByEmail(userEmail).orElse(null);
        ActivityLog log = new ActivityLog(user, actionType, entityType, entityId, description);
        enrichWithRequestInfo(log);
        return activityLogRepository.save(log);
    }

    /**
     * Log activity with old/new values (for updates).
     */
    @Transactional
    public ActivityLog logActivityWithValues(String userEmail, String actionType, String entityType,
            Long entityId, String description,
            String oldValue, String newValue) {
        User user = userRepository.findByEmail(userEmail).orElse(null);
        ActivityLog log = new ActivityLog(user, actionType, entityType, entityId, description);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        enrichWithRequestInfo(log);
        return activityLogRepository.save(log);
    }

    /**
     * Get all activity logs with pagination.
     */
    @Transactional(readOnly = true)
    public Page<ActivityLogDTO> getAllLogs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return activityLogRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::convertToDTO);
    }

    /**
     * Get activity logs by user.
     */
    @Transactional(readOnly = true)
    public Page<ActivityLogDTO> getLogsByUser(String userEmail, int page, int size) {
        User user = userRepository.findByEmail(userEmail).orElse(null);
        if (user == null) {
            return Page.empty();
        }
        Pageable pageable = PageRequest.of(page, size);
        return activityLogRepository.findByUserOrderByCreatedAtDesc(user, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Get activity logs by action type.
     */
    @Transactional(readOnly = true)
    public Page<ActivityLogDTO> getLogsByActionType(String actionType, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return activityLogRepository.findByActionTypeOrderByCreatedAtDesc(actionType, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Get activity logs by entity.
     */
    @Transactional(readOnly = true)
    public Page<ActivityLogDTO> getLogsByEntity(String entityType, Long entityId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return activityLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
                entityType, entityId, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Get activity logs by date range.
     */
    @Transactional(readOnly = true)
    public Page<ActivityLogDTO> getLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate,
            int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return activityLogRepository.findByDateRange(startDate, endDate, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Get activity logs with filters.
     */
    @Transactional(readOnly = true)
    public Page<ActivityLogDTO> getLogsWithFilters(String userEmail, String actionType,
            String entityType, LocalDateTime startDate,
            LocalDateTime endDate, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return activityLogRepository.findByFilters(userEmail, actionType, entityType,
                startDate, endDate, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Get distinct action types.
     */
    @Transactional(readOnly = true)
    public List<String> getActionTypes() {
        return activityLogRepository.findDistinctActionTypes();
    }

    /**
     * Get distinct entity types.
     */
    @Transactional(readOnly = true)
    public List<String> getEntityTypes() {
        return activityLogRepository.findDistinctEntityTypes();
    }

    /**
     * Delete old logs (data retention).
     */
    @Transactional
    public void deleteOldLogs(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        activityLogRepository.deleteOldLogs(cutoffDate);
    }

    /**
     * Convert ActivityLog to DTO.
     */
    private ActivityLogDTO convertToDTO(ActivityLog log) {
        return new ActivityLogDTO(
                log.getId(),
                log.getUserEmail(),
                log.getActionType(),
                log.getEntityType(),
                log.getEntityId(),
                log.getDescription(),
                log.getIpAddress(),
                log.getOldValue(),
                log.getNewValue(),
                log.getCreatedAt()
        );
    }

    /**
     * Enrich log with HTTP request information.
     */
    private void enrichWithRequestInfo(ActivityLog log) {
        try {
            ServletRequestAttributes attributes
                    = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                log.setIpAddress(getClientIpAddress(request));
                log.setUserAgent(request.getHeader("User-Agent"));
            }
        } catch (Exception e) {
            // Silently fail if request context is not available
        }
    }

    /**
     * Get client IP address from request.
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headers = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }

        return request.getRemoteAddr();
    }

    // Action type constants
    public static final String ACTION_LOGIN = "USER_LOGIN";
    public static final String ACTION_LOGOUT = "USER_LOGOUT";
    public static final String ACTION_REGISTER = "USER_REGISTER";
    public static final String ACTION_PASSWORD_CHANGE = "PASSWORD_CHANGE";
    public static final String ACTION_INTERNSHIP_CREATE = "INTERNSHIP_CREATE";
    public static final String ACTION_INTERNSHIP_UPDATE = "INTERNSHIP_UPDATE";
    public static final String ACTION_INTERNSHIP_DELETE = "INTERNSHIP_DELETE";
    public static final String ACTION_INTERNSHIP_SUBMIT = "INTERNSHIP_SUBMIT";
    public static final String ACTION_INTERNSHIP_CLAIM = "INTERNSHIP_CLAIM";
    public static final String ACTION_INTERNSHIP_VALIDATE = "INTERNSHIP_VALIDATE";
    public static final String ACTION_INTERNSHIP_REFUSE = "INTERNSHIP_REFUSE";
    public static final String ACTION_COMMENT_ADD = "COMMENT_ADD";
    public static final String ACTION_COMMENT_UPDATE = "COMMENT_UPDATE";
    public static final String ACTION_COMMENT_DELETE = "COMMENT_DELETE";
    public static final String ACTION_USER_CREATE = "USER_CREATE";
    public static final String ACTION_USER_UPDATE = "USER_UPDATE";
    public static final String ACTION_USER_DELETE = "USER_DELETE";
}
