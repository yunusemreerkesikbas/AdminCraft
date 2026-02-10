package com.backend.domain.enums;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

public enum Language {

    TR("tr", "tur", "Turkish", "Türkçe", false),

    EN("en", "eng", "English", "English", false),
    ZH("zh", "zho", "Chinese", "中文", false),
    HI("hi", "hin", "Hindi", "हिन्दी", false),
    ES("es", "spa", "Spanish", "Español", false),
    FR("fr", "fra", "French", "Français", false),
    AR("ar", "ara", "Arabic", "العربية", true),
    BN("bn", "ben", "Bengali", "বাংলা", false),
    RU("ru", "rus", "Russian", "Русский", false),
    PT("pt", "por", "Portuguese", "Português", false),
    UR("ur", "urd", "Urdu", "اردو", true);

    private final String code;

    private final String iso6392;
    private final String englishName;
    private final String nativeName;
    private final boolean rightToLeft;

    Language(String code, String iso6392, String englishName, String nativeName, boolean rightToLeft) {
        this.code = code;
        this.iso6392 = iso6392;
        this.englishName = englishName;
        this.nativeName = nativeName;
        this.rightToLeft = rightToLeft;
    }

    public String getCode() {
        return code;
    }

    public String getIso6392() {
        return iso6392;
    }

    public String getEnglishName() {
        return englishName;
    }

    public String getNativeName() {
        return nativeName;
    }

    public boolean isRightToLeft() {
        return rightToLeft;
    }

    public Locale toLocale() {
        return Locale.of(code);
    }

    public static Optional<Language> fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(language -> language.code.equalsIgnoreCase(code.trim()))
                .findFirst();
    }

    public static Optional<Language> fromLocale(Locale locale) {
        if (locale == null) {
            return Optional.empty();
        }
        return fromCode(locale.getLanguage());
    }

    public static Language fromCodeOrDefault(String code, Language defaultLanguage) {
        return fromCode(code).orElse(defaultLanguage);
    }

    public static Language fromCodeOrDefault(String code) {
        return fromCodeOrDefault(code, TR);
    }

    public static Language getDefault() {
        return TR;
    }

    public static String[] getAllCodes() {
        return Arrays.stream(values())
                .map(Language::getCode)
                .toArray(String[]::new);
    }

    public static boolean isSupported(String code) {
        return fromCode(code).isPresent();
    }
}