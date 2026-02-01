package com.backend.presentation.dto.response;

import java.time.LocalDateTime;

import com.backend.domain.entity.User;
import com.backend.domain.enums.UserRole;

public record UserResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        UserRole role,
        String phone,
        String jobTitle,
        String department,
        Boolean isActive,
        Boolean emailVerified,
        Boolean twoFactorEnabled,
        LocalDateTime lastLoginAt,
        String lastLoginIp,
        Integer failedLoginAttempts,
        Boolean accountLocked,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String notes,
        String displayName,
        Boolean isSuperAdmin,
        Boolean isTenantAdmin) {
    public static UserResponse from(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User entity cannot be null");
        }
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole(),
                user.getPhone(),
                user.getJobTitle(),
                user.getDepartment(),
                user.getIsActive(),
                user.getEmailVerified(),
                user.getTwoFactorEnabled(),
                user.getLastLoginAt(),
                user.getLastLoginIp(),
                user.getFailedLoginAttempts(),
                user.isAccountLocked(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getNotes(),
                user.getDisplayName(),
                user.isSuperAdmin(),
                user.isTenantAdmin());
    }
}
