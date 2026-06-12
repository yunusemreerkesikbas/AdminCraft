package com.backend.application.commerce;

import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import org.springframework.stereotype.Service;

import com.backend.domain.port.TenantContextPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommerceCustomerRateLimitService {

	private static final long WINDOW_MS = TimeUnit.MINUTES.toMillis(1);
	private static final int LOGIN_LIMIT = 5;
	private static final int REGISTER_LIMIT = 3;
	private static final int CLEANUP_INTERVAL_SIZE = 512;

	private final TenantContextPort tenantContext;
	private final ConcurrentHashMap<RateLimitKey, Window> windows = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<RateLimitKey, Lock> stripes = new ConcurrentHashMap<>();

	public void checkLoginOrThrow(String email, String clientIp) {
		checkOrThrow("commerce.customer.login", email, clientIp, LOGIN_LIMIT);
	}

	public void checkRegisterOrThrow(String email, String clientIp) {
		checkOrThrow("commerce.customer.register", email, clientIp, REGISTER_LIMIT);
	}

	private void checkOrThrow(String scope, String email, String clientIp, int limit) {
		RateLimitKey key = new RateLimitKey(scope, normalize(tenantContext.getTenantId()), normalize(email), normalize(clientIp));
		Lock lock = stripes.computeIfAbsent(key, ignored -> new java.util.concurrent.locks.ReentrantLock());
		long now = System.currentTimeMillis();
		lock.lock();
		try {
			Window window = windows.compute(key, (ignored, current) -> {
				if (current == null || now - current.windowStartMs >= WINDOW_MS) {
					return new Window(now, 1);
				}
				return new Window(current.windowStartMs, current.count + 1);
			});
			if (window.count > limit) {
				long retryAfterSeconds = Math.max(1, TimeUnit.MILLISECONDS.toSeconds(WINDOW_MS - (now - window.windowStartMs)));
				throw new CommerceCustomerRateLimitExceededException(scope, retryAfterSeconds, "commerce.customer.rate.limit.exceeded");
			}
		} finally {
			lock.unlock();
		}
		cleanupExpiredWindowsIfNeeded();
	}

	private void cleanupExpiredWindowsIfNeeded() {
		if (windows.size() < CLEANUP_INTERVAL_SIZE) {
			return;
		}
		long now = System.currentTimeMillis();
		windows.forEach((key, window) -> {
			if (now - window.windowStartMs >= WINDOW_MS) {
				windows.remove(key, window);
				stripes.remove(key);
			}
		});
	}

	private String normalize(String value) {
		if (value == null || value.isBlank()) {
			return "unknown";
		}
		return value.trim().toLowerCase(Locale.ROOT);
	}

	private record RateLimitKey(String scope, String tenantId, String email, String clientIp) {
	}

	private record Window(long windowStartMs, int count) {
	}
}
