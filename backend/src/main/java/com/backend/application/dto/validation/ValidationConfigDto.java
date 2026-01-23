package com.backend.application.dto.validation;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for validation configuration of product attributes.
 * Supports different validation rules based on field type:
 * - TEXT/RICHTEXT: minLength, maxLength, pattern (regex)
 * - NUMBER: minValue, maxValue
 * - DATE: minDate, maxDate
 */
public record ValidationConfigDto(
    Integer minLength,
    Integer maxLength,
    String pattern,
    BigDecimal minValue,
    BigDecimal maxValue,
    LocalDate minDate,
    LocalDate maxDate
) {
    public ValidationConfigDto {
        // Validate minLength <= maxLength if both present
        if (minLength != null && maxLength != null && minLength > maxLength) {
            throw new IllegalArgumentException("minLength cannot be greater than maxLength");
        }
        // Validate minValue <= maxValue if both present
        if (minValue != null && maxValue != null && minValue.compareTo(maxValue) > 0) {
            throw new IllegalArgumentException("minValue cannot be greater than maxValue");
        }
        // Validate minDate <= maxDate if both present
        if (minDate != null && maxDate != null && minDate.isAfter(maxDate)) {
            throw new IllegalArgumentException("minDate cannot be after maxDate");
        }
    }
}
