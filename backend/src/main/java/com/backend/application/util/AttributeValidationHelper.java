package com.backend.application.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.backend.application.dto.validation.ValidationConfigDto;
import com.backend.domain.enums.ProductFieldType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for validating product attribute values based on their
 * validation configuration.
 */
@Slf4j
public final class AttributeValidationHelper {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private AttributeValidationHelper() {
        // Utility class - prevent instantiation
    }

    /**
     * Parses a JSON validation config string into a ValidationConfigDto.
     *
     * @param validationConfigJson JSON string containing validation config
     * @return ValidationConfigDto or null if parsing fails or input is null/empty
     */
    public static ValidationConfigDto parseValidationConfig(String validationConfigJson) {
        if (validationConfigJson == null || validationConfigJson.trim().isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(validationConfigJson, ValidationConfigDto.class);
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse validation config: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Validates a value against the validation configuration based on field type.
     *
     * @param value           The value to validate
     * @param fieldType       The field type (TEXT, RICHTEXT, NUMBER, DATE, etc.)
     * @param config          The validation configuration
     * @param attributeName   The attribute name for error messages
     * @return List of validation error messages (empty if valid)
     */
    public static List<String> validate(Object value, ProductFieldType fieldType, ValidationConfigDto config, String attributeName) {
        return validate(value, fieldType, config, attributeName, false);
    }

    /**
     * Validates a value against the validation configuration based on field type,
     * with required field support.
     *
     * @param value           The value to validate
     * @param fieldType       The field type (TEXT, RICHTEXT, NUMBER, DATE, etc.)
     * @param config          The validation configuration
     * @param attributeName   The attribute name for error messages
     * @param isRequired      Whether the field is required
     * @return List of validation error messages (empty if valid)
     */
    public static List<String> validate(Object value, ProductFieldType fieldType, ValidationConfigDto config,
            String attributeName, boolean isRequired) {
        List<String> errors = new ArrayList<>();

        // Check required field validation first
        if (isRequired && isBlankValue(value)) {
            errors.add(String.format("Attribute '%s' is required", attributeName));
            return errors;
        }

        // Skip further validation if value is null/blank
        if (isBlankValue(value)) {
            return errors;
        }

        // Skip config-based validation if no config provided
        if (config == null) {
            return errors;
        }

        switch (fieldType) {
            case TEXT, RICHTEXT -> validateText(value, config, attributeName, errors);
            case NUMBER -> validateNumber(value, config, attributeName, errors);
            case DATE -> validateDate(value, config, attributeName, errors);
            default -> {
                // BOOLEAN, MEDIA - no validation config needed
            }
        }

        return errors;
    }

    /**
     * Checks if a value is blank (null, empty string, or whitespace only).
     */
    private static boolean isBlankValue(Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof String str) {
            return str.isBlank();
        }
        return false;
    }

    private static void validateText(Object value, ValidationConfigDto config, String attributeName, List<String> errors) {
        String text = String.valueOf(value);

        if (config.minLength() != null && text.length() < config.minLength()) {
            errors.add(String.format("Attribute '%s' must have at least %d characters (current: %d)",
                    attributeName, config.minLength(), text.length()));
        }

        if (config.maxLength() != null && text.length() > config.maxLength()) {
            errors.add(String.format("Attribute '%s' must have at most %d characters (current: %d)",
                    attributeName, config.maxLength(), text.length()));
        }

        if (config.pattern() != null && !config.pattern().isEmpty()) {
            try {
                if (!Pattern.matches(config.pattern(), text)) {
                    errors.add(String.format("Attribute '%s' does not match the required pattern",
                            attributeName));
                }
            } catch (PatternSyntaxException e) {
                log.warn("Invalid regex pattern for attribute {}: {}", attributeName, config.pattern());
            }
        }
    }

    private static void validateNumber(Object value, ValidationConfigDto config, String attributeName, List<String> errors) {
        BigDecimal number;
        if (value instanceof Number num) {
            // Use toString() to preserve precision instead of doubleValue()
            number = new BigDecimal(num.toString());
        } else {
            try {
                number = new BigDecimal(String.valueOf(value));
            } catch (NumberFormatException e) {
                errors.add(String.format("Attribute '%s' is not a valid number", attributeName));
                return;
            }
        }

        if (config.minValue() != null && number.compareTo(config.minValue()) < 0) {
            errors.add(String.format("Attribute '%s' must be at least %s (current: %s)",
                    attributeName, config.minValue(), number));
        }

        if (config.maxValue() != null && number.compareTo(config.maxValue()) > 0) {
            errors.add(String.format("Attribute '%s' must be at most %s (current: %s)",
                    attributeName, config.maxValue(), number));
        }
    }

    private static void validateDate(Object value, ValidationConfigDto config, String attributeName, List<String> errors) {
        LocalDate date;
        if (value instanceof LocalDate localDate) {
            date = localDate;
        } else {
            try {
                date = LocalDate.parse(String.valueOf(value), DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (Exception e) {
                errors.add(String.format("Attribute '%s' is not a valid date", attributeName));
                return;
            }
        }

        if (config.minDate() != null && date.isBefore(config.minDate())) {
            errors.add(String.format("Attribute '%s' must not be before %s (current: %s)",
                    attributeName, config.minDate(), date));
        }

        if (config.maxDate() != null && date.isAfter(config.maxDate())) {
            errors.add(String.format("Attribute '%s' must not be after %s (current: %s)",
                    attributeName, config.maxDate(), date));
        }
    }

    /**
     * Validates a value and throws an exception if validation fails.
     *
     * @param value           The value to validate
     * @param fieldType       The field type
     * @param config          The validation configuration
     * @param attributeName   The attribute name for error messages
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateOrThrow(Object value, ProductFieldType fieldType, ValidationConfigDto config, String attributeName) {
        List<String> errors = validate(value, fieldType, config, attributeName);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join("; ", errors));
        }
    }
}
