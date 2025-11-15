package com.backend.infrastructure.validation;

import java.util.ArrayList;
import java.util.List;

public class DefaultFieldValidator<T> implements FieldValidator<T> {

  private final List<ValidationRule<T>> rules;

  public DefaultFieldValidator(List<ValidationRule<T>> rules) {
    this.rules = new ArrayList<>(rules);
  }

  @Override
  public ValidationResult validate(T field) {
    return validate(field, ValidationContext.empty());
  }

  @Override
  public ValidationResult validate(T field, ValidationContext context) {
    List<String> errors = new ArrayList<>();

    for (ValidationRule<T> rule : rules) {
      ValidationResult result = rule.validate(field, context);
      if (!result.isValid()) {
        errors.addAll(result.getErrors());
      }
    }

    return errors.isEmpty() ? ValidationResult.success() : ValidationResult.failure(errors);
  }

  public void addRule(ValidationRule<T> rule) {
    rules.add(rule);
  }
}
