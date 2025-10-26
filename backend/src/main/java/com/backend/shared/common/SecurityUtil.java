package com.backend.shared.common;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;

public class SecurityUtil {
    
    private SecurityUtil() {
        // Utility class
    }
    
    /**
     * Gets the currently authenticated user ID from security context
     * @return User ID or null if not authenticated
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        
        // Get user ID from authentication details (set by JWT filter)
        Object details = authentication.getDetails();
        if (details instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> detailsMap = (Map<String, Object>) details;
            Object userId = detailsMap.get("userId");
            if (userId instanceof Long) {
                return (Long) userId;
            }
            // userId is missing from JWT token - this indicates an old token
            // User needs to re-login to get a token with userId claim
        }

        return null;
    }
    
    /**
     * Gets the currently authenticated user ID, throws exception if not found
     * @return User ID
     * @throws IllegalStateException if user is not authenticated
     */
    public static Long getCurrentUserIdOrThrow() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("User is not authenticated");
        }
        return userId;
    }
    
    /**
     * Gets the currently authenticated user's tenant ID
     * @return Tenant ID or null if not authenticated or no tenant
     */
    public static Long getCurrentUserTenantId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        
        Object details = authentication.getDetails();
        if (details instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> detailsMap = (Map<String, Object>) details;
            Object tenantId = detailsMap.get("tenantId");
            if (tenantId instanceof Long) {
                return (Long) tenantId;
            }
        }
        
        return null;
    }
    
    /**
     * Gets the currently authenticated user's email
     * @return Email or null if not authenticated
     */
    public static String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        
        Object principal = authentication.getPrincipal();
        if (principal instanceof String) {
            return (String) principal;
        }
        
        return null;
    }
    
    /**
     * Gets the currently authenticated user's role
     * @return Role or null if not authenticated
     */
    public static String getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        
        Object details = authentication.getDetails();
        if (details instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> detailsMap = (Map<String, Object>) details;
            Object role = detailsMap.get("role");
            if (role instanceof String) {
                return (String) role;
            }
        }
        
        return null;
    }
    
    /**
     * Checks if a user is authenticated
     * @return true if authenticated
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() && 
               !"anonymousUser".equals(authentication.getPrincipal());
    }
}