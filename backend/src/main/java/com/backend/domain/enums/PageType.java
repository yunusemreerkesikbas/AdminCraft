package com.backend.domain.enums;

import java.util.Arrays;
import java.util.Optional;

public enum PageType {
    CONTENT("ContentPage"),
    PRODUCT("ProductPage"),
    CATEGORY("CategoryPage"),
    SEARCH("SearchResultPage"),
    ERROR("ErrorPage"),
    LANDING("LandingPage");

    private final String typeCode;

    PageType(String typeCode) {
        this.typeCode = typeCode;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public static Optional<PageType> fromTypeCode(String typeCode) {
        if (typeCode == null || typeCode.isBlank()) {
            return Optional.empty();
        }
        String normalized = typeCode.trim();
        return Arrays.stream(values())
                .filter(pt -> pt.typeCode.equalsIgnoreCase(normalized))
                .findFirst();
    }
}
