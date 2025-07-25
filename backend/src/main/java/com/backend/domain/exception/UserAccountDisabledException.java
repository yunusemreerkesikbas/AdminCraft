package com.backend.domain.exception;

public class UserAccountDisabledException extends RuntimeException {
    
    public UserAccountDisabledException() {
        super("User account is disabled");
    }
    
    public UserAccountDisabledException(String message) {
        super(message);
    }
    
    public UserAccountDisabledException(String message, Throwable cause) {
        super(message, cause);
    }
}