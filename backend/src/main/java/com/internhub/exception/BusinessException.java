package com.internhub.exception;

/**
 * Exception for business logic violations. Follows SRP: Handles business rule
 * violations only.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
