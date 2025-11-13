package com.backend.infrastructure.validation;

public interface FieldValidator<T> {

  ValidationResult validate(T field);

  ValidationResult validate(T field, ValidationContext context);
}
