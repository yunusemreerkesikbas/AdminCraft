package com.backend.domain.exception;

/**
 * Exception thrown when there's a tenant boundary violation.
 * This ensures proper tenant isolation in the multi-tenant system.
 */
public class TenantMismatchException extends RuntimeException {

    public TenantMismatchException(Long expectedTenantId, Long actualTenantId) {
        super(String.format("Tenant mismatch: expected %d, but got %d", expectedTenantId, actualTenantId));
    }

    public TenantMismatchException(String message) {
        super(message);
    }

    public TenantMismatchException(String message, Throwable cause) {
        super(message, cause);
    }
}