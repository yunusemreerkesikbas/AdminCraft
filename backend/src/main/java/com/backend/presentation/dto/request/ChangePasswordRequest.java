package com.backend.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
    @NotBlank(message = "Current password is required")
    String currentPassword,
    
    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "New password must be at least 8 characters")
    String newPassword,
    
    @NotBlank(message = "Confirm password is required")
    String confirmPassword
) {
    public ChangePasswordRequest {
        if (currentPassword != null && currentPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Current password cannot be empty");
        }
        if (newPassword != null && newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("New password cannot be empty");
        }
        if (confirmPassword != null && confirmPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Confirm password cannot be empty");
        }
        if (newPassword != null && confirmPassword != null && !newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("New password and confirm password must match");
        }
    }
}