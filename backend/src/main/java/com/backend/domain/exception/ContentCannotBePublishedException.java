package com.backend.domain.exception;

public class ContentCannotBePublishedException extends RuntimeException {
    
    public ContentCannotBePublishedException(String message) {
        super(message);
    }
    
    public ContentCannotBePublishedException(String message, Throwable cause) {
        super(message, cause);
    }
}