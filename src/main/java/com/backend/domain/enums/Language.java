package com.backend.domain.enums;

public enum Language {
    TR("tr", "Türkçe", "Turkish"),
    EN("en", "İngilizce", "English");

    private final String code;
    private final String displayNameTr;
    private final String displayNameEn;

    Language(String code, String displayNameTr, String displayNameEn) {
        this.code = code;
        this.displayNameTr = displayNameTr;
        this.displayNameEn = displayNameEn;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayNameTr; // Default to Turkish for backward compatibility
    }

    public String getDisplayName(Language language) {
        return switch (language) {
            case TR -> displayNameTr;
            case EN -> displayNameEn;
            default -> displayNameTr;
        };
    }

    public String getDisplayNameTr() {
        return displayNameTr;
    }

    public String getDisplayNameEn() {
        return displayNameEn;
    }

    public static Language fromCode(String code) {
        for (Language language : values()) {
            if (language.code.equals(code)) {
                return language;
            }
        }
        throw new IllegalArgumentException("Unsupported language code: " + code);
    }
}