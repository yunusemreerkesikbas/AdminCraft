package com.backend.domain.converter;

import com.backend.domain.enums.Language;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Converter
@Slf4j
public class LanguageSetConverter implements AttributeConverter<Set<Language>, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Set<Language> languages) {
        if (languages == null || languages.isEmpty()) {
            return "[]";
        }

        try {
            Set<String> languageCodes = languages.stream()
                    .map(Enum::name)
                    .collect(Collectors.toSet());
            return objectMapper.writeValueAsString(languageCodes);
        } catch (JsonProcessingException e) {
            log.error("Error converting languages to JSON: {}", languages, e);
            return "[]";
        }
    }

    @Override
    public Set<Language> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty() || "[]".equals(dbData)) {
            return new HashSet<>(Set.of(Language.TR)); // Default to TR
        }

        try {
            Set<String> languageCodes = objectMapper.readValue(
                    dbData,
                    new TypeReference<Set<String>>() {
                    });
            return languageCodes.stream()
                    .map(code -> {
                        try {
                            return Language.valueOf(code);
                        } catch (IllegalArgumentException e) {
                            log.warn("Invalid language code in DB: {}", code);
                            return null;
                        }
                    })
                    .filter(lang -> lang != null)
                    .collect(Collectors.toSet());
        } catch (JsonProcessingException e) {
            log.error("Error parsing languages JSON: {}", dbData, e);
            return new HashSet<>(Set.of(Language.TR)); // Fallback to default
        }
    }
}
