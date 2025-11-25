package com.backend.infrastructure.config;

import com.backend.domain.enums.Language;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class LanguageConverter implements Converter<String, Language> {

    @Override
    public Language convert(String source) {
        if (source == null || source.trim().isEmpty()) {
            throw new IllegalArgumentException("Language code cannot be null or empty");
        }
        return Language.fromCode(source)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invalid language code: " + source + ". Supported: "
                                + String.join(", ", Language.getAllCodes())));
    }
}
