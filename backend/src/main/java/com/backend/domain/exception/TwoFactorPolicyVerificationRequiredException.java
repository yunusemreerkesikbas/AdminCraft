package com.backend.domain.exception;

public class TwoFactorPolicyVerificationRequiredException extends RuntimeException {

    public TwoFactorPolicyVerificationRequiredException() {
        super("Two-factor policy change requires email verification");
    }

    public TwoFactorPolicyVerificationRequiredException(String message) {
        super(message);
    }
}
