package com.backend.application.service;

import com.backend.domain.enums.TwoFactorPolicy;

public final class TwoFactorPolicyChangeMetadata {

    private static final String PREFIX = "two_factor_policy:";

    private TwoFactorPolicyChangeMetadata() {
    }

    public static String format(TwoFactorPolicy policy) {
        return PREFIX + policy.name();
    }

    public static TwoFactorPolicy parse(String metadata) {
        if (metadata == null || !metadata.startsWith(PREFIX)) {
            throw new IllegalStateException("Missing pending two-factor policy metadata on verification token");
        }
        return TwoFactorPolicy.valueOf(metadata.substring(PREFIX.length()));
    }
}
