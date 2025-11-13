package com.backend.infrastructure.validation;

import java.util.ArrayList;
import java.util.List;

public class ValidationResult {

  private final boolean valid;
  private final List<String> errors;

  private ValidationResult(boolean valid, List<String> errors) {
    this.valid = valid;
    this.errors = errors;
  }

  public static ValidationResult success() {
    return new ValidationResult(true, List.of());
  }

  public static ValidationResult failure(String error) {
    return new ValidationResult(false, List.of(error));
  }

  public static ValidationResult failure(List<String> errors) {
    return new ValidationResult(false, new ArrayList<>(errors));
  }

  public boolean isValid() {
    return valid;
  }

  public List<String> getErrors() {
    return errors;
  }

  public String getFirstError() {
    return errors.isEmpty() ? null : errors.get(0);
  }

  public void throwIfInvalid() {
    if (!valid) {
      throw new ValidationException(errors.get(0));
    }
  }
}
