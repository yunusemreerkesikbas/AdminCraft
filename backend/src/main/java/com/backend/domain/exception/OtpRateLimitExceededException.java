package com.backend.domain.exception;

public class OtpRateLimitExceededException extends RuntimeException {

    private final int retryAfterSeconds;

    public OtpRateLimitExceededException(String message) {
        super(message);
        this.retryAfterSeconds = 60; // Default 1 minute
    }

    public OtpRateLimitExceededException(String message, int retryAfterSeconds) {
        super(message);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public int getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
