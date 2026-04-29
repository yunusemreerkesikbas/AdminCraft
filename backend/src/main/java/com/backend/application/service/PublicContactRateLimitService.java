package com.backend.application.service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import org.springframework.stereotype.Component;

import com.backend.infrastructure.config.AppSecurityProperties;
import com.google.common.util.concurrent.Striped;

import lombok.RequiredArgsConstructor;

/**
 * Fixed-window rate limits for the public contact endpoint (per client IP and per tenant).
 */
@Component
@RequiredArgsConstructor
public class PublicContactRateLimitService {

    private static final long WINDOW_MS = TimeUnit.MINUTES.toMillis(1);

    private final AppSecurityProperties appSecurityProperties;
    private final Striped<Lock> ipStripes = Striped.lock(256);
    private final Striped<Lock> tenantStripes = Striped.lock(256);

    private final ConcurrentHashMap<String, Window> ipWindows = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Window> tenantWindows = new ConcurrentHashMap<>();

    public void checkOrThrow(String clientIp, String tenantId) {
        String ipKey = clientIp != null && !clientIp.isBlank() ? clientIp : "unknown";
        String tenantKey = tenantId != null && !tenantId.isBlank() ? tenantId : "unknown";
        enforce(ipKey, ipWindows, ipStripes, appSecurityProperties.getPublicContactPerIpPerMinute());
        enforce(tenantKey, tenantWindows, tenantStripes, appSecurityProperties.getPublicContactPerTenantPerMinute());
    }

    private static void enforce(String key, ConcurrentHashMap<String, Window> map, Striped<Lock> stripes, int limit) {
        Lock lock = stripes.get(key);
        lock.lock();
        try {
            long now = System.currentTimeMillis();
            Window w = map.get(key);
            if (w == null || now - w.windowStartMs >= WINDOW_MS) {
                map.put(key, new Window(now, 1));
                return;
            }
            if (w.count >= limit) {
                throw new PublicContactRateLimitExceededException();
            }
            w.count++;
        } finally {
            lock.unlock();
        }
    }

    private static final class Window {
        private final long windowStartMs;
        private int count;

        private Window(long windowStartMs, int count) {
            this.windowStartMs = windowStartMs;
            this.count = count;
        }
    }
}
