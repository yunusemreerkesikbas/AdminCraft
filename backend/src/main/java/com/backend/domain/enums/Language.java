package com.backend.domain.enums;

import java.util.Arrays;
import java.util.Optional;

public enum Language {
    TR("tr", "Türkçe", "Turkish"),
    EN("en", "İngilizce", "English"),
    ES("es", "İspanyolca", "Spanish"),
    RU("ru", "Rusça", "Russian"),
    AR("ar", "Arapça", "Arabic");

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
        return displayNameTr;
    }

    public String getDisplayName(Language language) {
        return switch (language) {
            case TR -> displayNameTr;
            case EN -> displayNameEn;
            case ES, RU, AR -> displayNameEn;
        };
    }

    public String getDisplayNameTr() {
        return displayNameTr;
    }

    public String getDisplayNameEn() {
        return displayNameEn;
    }

    public static Optional<Language> fromCode(String code) {
        if (code == null) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(language -> language.code.equalsIgnoreCase(code.trim()))
                .findFirst();
    }
    
    public static Language fromCodeOrDefault(String code, Language defaultLanguage) {
        return fromCode(code).orElse(defaultLanguage);
    }
    
    public static Language fromCodeOrDefault(String code) {
        return fromCodeOrDefault(code, TR); // Default to Turkish
    }
}