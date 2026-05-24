package com.backend.application.service;

import com.backend.domain.enums.TwoFactorPolicy;

public final class TwoFactorPolicyChangeMetadata {

    private static final String PREFIX = "two_factor_policy:";

    private TwoFactorPolicyChangeMetadata() {
    }

    public static String format(TwoFactorPolicy policy) {
        return PREFIX + policy.name();
    }

    public static TwoFactorPolicy parse(String userAgent) {
        if (userAgent == null || !userAgent.startsWith(PREFIX)) {
            throw new IllegalStateException("Missing pending two-factor policy metadata on verification token");
        }
        return TwoFactorPolicy.valueOf(userAgent.substring(PREFIX.length()));
    }
}
