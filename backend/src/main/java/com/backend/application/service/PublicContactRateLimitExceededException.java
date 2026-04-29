package com.backend.application.service;

/**
 * Thrown when the public contact endpoint exceeds configured per-IP or per-tenant limits.
 */
public final class PublicContactRateLimitExceededException extends RuntimeException {

    private static final long serialVersionUID = 1L;
}
