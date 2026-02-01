package com.backend.application.dto;

import com.backend.domain.enums.UserRole;

public record UpdateUserInput(
        String email,
        UserRole role,
        String firstName,
        String lastName,
        String phone,
        String jobTitle,
        String department,
        Boolean isActive,
        String notes) {
}
