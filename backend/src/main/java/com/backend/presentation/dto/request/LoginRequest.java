package com.backend.presentation.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "Email is required") @Email(message = "Please provide a valid email address") String email,

        @NotBlank(message = "Password is required") @Size(min = 6, message = "Password must be at least 6 characters long") String password,
        @Pattern(regexp = "^[a-z0-9]+(-[a-z0-9]+)*$", message = "Subdomain must contain only lowercase letters, numbers, and hyphens") String subdomain) {
    public LoginRequest {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
    }
}