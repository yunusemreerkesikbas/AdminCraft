package com.backend.application.service;

import org.springframework.stereotype.Component;

import com.backend.domain.entity.EntryFieldDefinition;
import com.backend.domain.enums.EntryFieldType;
import com.backend.infrastructure.validation.FieldValidator;
import com.backend.infrastructure.validation.ValidationContext;
import com.backend.infrastructure.validation.ValidationResult;

@Component
public class EntryFieldValidator {

    private final FieldValidator<String> fieldKeyValidator;

    public EntryFieldValidator(FieldValidator<String> componentFieldKeyValidator) {
        this.fieldKeyValidator = componentFieldKeyValidator;
    }

    public void validate(EntryFieldDefinition field, long existingFieldCount) {
        validateFieldKey(field.getFieldKey(), existingFieldCount);
        validateFieldType(field);
        validateConstraints(field);
    }

    protected void validateFieldKey(String fieldKey, long existingFieldCount) {
        ValidationContext context = ValidationContext.empty();
        context.set("existingFieldCount", existingFieldCount);

        ValidationResult result = fieldKeyValidator.validate(fieldKey, context);
        result.throwIfInvalid();
    }

    protected void validateFieldType(EntryFieldDefinition field) {
        if (field.getFieldType() == null) {
            throw new IllegalArgumentException("Field type is required");
        }
    }

    protected void validateConstraints(EntryFieldDefinition field) {
        if (field.getFieldType() == EntryFieldType.TEXT && field.getMaxLength() != null) {
            if (field.getMaxLength() < 1 || field.getMaxLength() > 5000) {
                throw new IllegalArgumentException("Text max length must be between 1 and 5000");
            }
        }

        if (field.getFieldType() == EntryFieldType.NUMBER) {
            if (field.getMinValue() != null && field.getMaxValue() != null) {
                if (field.getMinValue().compareTo(field.getMaxValue()) > 0) {
                    throw new IllegalArgumentException("Min value cannot be greater than max value");
                }
            }
        }
    }
}
