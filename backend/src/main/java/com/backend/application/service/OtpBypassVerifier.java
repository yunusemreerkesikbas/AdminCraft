package com.backend.application.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.backend.application.service.config.GlobalRuntimeConfigService;
import com.backend.domain.port.OtpConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OtpBypassVerifier {

    private final GlobalRuntimeConfigService globalRuntimeConfigService;
    private final OtpConfig otpConfig;
    private final Environment environment;

    public boolean isBypassCode(String otpCode) {
        if (isProductionProfile()) {
            return false;
        }

        if (Boolean.TRUE.equals(globalRuntimeConfigService.getOtpBypassEnabled())) {
            String configBypassCode = globalRuntimeConfigService.getOtpBypassCodeDecrypted();
            if (constantTimeEquals(otpCode, configBypassCode)) {
                log.warn("OTP bypass via config panel used — audit this access");
                return true;
            }
        }

        String envBypassCode = otpConfig.getBypassCode();
        return envBypassCode != null && constantTimeEquals(otpCode, envBypassCode);
    }

    private boolean isProductionProfile() {
        return Arrays.asList(environment.getActiveProfiles()).contains("prod");
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        return MessageDigest.isEqual(
                a.getBytes(StandardCharsets.UTF_8),
                b.getBytes(StandardCharsets.UTF_8));
    }
}
