package com.backend.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ProductFieldType {
    TEXT,
    RICHTEXT,
    NUMBER,
    BOOLEAN,
    DATE,
    MEDIA;

    @JsonValue
    public String toValue() {
        return name();
    }

    @JsonCreator
    public static ProductFieldType fromValue(String value) {
        if (value == null) {
            return null;
        }
        return valueOf(value.toUpperCase());
    }
}
