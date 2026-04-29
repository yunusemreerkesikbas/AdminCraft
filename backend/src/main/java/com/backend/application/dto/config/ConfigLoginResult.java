package com.backend.application.dto.config;

public record ConfigLoginResult(
        boolean requiresOtp,
        ConfigAuthChallengeResult challenge,
        ConfigAuthResult session
) {
    public static ConfigLoginResult challenge(ConfigAuthChallengeResult challenge) {
        return new ConfigLoginResult(true, challenge, null);
    }

    public static ConfigLoginResult session(ConfigAuthResult session) {
        return new ConfigLoginResult(false, null, session);
    }
}
