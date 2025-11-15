package com.backend.application.service;

import com.backend.infrastructure.validation.FieldValidator;
import com.backend.infrastructure.validation.FieldValidatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration
public class ComponentFieldValidatorConfig {
    
    private static final Set<String> RESERVED_KEYWORDS = Set.of(
        "id", "uuid", "uid", "type", "class", "object", "function",
        "name", "value", "key", "data", "item", "index", "length",
        "createdAt", "updatedAt", "createdBy", "updatedBy", "deleted"
    );
    
    private static final int MAX_FIELD_KEY_LENGTH = 50;
    private static final int MAX_TEXT_LENGTH = 5000;
    private static final int MAX_FIELDS_PER_COMPONENT = 50;
    
    @Bean
    public FieldValidator<String> componentFieldKeyValidator() {
        return FieldValidatorBuilder.<String>create()
            .withCamelCaseKey(MAX_FIELD_KEY_LENGTH)
            .withReservedKeywords(RESERVED_KEYWORDS)
            .withCountLimit(MAX_FIELDS_PER_COMPONENT, "existingFieldCount")
            .build();
    }
    
    @Bean
    public FieldValidator<String> componentFieldTextValidator() {
        return FieldValidatorBuilder.<String>create()
            .withLengthConstraint(1, MAX_TEXT_LENGTH)
            .build();
    }
}

