package com.backend.domain.exception;

public class ComponentConflictException extends RuntimeException {
  public ComponentConflictException(String message) {
    super(message);
  }
}
