package com.backend.infrastructure.validation.rules;

import com.backend.infrastructure.validation.ValidationContext;
import com.backend.infrastructure.validation.ValidationResult;
import com.backend.infrastructure.validation.ValidationRule;

public class LengthRule implements ValidationRule<String> {

  private final Integer minLength;
  private final Integer maxLength;

  public LengthRule(Integer minLength, Integer maxLength) {
    this.minLength = minLength;
    this.maxLength = maxLength;
  }

  @Override
  public ValidationResult validate(String value, ValidationContext context) {
    if (value == null) {
      return ValidationResult.success();
    }

    int length = value.length();

    if (minLength != null && length < minLength) {
      return ValidationResult.failure("Length must be at least " + minLength + " characters");
    }

    if (maxLength != null && length > maxLength) {
      return ValidationResult.failure("Length must not exceed " + maxLength + " characters");
    }

    return ValidationResult.success();
  }

  @Override
  public String getRuleName() {
    return "Length";
  }

  public static LengthRule max(int maxLength) {
    return new LengthRule(null, maxLength);
  }

  public static LengthRule min(int minLength) {
    return new LengthRule(minLength, null);
  }

  public static LengthRule between(int minLength, int maxLength) {
    return new LengthRule(minLength, maxLength);
  }
}
