package com.backend.infrastructure.validation.rules;

import com.backend.infrastructure.validation.ValidationContext;
import com.backend.infrastructure.validation.ValidationResult;
import com.backend.infrastructure.validation.ValidationRule;

import java.util.Set;

public class ReservedKeywordRule implements ValidationRule<String> {

  private final Set<String> reservedKeywords;
  private final boolean caseSensitive;

  public ReservedKeywordRule(Set<String> reservedKeywords, boolean caseSensitive) {
    this.reservedKeywords = caseSensitive ? reservedKeywords
        : reservedKeywords.stream().map(String::toLowerCase).collect(java.util.stream.Collectors.toSet());
    this.caseSensitive = caseSensitive;
  }

  public ReservedKeywordRule(Set<String> reservedKeywords) {
    this(reservedKeywords, false);
  }

  @Override
  public ValidationResult validate(String value, ValidationContext context) {
    if (value == null) {
      return ValidationResult.success();
    }

    String checkValue = caseSensitive ? value : value.toLowerCase();

    if (reservedKeywords.contains(checkValue)) {
      return ValidationResult.failure("Key is a reserved keyword: " + value);
    }

    return ValidationResult.success();
  }

  @Override
  public String getRuleName() {
    return "ReservedKeyword";
  }
}
