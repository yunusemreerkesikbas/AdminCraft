package com.backend.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ProductVariantOptionDisplayType {
    TEXT,
    COLOR;

    @JsonValue
    public String toValue() {
        return name();
    }

    @JsonCreator
    public static ProductVariantOptionDisplayType fromValue(String value) {
        if (value == null) {
            return null;
        }
        return valueOf(value.toUpperCase());
    }
}
