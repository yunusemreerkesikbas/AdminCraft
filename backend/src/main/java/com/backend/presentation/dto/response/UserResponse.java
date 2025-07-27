package com.backend.presentation.dto.response;

import com.backend.domain.enums.Language;
import com.backend.domain.enums.UserRole;

import java.time.LocalDateTime;

public record UserResponse(
    Long id,
    String email,
    String fullName,
    UserRole role,
    Language preferredLanguage,
    Long tenantId,
    String tenantName,
    Boolean isActive,
    Boolean isEmailVerified,
    Boolean isTwoFactorEnabled,
    Boolean isLocked,
    String phone,
    String jobTitle,
    String department,
    String timezone,
    String avatarUrl,
    LocalDateTime lastLoginAt,
    LocalDateTime lockedUntil,
    Integer failedLoginAttempts,
    LocalDateTime passwordChangedAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}