package com.backend.domain.enums;

public enum ProductVariantOptionDisplayType {
    TEXT,
    COLOR;

    public String toValue() {
        return name();
    }

    public static ProductVariantOptionDisplayType fromValue(String value) {
        if (value == null) {
            return null;
        }
        return valueOf(value.toUpperCase());
    }
}
