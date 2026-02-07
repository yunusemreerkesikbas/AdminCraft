package com.backend.domain.exception;

public class RecaptchaVerificationException extends RuntimeException {

    public RecaptchaVerificationException(String message) {
        super(message);
    }

    public RecaptchaVerificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
