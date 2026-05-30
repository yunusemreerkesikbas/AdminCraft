package com.backend.application.service;

import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.backend.domain.exception.OtpRateLimitExceededException;
import com.backend.shared.common.LogSanitizer;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OtpRateLimitService {

    private static final int CACHE_MAX_ENTRIES = 10_000;
    private static final int CACHE_TTL_SECONDS = 3600;

    private final Cache<String, Long> lastOtpSendAt = CacheBuilder.newBuilder()
            .maximumSize(CACHE_MAX_ENTRIES)
            .expireAfterWrite(CACHE_TTL_SECONDS, TimeUnit.SECONDS)
            .build();

    public void enforceResendCooldown(String email, String scopeKey, int cooldownSeconds) {
        if (email == null || email.isBlank()) {
            return;
        }
        if (cooldownSeconds <= 0) {
            cooldownSeconds = OtpResendCooldownService.DEFAULT_COOLDOWN_SECONDS;
        }

        String key = buildKey(email, scopeKey);
        Long lastSentAt = lastOtpSendAt.getIfPresent(key);
        if (lastSentAt == null) {
            return;
        }

        long elapsedMs = System.currentTimeMillis() - lastSentAt;
        long cooldownMs = cooldownSeconds * 1000L;
        if (elapsedMs >= cooldownMs) {
            return;
        }

        int retryAfterSeconds = (int) Math.max(1, (cooldownMs - elapsedMs + 999) / 1000);
        log.warn("OTP resend cooldown active for email: {}", LogSanitizer.maskEmail(email));
        throw new OtpRateLimitExceededException(
                "Too many OTP requests. Please try again later.",
                retryAfterSeconds);
    }

    public void recordOtpSend(String email, String scopeKey) {
        if (email == null || email.isBlank()) {
            return;
        }
        lastOtpSendAt.put(buildKey(email, scopeKey), System.currentTimeMillis());
    }

    private String buildKey(String email, String scopeKey) {
        String scope = scopeKey != null && !scopeKey.isBlank() ? scopeKey : "platform";
        return scope + ":" + email.trim().toLowerCase();
    }
}
