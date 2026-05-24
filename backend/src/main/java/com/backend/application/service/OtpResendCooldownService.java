package com.backend.application.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.backend.application.service.config.ConfigPropertyService;
import com.backend.domain.entity.Tenant;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OtpResendCooldownService {

    public static final String CONFIG_KEY = "security.otp.resend_cooldown_seconds";
    public static final int DEFAULT_COOLDOWN_SECONDS = 180;
    public static final int MIN_COOLDOWN_SECONDS = 60;
    public static final int MAX_COOLDOWN_SECONDS = 3600;

    private final ConfigPropertyService configPropertyService;

    @Value("${app.security.otp.resend-cooldown-seconds:180}")
    private int platformCooldownSeconds;

    public int resolveTenantCooldownSeconds(Long tenantId, String tenantDbName) {
        if (tenantId == null || tenantDbName == null || tenantDbName.isBlank()) {
            return DEFAULT_COOLDOWN_SECONDS;
        }
        return configPropertyService.findRaw(tenantId, tenantDbName, CONFIG_KEY)
                .map(this::parseCooldown)
                .orElse(DEFAULT_COOLDOWN_SECONDS);
    }

    public int resolveTenantCooldownSeconds(Tenant tenant) {
        if (tenant == null) {
            return DEFAULT_COOLDOWN_SECONDS;
        }
        return resolveTenantCooldownSeconds(tenant.getId(), tenant.getDatabaseName());
    }

    public int resolvePlatformCooldownSeconds() {
        return clampCooldown(platformCooldownSeconds);
    }

    private int parseCooldown(String raw) {
        if (raw == null || raw.isBlank()) {
            return DEFAULT_COOLDOWN_SECONDS;
        }
        try {
            return clampCooldown(Integer.parseInt(raw.trim()));
        } catch (NumberFormatException ex) {
            return DEFAULT_COOLDOWN_SECONDS;
        }
    }

    private int clampCooldown(int seconds) {
        return Math.max(MIN_COOLDOWN_SECONDS, Math.min(MAX_COOLDOWN_SECONDS, seconds));
    }
}
