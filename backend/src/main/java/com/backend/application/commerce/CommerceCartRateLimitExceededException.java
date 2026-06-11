package com.backend.application.commerce;

public final class CommerceCartRateLimitExceededException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String rateLimitName;
    private final long retryAfterSeconds;

    public CommerceCartRateLimitExceededException(String message) {
		this(null, 0, message, null);
    }

    public CommerceCartRateLimitExceededException(String message, Throwable cause) {
		this(null, 0, message, cause);
    }

    public CommerceCartRateLimitExceededException(String rateLimitName, long retryAfterSeconds, String message) {
		this(rateLimitName, retryAfterSeconds, message, null);
    }

    private CommerceCartRateLimitExceededException(
			String rateLimitName,
			long retryAfterSeconds,
			String message,
			Throwable cause) {
		super(message, cause);
		this.rateLimitName = rateLimitName;
		this.retryAfterSeconds = retryAfterSeconds;
    }

    public String getRateLimitName() {
		return rateLimitName;
    }

    public long getRetryAfterSeconds() {
		return retryAfterSeconds;
    }
}
