package com.backend.shared.common;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
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
     * Returns null for SUPER_ADMIN users (they don't belong to a tenant).
     *
     * @return Current user's tenant ID, or null if SUPER_ADMIN
     * @throws AccessDeniedException if user is not authenticated
     */
    public Long getCurrentUserTenantId() {
        requireAuthentication();

        // SUPER_ADMIN users don't have a tenant ID (they can access all tenants)
        if (isSuperAdmin()) {
            return null;
        }

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
     * Gets the current authenticated user's ID, returning null when not authenticated
     * or when the user ID is absent from the security context instead of throwing.
     *
     * @return Current user's ID, or null if unavailable
     */
    public Long getCurrentUserIdOrNull() {
        try {
            return getCurrentUserId();
        } catch (AccessDeniedException e) {
            return null;
        }
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
     * SUPER_ADMIN users bypass this validation (can access all tenants).
     *
     * @param requestedTenantId The tenant ID being requested for access
     * @throws AccessDeniedException if tenant IDs don't match
     */
    public void validateTenantAccess(Long requestedTenantId) {
        // SUPER_ADMIN bypass - they can access all tenants
        if (isSuperAdmin()) {
            return;
        }

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

    /**
     * Checks if the current user has SUPER_ADMIN role.
     * SUPER_ADMIN users can access all tenants and bypass tenant isolation.
     *
     * @return true if user has SUPER_ADMIN role, false otherwise
     */
    public boolean isSuperAdmin() {
        Authentication authentication = getCurrentAuthentication();
        if (authentication == null) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> "ROLE_SUPER_ADMIN".equals(role));
    }
}