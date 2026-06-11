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
    private static final int CLEANUP_INTERVAL_SIZE = 512;

    private final TenantContextPort tenantContext;
    private final Striped<Lock> stripes = Striped.lock(256);
    private final ConcurrentHashMap<RateLimitKey, Window> windows = new ConcurrentHashMap<>();

    public void checkReadOrThrow(String clientIp) {
        enforce("read", clientIp, READ_LIMIT_PER_MINUTE);
    }

    public void checkMutationOrThrow(String clientIp) {
        enforce("mutation", clientIp, MUTATION_LIMIT_PER_MINUTE);
    }

    private void enforce(String scope, String clientIp, int limit) {
		RateLimitKey key = new RateLimitKey(scope, normalize(tenantContext.getTenantId()), normalize(clientIp));
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
				long retryAfterSeconds = Math.max(1, TimeUnit.MILLISECONDS.toSeconds(WINDOW_MS - (now - window.windowStartMs)));
				throw new CommerceCartRateLimitExceededException(scope, retryAfterSeconds, "commerce.cart.rate.limit.exceeded");
            }
            window.count++;
        } finally {
            lock.unlock();
        }
		cleanupExpiredWindowsIfNeeded();
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? "unknown" : value.trim();
    }

    private void cleanupExpiredWindowsIfNeeded() {
		if (windows.size() < CLEANUP_INTERVAL_SIZE) {
			return;
		}
		long now = System.currentTimeMillis();
		windows.forEach((key, window) -> {
			if (now - window.windowStartMs < WINDOW_MS) {
				return;
			}
			Lock lock = stripes.get(key);
			lock.lock();
			try {
				Window current = windows.get(key);
				if (current != null && now - current.windowStartMs >= WINDOW_MS) {
					windows.remove(key, current);
				}
			} finally {
				lock.unlock();
			}
		});
    }

    private record RateLimitKey(String scope, String tenantId, String clientIp) {
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
