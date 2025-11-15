package com.backend.infrastructure.validation;

public interface ValidationRule<T> {
    
    ValidationResult validate(T value, ValidationContext context);
    
    String getRuleName();
}

