package com.backend.domain.commerce.exception;

import com.backend.domain.exception.BusinessRuleViolationException;

public class CommerceDomainException extends BusinessRuleViolationException {

    public CommerceDomainException(String message) {
        super(message);
    }

    public CommerceDomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
