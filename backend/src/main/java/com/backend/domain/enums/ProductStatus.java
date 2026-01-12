package com.backend.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ProductStatus {
    DRAFT,
    PUBLISHED;

    @JsonValue
    public String toValue() {
        return name();
    }

    @JsonCreator
    public static ProductStatus fromValue(String value) {
        if (value == null) {
            return null;
        }
        return valueOf(value.toUpperCase());
    }
}
