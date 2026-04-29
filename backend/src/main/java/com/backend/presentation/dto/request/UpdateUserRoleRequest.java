package com.backend.presentation.dto.request;

import com.backend.domain.enums.UserRole;

import jakarta.validation.constraints.NotNull;

public record UpdateUserRoleRequest(
        @NotNull UserRole role
) {}
