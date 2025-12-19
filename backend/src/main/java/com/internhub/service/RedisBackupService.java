package com.internhub.service;

import com.internhub.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Redis-based caching and backup service for emergency data recovery. Provides
 * backup mechanism in case of primary database failure.
 */
@Service
public class RedisBackupService {

    private static final Logger logger = LoggerFactory.getLogger(RedisBackupService.class);
    private static final String USER_CACHE_PREFIX = "user:";
    private static final String USER_EMAIL_INDEX = "user:email:";
    private static final long CACHE_TTL_HOURS = 24;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Cache user data for emergency backup
     */
    public void cacheUser(User user) {
        try {
            String userKey = USER_CACHE_PREFIX + user.getId();
            String emailKey = USER_EMAIL_INDEX + user.getEmail();

            // Cache user by ID
            redisTemplate.opsForValue().set(userKey, user, CACHE_TTL_HOURS, TimeUnit.HOURS);

            // Cache user ID by email for quick lookup
            redisTemplate.opsForValue().set(emailKey, user.getId(), CACHE_TTL_HOURS, TimeUnit.HOURS);

            logger.info("Cached user data for emergency backup: {}", user.getEmail());
        } catch (Exception e) {
            logger.error("Failed to cache user data: {}", e.getMessage());
        }
    }

    /**
     * Retrieve user from cache by ID
     */
    public User getUserById(Long userId) {
        try {
            String userKey = USER_CACHE_PREFIX + userId;
            Object cached = redisTemplate.opsForValue().get(userKey);

            if (cached != null) {
                return objectMapper.convertValue(cached, User.class);
            }
        } catch (Exception e) {
            logger.error("Failed to retrieve user from cache: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Retrieve user from cache by email
     */
    public User getUserByEmail(String email) {
        try {
            String emailKey = USER_EMAIL_INDEX + email;
            Object userId = redisTemplate.opsForValue().get(emailKey);

            if (userId != null) {
                return getUserById(Long.valueOf(userId.toString()));
            }
        } catch (Exception e) {
            logger.error("Failed to retrieve user by email from cache: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Update cached user data
     */
    public void updateCachedUser(User user) {
        cacheUser(user);
    }

    /**
     * Remove user from cache
     */
    public void evictUser(Long userId, String email) {
        try {
            String userKey = USER_CACHE_PREFIX + userId;
            String emailKey = USER_EMAIL_INDEX + email;

            redisTemplate.delete(userKey);
            redisTemplate.delete(emailKey);

            logger.info("Evicted user from cache: {}", email);
        } catch (Exception e) {
            logger.error("Failed to evict user from cache: {}", e.getMessage());
        }
    }

    /**
     * Cache verification token for quick access
     */
    public void cacheVerificationToken(String token, Long userId) {
        try {
            String tokenKey = "token:" + token;
            redisTemplate.opsForValue().set(tokenKey, userId, 24, TimeUnit.HOURS);
            logger.info("Cached verification token for user: {}", userId);
        } catch (Exception e) {
            logger.error("Failed to cache verification token: {}", e.getMessage());
        }
    }

    /**
     * Retrieve user ID from cached token
     */
    public Long getUserIdByToken(String token) {
        try {
            String tokenKey = "token:" + token;
            Object userId = redisTemplate.opsForValue().get(tokenKey);
            return userId != null ? Long.valueOf(userId.toString()) : null;
        } catch (Exception e) {
            logger.error("Failed to retrieve token from cache: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Health check - verify Redis connection
     */
    public boolean isHealthy() {
        try {
            redisTemplate.opsForValue().set("health:check", "ok", 10, TimeUnit.SECONDS);
            return true;
        } catch (Exception e) {
            logger.error("Redis health check failed: {}", e.getMessage());
            return false;
        }
    }
}
