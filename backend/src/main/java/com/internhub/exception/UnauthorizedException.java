package com.internhub.exception;

/**
 * Exception for unauthorized access attempts. Follows SRP: Handles
 * authorization violations only.
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
