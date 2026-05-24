package com.backend.application.service;

import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.backend.domain.exception.OtpRateLimitExceededException;
import com.backend.domain.port.TenantContextPort;
import com.backend.shared.common.LogSanitizer;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpRateLimitService {

    private static final int OTP_MAX_REQUESTS_PER_WINDOW = 5;
    private static final int OTP_RATE_LIMIT_WINDOW_SECONDS = 300;
    private static final int OTP_RATE_LIMIT_MAX_ENTRIES = 10_000;

    private final TenantContextPort tenantContext;
    private final Cache<String, OtpRateLimitEntry> otpRateLimiters = CacheBuilder.newBuilder()
            .maximumSize(OTP_RATE_LIMIT_MAX_ENTRIES)
            .expireAfterWrite(OTP_RATE_LIMIT_WINDOW_SECONDS, TimeUnit.SECONDS)
            .build();

    public void checkRateLimit(String email) {
        checkRateLimit(email, tenantContext.getTenantId());
    }

    public void checkRateLimit(String email, String tenantKey) {
        String scopeKey = tenantKey != null ? tenantKey : "platform";
        String key = scopeKey + ":" + email.toLowerCase();
        long currentTime = System.currentTimeMillis();
        otpRateLimiters.cleanUp();

        otpRateLimiters.asMap().compute(key, (k, entry) -> {
            if (entry == null || currentTime - entry.windowStart > OTP_RATE_LIMIT_WINDOW_SECONDS * 1000L) {
                return new OtpRateLimitEntry(currentTime, 1);
            }
            entry.requestCount++;
            return entry;
        });

        OtpRateLimitEntry entry = otpRateLimiters.getIfPresent(key);
        if (entry != null && entry.requestCount > OTP_MAX_REQUESTS_PER_WINDOW) {
            long remainingSeconds = OTP_RATE_LIMIT_WINDOW_SECONDS -
                    (currentTime - entry.windowStart) / 1000;
            log.warn("OTP rate limit exceeded for email: {}", LogSanitizer.maskEmail(email));
            throw new OtpRateLimitExceededException(
                    "Too many OTP requests. Please try again later.",
                    (int) Math.max(remainingSeconds, 60));
        }
    }

    private static final class OtpRateLimitEntry {
        private final long windowStart;
        private int requestCount;

        private OtpRateLimitEntry(long windowStart, int requestCount) {
            this.windowStart = windowStart;
            this.requestCount = requestCount;
        }
    }
}
