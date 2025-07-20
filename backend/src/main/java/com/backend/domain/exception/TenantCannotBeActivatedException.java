package com.backend.domain.exception;

public class TenantCannotBeActivatedException extends RuntimeException {
    
    public TenantCannotBeActivatedException() {
        super("Tenant cannot be activated in current status");
    }
    
    public TenantCannotBeActivatedException(String message) {
        super(message);
    }
    
    public TenantCannotBeActivatedException(String message, Throwable cause) {
        super(message, cause);
    }
}