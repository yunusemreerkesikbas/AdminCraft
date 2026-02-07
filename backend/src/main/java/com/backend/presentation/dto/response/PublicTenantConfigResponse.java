package com.backend.presentation.dto.response;

import com.backend.application.dto.PublicTenantConfigResult;

import java.math.BigDecimal;

public record PublicTenantConfigResponse(
    RecaptchaConfigDto recaptcha
) {
    public record RecaptchaConfigDto(
        Boolean enabled,
        String siteKey,
        BigDecimal threshold
    ) {}

    public static PublicTenantConfigResponse from(PublicTenantConfigResult result) {
        return new PublicTenantConfigResponse(
            new RecaptchaConfigDto(
                result.recaptcha().enabled(),
                result.recaptcha().siteKey(),
                result.recaptcha().threshold()
            )
        );
    }
}
