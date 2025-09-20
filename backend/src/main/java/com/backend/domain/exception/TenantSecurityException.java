package com.backend.domain.exception;

/**
 * Exception thrown when a security violation is detected related to tenant isolation.
 * This includes attempts to access resources belonging to other tenants.
 */
public class TenantSecurityException extends RuntimeException {

    public TenantSecurityException(String message) {
        super(message);
    }

    public TenantSecurityException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates exception for invalid component access attempt
     */
    public static TenantSecurityException invalidComponentAccess(Long componentId, Long tenantId) {
        return new TenantSecurityException(
            String.format("Component with ID %d does not belong to tenant %d", componentId, tenantId)
        );
    }

    /**
     * Creates exception for batch component access validation failure
     */
    public static TenantSecurityException invalidBatchComponentAccess(Long tenantId) {
        return new TenantSecurityException(
            String.format("One or more components do not belong to tenant %d", tenantId)
        );
    }
}