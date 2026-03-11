package com.backend.presentation.dto.request.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ConfigVerifyOtpRequest(
        @NotBlank(message = "validation.pending.token.required")
        String pendingToken,

        @NotBlank(message = "validation.otp.required")
        @Size(min = 6, max = 6, message = "validation.otp.size")
        @Pattern(regexp = "^\\d{6}$", message = "validation.otp.pattern")
        String otpCode,

        Long tenantId,

        @Size(max = 50, message = "validation.subdomain.size")
        String subdomain
) {
}
