package com.internhub.controller;

import com.internhub.dto.MessageResponse;
import com.internhub.service.RedisBackupService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final RedisBackupService redisBackupService;

    public HealthController(RedisBackupService redisBackupService) {
        this.redisBackupService = redisBackupService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "auth-backend");
        health.put("timestamp", System.currentTimeMillis());

        // Check Redis backup system
        boolean redisHealthy = redisBackupService.isHealthy();
        health.put("redis", redisHealthy ? "UP" : "DOWN");
        health.put("backup", redisHealthy ? "AVAILABLE" : "UNAVAILABLE");

        return ResponseEntity.ok(health);
    }

    @GetMapping("/redis")
    public ResponseEntity<MessageResponse> redisHealth() {
        boolean healthy = redisBackupService.isHealthy();
        String message = healthy
                ? "Redis backup system is operational"
                : "Redis backup system is down - check configuration";

        return healthy
                ? ResponseEntity.ok(new MessageResponse(message))
                : ResponseEntity.status(503).body(new MessageResponse(message));
    }
}
