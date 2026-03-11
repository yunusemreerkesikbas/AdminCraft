package com.backend.infrastructure.email;

import org.springframework.stereotype.Component;

import com.backend.domain.port.OtpConfig;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OtpConfigImpl implements OtpConfig {

    private final OtpProperties otpProperties;

    @Override
    public int getExpirySeconds() {
        return otpProperties.getExpirySeconds();
    }

    @Override
    public int getMaxAttempts() {
        return otpProperties.getMaxAttempts();
    }

    @Override
    public String getBypassCode() {
        return otpProperties.getBypassCode();
    }
}
