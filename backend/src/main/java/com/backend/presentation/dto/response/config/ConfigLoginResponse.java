package com.backend.presentation.dto.response.config;

import com.backend.application.dto.config.ConfigLoginResult;

public record ConfigLoginResponse(
        boolean requiresOtp,
        ConfigAuthChallengeResponse challenge,
        ConfigAuthResponse session
) {
    public static ConfigLoginResponse from(ConfigLoginResult result) {
        return new ConfigLoginResponse(
                result.requiresOtp(),
                result.challenge() != null ? ConfigAuthChallengeResponse.from(result.challenge()) : null,
                result.session() != null ? ConfigAuthResponse.from(result.session()) : null);
    }
}
