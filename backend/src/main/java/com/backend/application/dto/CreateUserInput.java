package com.backend.application.dto;

import com.backend.domain.enums.Language;
import com.backend.domain.enums.UserRole;

public record CreateUserInput(
        String email,
        UserRole role,
        String firstName,
        String lastName,
        String phone,
        String jobTitle,
        String department,
        Boolean isActive,
        String notes,
        String ipAddress,
        String userAgent,
        Language language) {

    public CreateUserInput {
        if (isActive == null) {
            isActive = true;
        }
        if (language == null) {
            language = Language.TR;
        }
    }

    public CreateUserInput(
            String email,
            UserRole role,
            String firstName,
            String lastName,
            String phone,
            String jobTitle,
            String department,
            Boolean isActive,
            String notes) {
        this(email, role, firstName, lastName, phone, jobTitle, department, isActive, notes, null, null, Language.TR);
    }
}
