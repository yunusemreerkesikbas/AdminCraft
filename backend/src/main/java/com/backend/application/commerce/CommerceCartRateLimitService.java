package com.backend.application.commerce;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import org.springframework.stereotype.Component;

import com.backend.domain.port.TenantContextPort;
import com.google.common.util.concurrent.Striped;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CommerceCartRateLimitService {

    private static final long WINDOW_MS = TimeUnit.MINUTES.toMillis(1);
    private static final int MUTATION_LIMIT_PER_MINUTE = 60;
    private static final int READ_LIMIT_PER_MINUTE = 180;

    private final TenantContextPort tenantContext;
    private final Striped<Lock> stripes = Striped.lock(256);
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    public void checkReadOrThrow(String clientIp) {
        enforce("read", clientIp, READ_LIMIT_PER_MINUTE);
    }

    public void checkMutationOrThrow(String clientIp) {
        enforce("mutation", clientIp, MUTATION_LIMIT_PER_MINUTE);
    }

    private void enforce(String scope, String clientIp, int limit) {
        String key = scope + ":" + normalize(tenantContext.getTenantId()) + ":" + normalize(clientIp);
        Lock lock = stripes.get(key);
        lock.lock();
        try {
            long now = System.currentTimeMillis();
            Window window = windows.get(key);
            if (window == null || now - window.windowStartMs >= WINDOW_MS) {
                windows.put(key, new Window(now, 1));
                return;
            }
            if (window.count >= limit) {
                throw new CommerceCartRateLimitExceededException();
            }
            window.count++;
        } finally {
            lock.unlock();
        }
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? "unknown" : value.trim();
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
