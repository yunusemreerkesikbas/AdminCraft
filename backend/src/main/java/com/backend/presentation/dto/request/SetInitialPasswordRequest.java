package com.backend.presentation.dto.request;

import com.backend.shared.constants.ValidationConstants;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SetInitialPasswordRequest(
        @NotBlank(message = "validation.token.required")
        String token,

        @NotBlank(message = "validation.password.required")
        @Size(min = 8, max = 128, message = "validation.password.size")
        @Pattern(regexp = ValidationConstants.PASSWORD_PATTERN, message = "validation.password.pattern")
        String password,

        @NotBlank(message = "validation.password.confirm.required")
        String confirmPassword,

        // Optional fields for trusted device
        @Size(max = 128, message = "validation.device.fingerprint.size")
        String deviceFingerprint,

        Boolean trustDevice,

        @Size(max = 100, message = "validation.device.name.size")
        String deviceName,

        @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "Invalid reCAPTCHA token format")
        String recaptchaToken
) {
    @AssertTrue(message = "validation.password.mismatch")
    public boolean isPasswordMatching() {
        return password != null && password.equals(confirmPassword);
    }
}
