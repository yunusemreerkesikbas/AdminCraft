package com.backend.domain.exception;

public class InvalidTokenException extends RuntimeException {
    
    public InvalidTokenException() {
        super("Invalid or expired token");
    }
    
    public InvalidTokenException(String message) {
        super(message);
    }
    
    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}