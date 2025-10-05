package com.backend.presentation.dto.response;

import com.backend.domain.entity.Page;
import com.backend.domain.entity.PageI18n;
import com.backend.domain.enums.Language;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public record PageWithI18nResponse(
        PageResponse page,
        Map<String, PageI18nResponse> translations, // key = language code (lowercase: tr, en)
        Set<String> availableLanguages // All languages from tenant.supportedLanguages
) {
    public PageWithI18nResponse {
        if (page == null) {
            throw new IllegalArgumentException("Page cannot be null");
        }
        // Make translations map immutable and ensure it's never null
        translations = translations == null
                ? Map.of()
                : Map.copyOf(translations);
        // Make availableLanguages immutable and ensure it's never null
        availableLanguages = availableLanguages == null
                ? Set.of()
                : Set.copyOf(availableLanguages);
    }

    public static PageWithI18nResponse from(Page page, List<PageI18n> pageI18nList, Set<Language> tenantSupportedLanguages) {
        if (page == null) {
            throw new IllegalArgumentException("Page entity cannot be null");
        }

        PageResponse pageResponse = PageResponse.from(page);

        Map<String, PageI18nResponse> translationsMap = pageI18nList == null || pageI18nList.isEmpty()
                ? new HashMap<>()
                : pageI18nList.stream()
                        .collect(Collectors.toMap(
                                pageI18n -> pageI18n.getLanguage().getCode().toLowerCase(),
                                PageI18nResponse::from,
                                (existing, replacement) -> replacement // In case of duplicate, keep latest
                        ));

        // Convert tenant's supported languages to lowercase language codes
        Set<String> availableLangs = tenantSupportedLanguages == null || tenantSupportedLanguages.isEmpty()
                ? Set.of()
                : tenantSupportedLanguages.stream()
                        .map(lang -> lang.getCode().toLowerCase())
                        .collect(Collectors.toSet());

        return new PageWithI18nResponse(pageResponse, translationsMap, availableLangs);
    }

    public PageI18nResponse getTranslation(Language language) {
        return translations.get(language.getCode().toLowerCase());
    }

    public PageI18nResponse getTranslation(String languageCode) {
        return translations.get(languageCode.toLowerCase());
    }

    public boolean hasTranslation(Language language) {
        return translations.containsKey(language.getCode().toLowerCase());
    }

    public boolean hasTranslation(String languageCode) {
        return translations.containsKey(languageCode.toLowerCase());
    }

    public int getTranslationCount() {
        return translations.size();
    }

    public boolean hasTranslations() {
        return !translations.isEmpty();
    }

    public boolean areAllTranslationsPublished() {
        return !translations.isEmpty() &&
                translations.values().stream().allMatch(PageI18nResponse::isPublished);
    }

    public boolean hasAnyPublishedTranslation() {
        return translations.values().stream().anyMatch(PageI18nResponse::isPublished);
    }

    public long getPublishedTranslationCount() {
        return translations.values().stream()
                .filter(PageI18nResponse::isPublished)
                .count();
    }
}
