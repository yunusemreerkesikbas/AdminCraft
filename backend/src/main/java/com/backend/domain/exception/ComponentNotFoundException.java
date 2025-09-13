package com.backend.domain.exception;

public class ComponentNotFoundException extends RuntimeException {
  public ComponentNotFoundException(String message) {
    super(message);
  }
}
