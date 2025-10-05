package com.backend.domain.enums;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Supported languages in the system.
 * Display names should be retrieved from MessageSource using key: "language.{CODE}"
 */
public enum Language {
    TR("tr"),
    EN("en"),
    ES("es"),
    RU("ru"),
    AR("ar");

    private final String code;

    // Display names mapped by language code
    private static final Map<String, Map<String, String>> DISPLAY_NAMES = Map.of(
        "tr", Map.of(
            "tr", "Türkçe",
            "en", "İngilizce",
            "es", "İspanyolca",
            "ru", "Rusça",
            "ar", "Arapça"
        ),
        "en", Map.of(
            "tr", "Turkish",
            "en", "English",
            "es", "Spanish",
            "ru", "Russian",
            "ar", "Arabic"
        ),
        "es", Map.of(
            "tr", "Turco",
            "en", "Inglés",
            "es", "Español",
            "ru", "Ruso",
            "ar", "Árabe"
        ),
        "ru", Map.of(
            "tr", "Турецкий",
            "en", "Английский",
            "es", "Испанский",
            "ru", "Русский",
            "ar", "Арабский"
        ),
        "ar", Map.of(
            "tr", "التركية",
            "en", "الإنجليزية",
            "es", "الإسبانية",
            "ru", "الروسية",
            "ar", "العربية"
        )
    );

    Language(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    /**
     * Gets the display name of this language in the specified viewer language.
     * @param viewerLanguage The language to display the name in
     * @return Display name in the viewer's language
     */
    public String getDisplayName(Language viewerLanguage) {
        if (viewerLanguage == null) {
            viewerLanguage = TR; // Default fallback
        }

        Map<String, String> names = DISPLAY_NAMES.get(viewerLanguage.code);
        if (names == null) {
            names = DISPLAY_NAMES.get("en"); // Fallback to English
        }

        return names.getOrDefault(this.code, this.code.toUpperCase());
    }

    /**
     * Gets the display name based on Java Locale.
     * @param locale The locale to display the name in
     * @return Display name in the specified locale's language
     */
    public String getDisplayName(Locale locale) {
        if (locale == null) {
            return getDisplayName(TR);
        }

        Language viewerLang = fromCode(locale.getLanguage()).orElse(EN);
        return getDisplayName(viewerLang);
    }

    /**
     * Finds a Language enum by its code.
     * @param code The language code (case-insensitive)
     * @return Optional containing the Language if found
     */
    public static Optional<Language> fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(language -> language.code.equalsIgnoreCase(code.trim()))
                .findFirst();
    }

    /**
     * Finds a Language enum by code with fallback.
     * @param code The language code
     * @param defaultLanguage The fallback language
     * @return The Language enum or default
     */
    public static Language fromCodeOrDefault(String code, Language defaultLanguage) {
        return fromCode(code).orElse(defaultLanguage);
    }

    /**
     * Finds a Language enum by code with TR as fallback.
     * @param code The language code
     * @return The Language enum or TR
     */
    public static Language fromCodeOrDefault(String code) {
        return fromCodeOrDefault(code, TR);
    }
}