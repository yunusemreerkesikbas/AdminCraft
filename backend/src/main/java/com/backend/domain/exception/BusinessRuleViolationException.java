package com.backend.domain.exception;

/**
 * Exception thrown when a business rule is violated.
 * This results in HTTP 409 CONFLICT response.
 */
public class BusinessRuleViolationException extends RuntimeException {

  public BusinessRuleViolationException(String message) {
    super(message);
  }

  public BusinessRuleViolationException(String message, Throwable cause) {
    super(message, cause);
  }
}
