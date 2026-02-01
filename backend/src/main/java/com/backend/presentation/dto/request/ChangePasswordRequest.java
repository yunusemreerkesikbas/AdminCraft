package com.backend.presentation.dto.request;

import com.backend.shared.validation.PasswordMatch;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@PasswordMatch
public record ChangePasswordRequest(
    @NotBlank(message = "{validation.password.required}") String currentPassword,

    @NotBlank(message = "{validation.password.required}") @Size(min = 8, max = 128, message = "{validation.password.size}") String password,

    @NotBlank(message = "{validation.password.confirm.required}") String confirmPassword) {
}
