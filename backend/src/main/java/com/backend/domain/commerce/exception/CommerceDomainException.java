package com.backend.domain.commerce.exception;

public class CommerceDomainException extends RuntimeException {

    public CommerceDomainException(String message) {
        super(message);
    }

    public CommerceDomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
