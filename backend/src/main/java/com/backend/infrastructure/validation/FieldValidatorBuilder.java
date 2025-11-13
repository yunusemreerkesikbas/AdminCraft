package com.backend.infrastructure.validation;

import com.backend.infrastructure.validation.rules.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FieldValidatorBuilder<T> {

  private final List<ValidationRule<T>> rules = new ArrayList<>();

  private FieldValidatorBuilder() {
  }

  public static <T> FieldValidatorBuilder<T> create() {
    return new FieldValidatorBuilder<>();
  }

  @SuppressWarnings("unchecked")
  public FieldValidatorBuilder<T> withKeyFormat(String regex, String errorMessage) {
    rules.add((ValidationRule<T>) new KeyFormatRule(regex, errorMessage));
    return this;
  }

  @SuppressWarnings("unchecked")
  public FieldValidatorBuilder<T> withKeyFormat(String regex) {
    rules.add((ValidationRule<T>) new KeyFormatRule(regex));
    return this;
  }

  @SuppressWarnings("unchecked")
  public FieldValidatorBuilder<T> withCamelCaseKey(int maxLength) {
    rules.add((ValidationRule<T>) KeyFormatRule.camelCase(maxLength));
    return this;
  }

  @SuppressWarnings("unchecked")
  public FieldValidatorBuilder<T> withSnakeCaseKey(int maxLength) {
    rules.add((ValidationRule<T>) KeyFormatRule.snakeCase(maxLength));
    return this;
  }

  @SuppressWarnings("unchecked")
  public FieldValidatorBuilder<T> withReservedKeywords(Set<String> keywords) {
    rules.add((ValidationRule<T>) new ReservedKeywordRule(keywords));
    return this;
  }

  @SuppressWarnings("unchecked")
  public FieldValidatorBuilder<T> withReservedKeywords(Set<String> keywords, boolean caseSensitive) {
    rules.add((ValidationRule<T>) new ReservedKeywordRule(keywords, caseSensitive));
    return this;
  }

  @SuppressWarnings("unchecked")
  public FieldValidatorBuilder<T> withLengthConstraint(Integer minLength, Integer maxLength) {
    rules.add((ValidationRule<T>) new LengthRule(minLength, maxLength));
    return this;
  }

  @SuppressWarnings("unchecked")
  public FieldValidatorBuilder<T> withMaxLength(int maxLength) {
    rules.add((ValidationRule<T>) LengthRule.max(maxLength));
    return this;
  }

  @SuppressWarnings("unchecked")
  public FieldValidatorBuilder<T> withMinLength(int minLength) {
    rules.add((ValidationRule<T>) LengthRule.min(minLength));
    return this;
  }

  @SuppressWarnings("unchecked")
  public FieldValidatorBuilder<T> withRangeConstraint(BigDecimal minValue, BigDecimal maxValue) {
    rules.add((ValidationRule<T>) new RangeRule(minValue, maxValue));
    return this;
  }

  public FieldValidatorBuilder<T> withCountLimit(int maxCount) {
    rules.add(new CountLimitRule<>(maxCount));
    return this;
  }

  public FieldValidatorBuilder<T> withCountLimit(int maxCount, String contextKey) {
    rules.add(new CountLimitRule<>(maxCount, contextKey));
    return this;
  }

  public FieldValidatorBuilder<T> withCustomRule(ValidationRule<T> rule) {
    rules.add(rule);
    return this;
  }

  public FieldValidator<T> build() {
    return new DefaultFieldValidator<>(rules);
  }
}
