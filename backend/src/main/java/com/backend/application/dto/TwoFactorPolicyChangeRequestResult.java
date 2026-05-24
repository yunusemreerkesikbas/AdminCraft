package com.backend.application.dto;

import com.backend.domain.enums.TwoFactorPolicy;

public record TwoFactorPolicyChangeRequestResult(
        String pendingChangeId,
        String maskedEmail,
        TwoFactorPolicy targetPolicy,
        int expiresInSeconds,
        int resendCooldownSeconds,
        boolean emailSent) {
}
