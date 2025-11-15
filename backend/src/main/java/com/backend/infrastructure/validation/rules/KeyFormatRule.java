package com.backend.infrastructure.validation.rules;

import com.backend.infrastructure.validation.ValidationContext;
import com.backend.infrastructure.validation.ValidationResult;
import com.backend.infrastructure.validation.ValidationRule;

import java.util.regex.Pattern;

public class KeyFormatRule implements ValidationRule<String> {

  private final Pattern pattern;
  private final String errorMessage;

  public KeyFormatRule(String regex, String errorMessage) {
    this.pattern = Pattern.compile(regex);
    this.errorMessage = errorMessage;
  }

  public KeyFormatRule(String regex) {
    this(regex, "Invalid key format. Must match pattern: " + regex);
  }

  @Override
  public ValidationResult validate(String value, ValidationContext context) {
    if (value == null || value.isBlank()) {
      return ValidationResult.failure("Key cannot be empty");
    }

    if (!pattern.matcher(value).matches()) {
      return ValidationResult.failure(errorMessage);
    }

    return ValidationResult.success();
  }

  @Override
  public String getRuleName() {
    return "KeyFormat";
  }

  public static KeyFormatRule camelCase(int maxLength) {
    return new KeyFormatRule(
        "^[a-z][a-zA-Z0-9]{0," + (maxLength - 1) + "}$",
        "Key must start with lowercase letter and contain only alphanumeric characters (max " + maxLength + " chars)");
  }

  public static KeyFormatRule snakeCase(int maxLength) {
    return new KeyFormatRule(
        "^[a-z][a-z0-9_]{0," + (maxLength - 1) + "}$",
        "Key must be in snake_case format (max " + maxLength + " chars)");
  }
}
