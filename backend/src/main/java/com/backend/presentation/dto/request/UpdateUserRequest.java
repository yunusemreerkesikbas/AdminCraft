package com.backend.presentation.dto.request;

import com.backend.domain.enums.Language;
import com.backend.domain.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email,
    
    @NotBlank(message = "Full name is required")
    @Size(max = 255, message = "Full name must not exceed 255 characters")
    String fullName,
    
    @NotNull(message = "Role is required")
    UserRole role,
    
    @NotNull(message = "Preferred language is required")
    Language preferredLanguage,
    
    String phone,
    String jobTitle,
    String department,
    String timezone,
    Boolean isActive
) {
    public UpdateUserRequest {
        if (email != null && email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (fullName != null && fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name cannot be empty");
        }
    }
}