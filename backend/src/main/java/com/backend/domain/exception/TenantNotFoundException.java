package com.backend.domain.exception;

/**
 * Exception thrown when a tenant is not found.
 * This is a runtime exception that indicates a tenant with the specified identifier does not exist.
 */
public class TenantNotFoundException extends RuntimeException {
    
    public TenantNotFoundException(String message) {
        super(message);
    }
    
    public TenantNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public TenantNotFoundException(Long tenantId) {
        super("Tenant not found with ID: " + tenantId);
    }
}