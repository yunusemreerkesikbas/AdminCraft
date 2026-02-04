package com.backend.presentation.dto.request;

import com.backend.shared.constants.ValidationConstants;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record VerifyOtpRequest(
        @NotBlank(message = "validation.pending.token.required")
        String pendingToken,

        @NotBlank(message = "validation.otp.required")
        @Size(min = 6, max = 6, message = "validation.otp.size")
        @Pattern(regexp = "^\\d{6}$", message = "validation.otp.pattern")
        String otpCode,

        boolean trustDevice,

        @Size(max = 128, message = "validation.device.fingerprint.size")
        @Pattern(regexp = ValidationConstants.DEVICE_FINGERPRINT_PATTERN, message = "validation.device.fingerprint.pattern")
        String deviceFingerprint,

        @Size(max = 100, message = "validation.device.name.size")
        String deviceName,

        Long tenantId,

        String subdomain
) {}
