package com.backend.application.dto;

import com.backend.domain.enums.UserRole;

public record CreateUserInput(
        String email,
        String password,
        String fullName,
        UserRole role,
        String firstName,
        String lastName,
        String phone,
        String jobTitle,
        String department,
        Boolean isActive,
        String notes) {

    public CreateUserInput {
        if (isActive == null) {
            isActive = true;
        }
    }
}
