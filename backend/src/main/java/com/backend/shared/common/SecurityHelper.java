package com.backend.shared.common;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Security helper utility for tenant isolation and user context management.
 * Provides secure methods to get current user information and validate tenant access.
 */
@Component
public class SecurityHelper {

    /**
     * Gets the current authenticated user's tenant ID from security context.
     * 
     * @return Current user's tenant ID
     * @throws AccessDeniedException if user is not authenticated or tenant ID not found
     */
    public Long getCurrentUserTenantId() {
        Long tenantId = SecurityUtil.getCurrentUserTenantId();
        if (tenantId == null) {
            throw new AccessDeniedException("User tenant ID not found in security context");
        }
        return tenantId;
    }

    /**
     * Gets the current authenticated user's ID from security context.
     * 
     * @return Current user's ID
     * @throws AccessDeniedException if user is not authenticated
     */
    public Long getCurrentUserId() {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            throw new AccessDeniedException("User ID not found in security context");
        }
        return userId;
    }

    /**
     * Gets the current authenticated user's email from security context.
     * 
     * @return Current user's email
     * @throws AccessDeniedException if user is not authenticated
     */
    public String getCurrentUserEmail() {
        String email = SecurityUtil.getCurrentUserEmail();
        if (email == null) {
            throw new AccessDeniedException("User email not found in security context");
        }
        return email;
    }

    /**
     * Validates that the provided tenant ID matches the current user's tenant ID.
     * Ensures tenant isolation by preventing cross-tenant access.
     * 
     * @param requestedTenantId The tenant ID being requested for access
     * @throws AccessDeniedException if tenant IDs don't match
     */
    public void validateTenantAccess(Long requestedTenantId) {
        Long currentUserTenantId = getCurrentUserTenantId();
        if (!currentUserTenantId.equals(requestedTenantId)) {
            throw new AccessDeniedException(
                String.format("Access denied. User tenant ID (%d) does not match requested tenant ID (%d)", 
                             currentUserTenantId, requestedTenantId));
        }
    }

    /**
     * Gets current authentication from security context.
     * 
     * @return Current authentication or null if not authenticated
     */
    public Authentication getCurrentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * Checks if current user is authenticated.
     * 
     * @return true if user is authenticated, false otherwise
     */
    public boolean isAuthenticated() {
        Authentication authentication = getCurrentAuthentication();
        return authentication != null && authentication.isAuthenticated() 
               && !"anonymousUser".equals(authentication.getPrincipal());
    }

    /**
     * Ensures user is authenticated, throws exception if not.
     * 
     * @throws AccessDeniedException if user is not authenticated
     */
    public void requireAuthentication() {
        if (!isAuthenticated()) {
            throw new AccessDeniedException("Authentication required");
        }
    }
}