package com.backend.presentation.controller;

import java.util.Map;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.backend.application.dto.config.ConfigPrincipal;

@Component
public class ConfigPrincipalResolver {

    public ConfigPrincipal resolve(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Authentication required");
        }

        Object details = authentication.getDetails();
        if (!(details instanceof Map<?, ?> detailsMap)) {
            throw new AccessDeniedException("Invalid authentication details");
        }

        Long userId = toLong(detailsMap.get("userId"));
        Long tenantId = toLong(detailsMap.get("tenantId"));
        String email = detailsMap.get("email") instanceof String s ? s : null;
        String role = detailsMap.get("role") instanceof String s ? s : null;

        if (userId == null || email == null || role == null) {
            throw new AccessDeniedException("Missing auth claims");
        }

        return new ConfigPrincipal(userId, email, role, tenantId);
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number num) {
            return num.longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
