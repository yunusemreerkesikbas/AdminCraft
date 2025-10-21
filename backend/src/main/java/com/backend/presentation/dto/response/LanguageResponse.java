package com.backend.presentation.dto.response;

import com.backend.domain.enums.Language;

public record LanguageResponse(
        String code,
        String nativeName,
        String englishName,
        boolean rtl
) {
    public static LanguageResponse from(Language language) {
        return new LanguageResponse(
                language.name(),
                language.getNativeName(),
                language.getEnglishName(),
                language.isRightToLeft()
        );
    }
}
