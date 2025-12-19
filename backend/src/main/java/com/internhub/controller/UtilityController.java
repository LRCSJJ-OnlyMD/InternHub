package com.internhub.controller;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Temporary utility controller for development purposes. Should be removed or
 * secured in production.
 */
@RestController
@RequestMapping("/api/utility")
public class UtilityController {

    private final PasswordEncoder passwordEncoder;

    public UtilityController(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Generate BCrypt hash for a given password. REMOVE THIS IN PRODUCTION!
     */
    @PostMapping("/hash-password")
    public Map<String, String> hashPassword(@RequestBody Map<String, String> request) {
        String password = request.get("password");
        String hash = passwordEncoder.encode(password);

        Map<String, String> response = new HashMap<>();
        response.put("password", password);
        response.put("hash", hash);
        response.put("algorithm", "BCrypt");

        return response;
    }

    /**
     * Verify if a password matches a BCrypt hash
     */
    @PostMapping("/verify-password")
    public Map<String, Object> verifyPassword(@RequestBody Map<String, String> request) {
        String password = request.get("password");
        String hash = request.get("hash");
        boolean matches = passwordEncoder.matches(password, hash);

        Map<String, Object> response = new HashMap<>();
        response.put("password", password);
        response.put("hash", hash);
        response.put("matches", matches);

        return response;
    }
}
