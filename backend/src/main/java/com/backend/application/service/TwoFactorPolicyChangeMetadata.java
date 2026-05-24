package com.backend.application.service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.backend.domain.enums.TwoFactorPolicy;

public final class TwoFactorPolicyChangeMetadata {

    private static final String PREFIX = "two_factor_policy:";
    private static final String USER_AGENT_SEPARATOR = ";user_agent:";

    private TwoFactorPolicyChangeMetadata() {
    }

    public static String format(TwoFactorPolicy policy) {
        return PREFIX + policy.name();
    }

    public static String format(TwoFactorPolicy policy, String userAgent) {
        String formatted = format(policy);
        if (userAgent == null || userAgent.isBlank()) {
            return formatted;
        }
        return formatted + USER_AGENT_SEPARATOR + Base64.getEncoder()
                .encodeToString(userAgent.getBytes(StandardCharsets.UTF_8));
    }

    public static TwoFactorPolicy parse(String metadata) {
        if (metadata == null || !metadata.startsWith(PREFIX)) {
            throw new IllegalStateException("Missing pending two-factor policy metadata on verification token");
        }
        String policyValue = metadata.substring(PREFIX.length());
        int separatorIndex = policyValue.indexOf(USER_AGENT_SEPARATOR);
        if (separatorIndex >= 0) {
            policyValue = policyValue.substring(0, separatorIndex);
        }
        return TwoFactorPolicy.valueOf(policyValue);
    }
}
