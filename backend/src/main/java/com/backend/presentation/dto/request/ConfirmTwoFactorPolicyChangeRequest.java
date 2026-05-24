package com.backend.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ConfirmTwoFactorPolicyChangeRequest(
        @NotBlank(message = "validation.pendingChangeId.required")
        String pendingChangeId,

        @NotBlank(message = "validation.otpCode.required")
        @Size(min = 6, max = 6, message = "validation.otpCode.size")
        @Pattern(regexp = "\\d{6}", message = "validation.otpCode.pattern")
        String otpCode) {
}
