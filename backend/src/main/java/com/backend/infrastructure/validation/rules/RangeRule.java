package com.backend.infrastructure.validation.rules;

import com.backend.infrastructure.validation.ValidationContext;
import com.backend.infrastructure.validation.ValidationResult;
import com.backend.infrastructure.validation.ValidationRule;

import java.math.BigDecimal;

public class RangeRule implements ValidationRule<BigDecimal> {

  private final BigDecimal minValue;
  private final BigDecimal maxValue;

  public RangeRule(BigDecimal minValue, BigDecimal maxValue) {
    this.minValue = minValue;
    this.maxValue = maxValue;
  }

  @Override
  public ValidationResult validate(BigDecimal value, ValidationContext context) {
    if (value == null) {
      return ValidationResult.success();
    }

    if (minValue != null && maxValue != null) {
      if (minValue.compareTo(maxValue) > 0) {
        return ValidationResult.failure("Min value cannot be greater than max value");
      }
    }

    if (minValue != null && value.compareTo(minValue) < 0) {
      return ValidationResult.failure("Value must be at least " + minValue);
    }

    if (maxValue != null && value.compareTo(maxValue) > 0) {
      return ValidationResult.failure("Value must not exceed " + maxValue);
    }

    return ValidationResult.success();
  }

  @Override
  public String getRuleName() {
    return "Range";
  }

  public static RangeRule between(BigDecimal min, BigDecimal max) {
    return new RangeRule(min, max);
  }

  public static RangeRule min(BigDecimal min) {
    return new RangeRule(min, null);
  }

  public static RangeRule max(BigDecimal max) {
    return new RangeRule(null, max);
  }
}
