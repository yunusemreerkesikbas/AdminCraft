package com.backend.domain.exception;

public class UnauthorizedOperationException extends RuntimeException {
    
    public UnauthorizedOperationException() {
        super("User is not authorized to perform this operation");
    }
    
    public UnauthorizedOperationException(String operation) {
        super("User is not authorized to perform operation: " + operation);
    }
    
    public UnauthorizedOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}