package com.internhub.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.internhub.config.JwtTokenProvider;
import com.internhub.dto.ActivateAccountRequest;
import com.internhub.dto.AuthResponse;
import com.internhub.dto.ChangePasswordRequest;
import com.internhub.dto.LoginRequest;
import com.internhub.dto.MessageResponse;
import com.internhub.dto.PasswordResetConfirmRequest;
import com.internhub.dto.PasswordResetRequest;
import com.internhub.dto.RegisterRequest;
import com.internhub.dto.TwoFactorResponse;
import com.internhub.dto.UpdateProfileRequest;
import com.internhub.model.Role;
import com.internhub.model.User;
import com.internhub.model.VerificationToken;
import com.internhub.repository.UserRepository;
import com.internhub.repository.VerificationTokenRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final VerificationTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final TwoFactorAuthService twoFactorAuthService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final RedisBackupService redisBackupService;
    private final ActivityLogService activityLogService;

    public AuthService(
            UserRepository userRepository,
            VerificationTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService,
            TwoFactorAuthService twoFactorAuthService,
            JwtTokenProvider jwtTokenProvider,
            AuthenticationManager authenticationManager,
            RedisBackupService redisBackupService,
            ActivityLogService activityLogService) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.twoFactorAuthService = twoFactorAuthService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
        this.redisBackupService = redisBackupService;
        this.activityLogService = activityLogService;
    }

    @Transactional
    public MessageResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setRole(Role.STUDENT);  // Only students can self-register
        user.setEnabled(false);
        user.setTwoFactorEnabled(false);
        user.setCreatedAt(LocalDateTime.now());

        userRepository.save(user);

        // Backup to Redis for emergency recovery
        redisBackupService.cacheUser(user);

        // Log activity
        activityLogService.logActivity(user.getEmail(), ActivityLogService.ACTION_REGISTER,
                "User registered successfully");

        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken(token, user, "EMAIL_VERIFICATION");
        tokenRepository.save(verificationToken);

        // Cache token for quick verification
        redisBackupService.cacheVerificationToken(token, user.getId());

        emailService.sendVerificationEmail(user.getEmail(), token);

        return new MessageResponse("Registration successful. Please check your email to verify your account.");
    }

    @Transactional
    public MessageResponse verifyEmail(String token) {
        VerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid verification token"));

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Verification token has expired");
        }

        if (!verificationToken.getType().equals("EMAIL_VERIFICATION")) {
            throw new RuntimeException("Invalid token type");
        }

        User user = verificationToken.getUser();
        user.setEnabled(true);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // Update cached user data
        redisBackupService.updateCachedUser(user);

        tokenRepository.delete(verificationToken);

        return new MessageResponse("Email verified successfully. You can now login.");
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!user.isEnabled()) {
            throw new RuntimeException("Please verify your email before logging in");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // 2FA is now handled in settings, not during login
        String token = jwtTokenProvider.generateToken(user.getEmail(), user.getId(), user.getRole().name());

        // Log activity
        activityLogService.logActivity(user.getEmail(), ActivityLogService.ACTION_LOGIN,
                "User logged in successfully");

        return new AuthResponse(token, user.getEmail(), user.getFirstName(),
                user.getLastName(), user.getRole().name(), user.isTwoFactorEnabled(), null, user.getId(),
                user.isMustChangePassword());
    }

    @Transactional
    public MessageResponse requestPasswordReset(PasswordResetRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        tokenRepository.findByUserAndType(user, "PASSWORD_RESET")
                .ifPresent(tokenRepository::delete);

        String token = UUID.randomUUID().toString();
        VerificationToken resetToken = new VerificationToken(token, user, "PASSWORD_RESET");
        tokenRepository.save(resetToken);

        emailService.sendPasswordResetEmail(user.getEmail(), token);

        return new MessageResponse("Password reset email sent. Please check your inbox.");
    }

    @Transactional
    public MessageResponse resetPassword(PasswordResetConfirmRequest request) {
        VerificationToken resetToken = tokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("Invalid reset token"));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Reset token has expired");
        }

        if (!resetToken.getType().equals("PASSWORD_RESET")) {
            throw new RuntimeException("Invalid token type");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        tokenRepository.delete(resetToken);

        return new MessageResponse("Password reset successful. You can now login with your new password.");
    }

    @Transactional
    public TwoFactorResponse enableTwoFactor(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.isTwoFactorEnabled()) {
            throw new RuntimeException("2FA is already enabled");
        }

        String secret = twoFactorAuthService.generateSecretKey();
        String qrCodeUrl = twoFactorAuthService.generateQRUrl(email, secret);

        // Save secret but don't enable 2FA yet - wait for confirmation
        user.setTwoFactorSecret(secret);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return new TwoFactorResponse(qrCodeUrl, secret,
                "Scan the QR code with your authenticator app and enter the code to confirm.");
    }

    @Transactional
    public MessageResponse confirm2FA(String email, String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.isTwoFactorEnabled()) {
            throw new RuntimeException("2FA is already enabled");
        }

        if (user.getTwoFactorSecret() == null) {
            throw new RuntimeException("Please enable 2FA first");
        }

        int verificationCode;
        try {
            verificationCode = Integer.parseInt(code);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid code format");
        }

        if (!twoFactorAuthService.validateCode(user.getTwoFactorSecret(), verificationCode)) {
            throw new RuntimeException("Invalid verification code");
        }

        // Now enable 2FA
        user.setTwoFactorEnabled(true);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        emailService.sendTwoFactorEnabledEmail(email);

        return new MessageResponse("2FA enabled successfully!");
    }

    @Transactional
    public MessageResponse send2FACodeViaEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isTwoFactorEnabled() || user.getTwoFactorSecret() == null) {
            throw new RuntimeException("2FA is not enabled for this user");
        }

        // Generate current code from secret
        int currentCode = twoFactorAuthService.getCurrentCode(user.getTwoFactorSecret());

        emailService.send2FACodeEmail(user.getEmail(), currentCode);

        return new MessageResponse("2FA code sent to your email");
    }

    public AuthResponse getUserInfo(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return new AuthResponse(null, user.getEmail(), user.getFirstName(),
                user.getLastName(), user.getRole().name(), user.isTwoFactorEnabled(), null, user.getId());
    }

    @Transactional
    public MessageResponse disableTwoFactor(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isTwoFactorEnabled()) {
            throw new RuntimeException("2FA is not enabled");
        }

        user.setTwoFactorEnabled(false);
        user.setTwoFactorSecret(null);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return new MessageResponse("2FA disabled successfully");
    }

    @Transactional
    public AuthResponse updateProfile(String currentEmail, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if new email is already taken by another user
        if (!currentEmail.equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use by another account");
        }

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // Update cached user data
        redisBackupService.updateCachedUser(user);

        return new AuthResponse(null, user.getEmail(), user.getFirstName(),
                user.getLastName(), user.getRole().name(), user.isTwoFactorEnabled(), null, user.getId());
    }

    @Transactional
    public MessageResponse changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        // Update to new password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setMustChangePassword(false);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // Update cached user data
        redisBackupService.updateCachedUser(user);

        return new MessageResponse("Password changed successfully");
    }

    @Transactional
    public AuthResponse activateAccount(ActivateAccountRequest request) {
        // Verify passwords match
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }

        // Find user by activation token
        User user = userRepository.findByActivationToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("Invalid activation token"));

        // Check if token has expired
        if (user.getActivationTokenExpiry() == null
                || user.getActivationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Activation token has expired");
        }

        // Check if already activated
        if (user.isAccountActivated()) {
            throw new RuntimeException("Account is already activated");
        }

        // Activate account
        user.setEnabled(true);
        user.setAccountActivated(true);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setMustChangePassword(false);
        user.setActivationToken(null);
        user.setActivationTokenExpiry(null);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // Update cached user data
        redisBackupService.updateCachedUser(user);

        // Generate JWT token for auto-login
        String token = jwtTokenProvider.generateToken(user.getEmail());

        return new AuthResponse(token, user.getEmail(), user.getFirstName(),
                user.getLastName(), user.getRole().name(), user.isTwoFactorEnabled(), null, user.getId());
    }
}
