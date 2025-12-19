package com.internhub.controller;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.internhub.dto.MessageResponse;
import com.internhub.model.Role;
import com.internhub.model.Sector;
import com.internhub.model.User;
import com.internhub.repository.SectorRepository;
import com.internhub.repository.UserRepository;
import com.internhub.service.EmailService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Controller for admin user management operations. Only accessible by users
 * with ADMIN role.
 */
@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasAuthority('ADMIN')")
@CrossOrigin(origins = "http://localhost:4200")
public class UserManagementController {

    private final UserRepository userRepository;
    private final SectorRepository sectorRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public UserManagementController(
            UserRepository userRepository,
            SectorRepository sectorRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService) {
        this.userRepository = userRepository;
        this.sectorRepository = sectorRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    /**
     * Create an instructor account. Only admins can create instructor accounts.
     */
    @PostMapping("/instructors")
    public ResponseEntity<?> createInstructor(@Valid @RequestBody CreateInstructorRequest request) {
        try {
            if (userRepository.existsByEmail(request.getEmail())) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Email already exists"));
            }

            // Generate activation token
            String activationToken = UUID.randomUUID().toString();
            LocalDateTime tokenExpiry = LocalDateTime.now().plusHours(24);

            User instructor = new User();
            instructor.setEmail(request.getEmail());
            instructor.setPassword(passwordEncoder.encode(UUID.randomUUID().toString())); // Temporary, user will set their own
            instructor.setFirstName(request.getFirstName());
            instructor.setLastName(request.getLastName());
            instructor.setRole(Role.INSTRUCTOR);
            instructor.setEnabled(false);  // Disabled until activation
            instructor.setAccountActivated(false);
            instructor.setMustChangePassword(true);
            instructor.setActivationToken(activationToken);
            instructor.setActivationTokenExpiry(tokenExpiry);
            instructor.setTwoFactorEnabled(false);
            instructor.setCreatedAt(LocalDateTime.now());

            // Assign sectors
            if (request.getSectorIds() != null && !request.getSectorIds().isEmpty()) {
                Set<Sector> sectors = new HashSet<>();
                for (Long sectorId : request.getSectorIds()) {
                    Sector sector = sectorRepository.findById(sectorId)
                            .orElseThrow(() -> new RuntimeException("Sector not found: " + sectorId));
                    sectors.add(sector);
                }
                instructor.setSectors(sectors);
            }

            userRepository.save(instructor);

            // Send activation email
            emailService.sendInstructorActivationEmail(
                    instructor.getEmail(),
                    activationToken,
                    instructor.getFirstName()
            );

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new MessageResponse(
                            "Instructor account created successfully. Activation email sent to: " + instructor.getEmail()
                    ));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error creating instructor: " + e.getMessage()));
        }
    }

    /**
     * Get all instructors
     */
    @GetMapping("/instructors")
    public ResponseEntity<List<UserDTO>> getAllInstructors() {
        List<User> instructors = userRepository.findAll().stream()
                .filter(user -> user.getRole() == Role.INSTRUCTOR)
                .toList();

        List<UserDTO> dtos = instructors.stream()
                .map(this::mapToDTO)
                .toList();

        return ResponseEntity.ok(dtos);
    }

    /**
     * Get all students
     */
    @GetMapping("/students")
    public ResponseEntity<List<UserDTO>> getAllStudents() {
        List<User> students = userRepository.findAll().stream()
                .filter(user -> user.getRole() == Role.STUDENT)
                .toList();

        List<UserDTO> dtos = students.stream()
                .map(this::mapToDTO)
                .toList();

        return ResponseEntity.ok(dtos);
    }

    /**
     * Update instructor sectors
     */
    @PutMapping("/instructors/{id}/sectors")
    public ResponseEntity<?> updateInstructorSectors(
            @PathVariable Long id,
            @RequestBody List<Long> sectorIds) {
        try {
            User instructor = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Instructor not found"));

            if (instructor.getRole() != Role.INSTRUCTOR) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("User is not an instructor"));
            }

            Set<Sector> sectors = new HashSet<>();
            for (Long sectorId : sectorIds) {
                Sector sector = sectorRepository.findById(sectorId)
                        .orElseThrow(() -> new RuntimeException("Sector not found: " + sectorId));
                sectors.add(sector);
            }

            instructor.setSectors(sectors);
            instructor.setUpdatedAt(LocalDateTime.now());
            userRepository.save(instructor);

            return ResponseEntity.ok(new MessageResponse("Instructor sectors updated successfully"));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error updating sectors: " + e.getMessage()));
        }
    }

    /**
     * Delete user (instructor or student)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (user.getRole() == Role.ADMIN) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Cannot delete admin accounts"));
            }

            userRepository.delete(user);

            return ResponseEntity.ok(new MessageResponse("User deleted successfully"));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error deleting user: " + e.getMessage()));
        }
    }

    // Helper method to map User to DTO
    private UserDTO mapToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setRole(user.getRole().name());
        dto.setEnabled(user.isEnabled());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setSectorIds(user.getSectors().stream().map(Sector::getId).toList());
        return dto;
    }

    // Inner DTOs
    public static class CreateInstructorRequest {

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        private String email;

        @NotBlank(message = "First name is required")
        @Size(min = 2, max = 50)
        private String firstName;

        @NotBlank(message = "Last name is required")
        @Size(min = 2, max = 50)
        private String lastName;

        private List<Long> sectorIds;

        // Getters and setters
        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public List<Long> getSectorIds() {
            return sectorIds;
        }

        public void setSectorIds(List<Long> sectorIds) {
            this.sectorIds = sectorIds;
        }
    }

    public static class UserDTO {

        private Long id;
        private String email;
        private String firstName;
        private String lastName;
        private String role;
        private boolean enabled;
        private LocalDateTime createdAt;
        private List<Long> sectorIds;

        // Getters and setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }

        public List<Long> getSectorIds() {
            return sectorIds;
        }

        public void setSectorIds(List<Long> sectorIds) {
            this.sectorIds = sectorIds;
        }
    }
}
