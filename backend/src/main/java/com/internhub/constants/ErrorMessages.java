package com.internhub.constants;

/**
 * Centralized error messages. Follows DRY principle and makes messages
 * consistent and maintainable.
 */
public final class ErrorMessages {

    private ErrorMessages() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // User related
    public static final String USER_NOT_FOUND = "User not found";
    public static final String USER_NOT_FOUND_WITH_ID = "User not found with ID: %s";
    public static final String USER_NOT_FOUND_WITH_EMAIL = "User not found with email: %s";
    public static final String EMAIL_ALREADY_EXISTS = "Email already exists: %s";
    public static final String INVALID_CREDENTIALS = "Invalid email or password";
    public static final String USER_NOT_ENABLED = "Account not verified. Please check your email.";

    // Internship related
    public static final String INTERNSHIP_NOT_FOUND = "Internship not found";
    public static final String INTERNSHIP_NOT_FOUND_WITH_ID = "Internship not found with ID: %s";
    public static final String CANNOT_MODIFY_INTERNSHIP = "Cannot modify internship in current status";
    public static final String UNAUTHORIZED_INTERNSHIP_ACCESS = "Unauthorized: You don't own this internship";
    public static final String INTERNSHIP_ALREADY_VALIDATED = "Internship is already validated";

    // Sector related
    public static final String SECTOR_NOT_FOUND = "Sector not found";
    public static final String SECTOR_NOT_FOUND_WITH_ID = "Sector not found with ID: %s";
    public static final String SECTOR_NAME_EXISTS = "Sector with name '%s' already exists";

    // Instructor related
    public static final String INSTRUCTOR_NOT_FOUND = "Instructor not found";
    public static final String USER_NOT_INSTRUCTOR = "User is not an instructor";
    public static final String INSTRUCTOR_NOT_ASSIGNED = "No instructor assigned to this internship";

    // Role related
    public static final String INVALID_ROLE = "Invalid role: %s";
    public static final String UNAUTHORIZED_ROLE_ACCESS = "Unauthorized: Insufficient permissions";

    // Token related
    public static final String TOKEN_EXPIRED = "Token has expired";
    public static final String TOKEN_INVALID = "Invalid token";
    public static final String TOKEN_NOT_FOUND = "Token not found";

    // 2FA related
    public static final String TWO_FA_ALREADY_ENABLED = "Two-factor authentication is already enabled";
    public static final String TWO_FA_NOT_ENABLED = "Two-factor authentication is not enabled";
    public static final String INVALID_2FA_CODE = "Invalid verification code";
}
