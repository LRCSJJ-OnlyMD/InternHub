package com.internhub.service;

import com.internhub.model.Role;
import com.internhub.model.User;
import com.internhub.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Initializes the database with a default admin account on application startup.
 * The admin account is created only if no admin exists in the system.
 */
@Component
@Order(1)  // Run first before DataSeederService
public class AdminInitializerService implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminInitializerService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Check if admin already exists
        boolean adminExists = userRepository.findAll().stream()
                .anyMatch(user -> user.getRole() == Role.ADMIN);

        if (!adminExists) {
            createDefaultAdmin();
        }
    }

    private void createDefaultAdmin() {
        User admin = new User();
        admin.setEmail("admin@internship.com");
        admin.setPassword(passwordEncoder.encode("Admin123!"));  // Change this in production!
        admin.setFirstName("System");
        admin.setLastName("Administrator");
        admin.setRole(Role.ADMIN);
        admin.setEnabled(true);  // Admin account is pre-enabled
        admin.setTwoFactorEnabled(false);
        admin.setCreatedAt(LocalDateTime.now());

        userRepository.save(admin);

        System.out.println("========================================");
        System.out.println("DEFAULT ADMIN ACCOUNT CREATED");
        System.out.println("Email: admin@internship.com");
        System.out.println("Password: Admin123!");
        System.out.println("PLEASE CHANGE THE PASSWORD IMMEDIATELY!");
        System.out.println("========================================");
    }
}
