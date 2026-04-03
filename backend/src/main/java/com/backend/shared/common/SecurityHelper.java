package com.backend.shared.common;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityHelper {

    public Long getCurrentUserTenantId() {
        requireAuthentication();

        if (isSuperAdmin()) {
            return null;
        }

        Long tenantId = SecurityUtil.getCurrentUserTenantId();
        if (tenantId == null) {
            throw new AccessDeniedException("User tenant ID not found in security context");
        }
        return tenantId;
    }

    public Long getCurrentUserId() {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            throw new AccessDeniedException("User ID not found in security context");
        }
        return userId;
    }

    public Long getCurrentUserIdOrNull() {
        try {
            return getCurrentUserId();
        } catch (AccessDeniedException e) {
            return null;
        }
    }

    public String getCurrentUserEmail() {
        String email = SecurityUtil.getCurrentUserEmail();
        if (email == null) {
            throw new AccessDeniedException("User email not found in security context");
        }
        return email;
    }

    public void validateTenantAccess(Long requestedTenantId) {
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

    public Authentication getCurrentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public boolean isAuthenticated() {
        Authentication authentication = getCurrentAuthentication();
        return authentication != null && authentication.isAuthenticated()
               && !"anonymousUser".equals(authentication.getPrincipal());
    }

    public void requireAuthentication() {
        if (!isAuthenticated()) {
            throw new AccessDeniedException("Authentication required");
        }
    }

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
