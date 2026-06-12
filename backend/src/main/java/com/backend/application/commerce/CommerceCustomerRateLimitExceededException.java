package com.backend.application.commerce;

public class CommerceCustomerRateLimitExceededException extends RuntimeException {

	private final String rateLimitName;
	private final long retryAfterSeconds;

	public CommerceCustomerRateLimitExceededException(String rateLimitName, long retryAfterSeconds, String message) {
		super(message);
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
