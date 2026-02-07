package com.backend.presentation.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ForgotPasswordRequest(
        @NotBlank(message = "validation.email.required")
        @Email(message = "validation.email.invalid")
        String email,

        @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "Invalid reCAPTCHA token format")
        String recaptchaToken
) {}
