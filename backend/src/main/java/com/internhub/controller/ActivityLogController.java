package com.internhub.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.internhub.dto.ActivityLogDTO;
import com.internhub.service.ActivityLogService;

/**
 * REST controller for activity logs (Admin only).
 */
@RestController
@RequestMapping("/api/activity-logs")
@CrossOrigin(origins = "*", maxAge = 3600)
@PreAuthorize("hasAuthority('ADMIN')")
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    @Autowired
    public ActivityLogController(ActivityLogService activityLogService) {
        this.activityLogService = activityLogService;
    }

    /**
     * Get all activity logs with pagination. GET /api/admin/activity-logs
     */
    @GetMapping
    public ResponseEntity<Page<ActivityLogDTO>> getAllLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Page<ActivityLogDTO> logs = activityLogService.getAllLogs(page, size);
        return ResponseEntity.ok(logs);
    }

    /**
     * Get activity logs with filters. GET /api/admin/activity-logs/search
     */
    @GetMapping("/search")
    public ResponseEntity<Page<ActivityLogDTO>> searchLogs(
            @RequestParam(required = false) String userEmail,
            @RequestParam(required = false) String actionType,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Page<ActivityLogDTO> logs = activityLogService.getLogsWithFilters(
                userEmail, actionType, entityType, startDate, endDate, page, size);
        return ResponseEntity.ok(logs);
    }

    /**
     * Get activity logs by user. GET /api/admin/activity-logs/user/{userEmail}
     */
    @GetMapping("/user/{userEmail}")
    public ResponseEntity<Page<ActivityLogDTO>> getLogsByUser(
            @PathVariable String userEmail,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Page<ActivityLogDTO> logs = activityLogService.getLogsByUser(userEmail, page, size);
        return ResponseEntity.ok(logs);
    }

    /**
     * Get activity logs by action type. GET
     * /api/admin/activity-logs/action/{actionType}
     */
    @GetMapping("/action/{actionType}")
    public ResponseEntity<Page<ActivityLogDTO>> getLogsByAction(
            @PathVariable String actionType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Page<ActivityLogDTO> logs = activityLogService.getLogsByActionType(actionType, page, size);
        return ResponseEntity.ok(logs);
    }

    /**
     * Get activity logs by entity. GET
     * /api/admin/activity-logs/entity/{entityType}/{entityId}
     */
    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<Page<ActivityLogDTO>> getLogsByEntity(
            @PathVariable String entityType,
            @PathVariable Long entityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Page<ActivityLogDTO> logs = activityLogService.getLogsByEntity(entityType, entityId, page, size);
        return ResponseEntity.ok(logs);
    }

    /**
     * Get distinct action types. GET /api/admin/activity-logs/action-types
     */
    @GetMapping("/action-types")
    public ResponseEntity<List<String>> getActionTypes() {
        List<String> actionTypes = activityLogService.getActionTypes();
        return ResponseEntity.ok(actionTypes);
    }

    /**
     * Get distinct entity types. GET /api/admin/activity-logs/entity-types
     */
    @GetMapping("/entity-types")
    public ResponseEntity<List<String>> getEntityTypes() {
        List<String> entityTypes = activityLogService.getEntityTypes();
        return ResponseEntity.ok(entityTypes);
    }
}
