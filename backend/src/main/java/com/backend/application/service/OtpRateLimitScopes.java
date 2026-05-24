package com.backend.application.service;

/**
 * Isolates LOGIN_OTP and OPERATION_OTP cooldown buckets per tenant/platform.
 * Same {@link OtpResendCooldownService} duration applies to both; only the rate-limit key differs.
 */
public final class OtpRateLimitScopes {

    private OtpRateLimitScopes() {
    }

    public static String tenantLogin(long tenantId) {
        return tenantId + ":login";
    }

    public static String tenantOperation(long tenantId) {
        return tenantId + ":operation";
    }

    public static final String PLATFORM_LOGIN = "platform:login";

    public static final String PLATFORM_OPERATION = "platform:operation";
}
