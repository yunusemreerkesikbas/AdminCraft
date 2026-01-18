package com.backend.application.dto.request;

import com.backend.domain.enums.Currency;
import com.backend.domain.enums.Language;
import jakarta.validation.constraints.*;

import java.util.Set;

public record CreateTenantRequest(
        @NotBlank(message = "validation.subdomain.required") @Size(min = 3, max = 50, message = "validation.subdomain.size") @Pattern(regexp = "^[a-z0-9]+(-[a-z0-9]+)*$", message = "validation.subdomain.pattern") String subdomain,

        @NotBlank(message = "validation.company.name.required") @Size(max = 100, message = "validation.company.name.size") String companyName,

        @NotNull(message = "validation.default.language.required") Language defaultLanguage,

        Set<Language> supportedLanguages,

        @NotNull(message = "validation.currency.required") Currency currency,

        @Size(max = 1000, message = "validation.notes.size") String notes) {
    public CreateTenantRequest(
            String subdomain,
            String companyName,
            Language defaultLanguage,
            Set<Language> supportedLanguages,
            Currency currency,
            String notes) {
        this.subdomain = normalizeString(subdomain, true);
        this.companyName = normalizeString(companyName, false);
        this.currency = currency != null ? currency : Currency.TRY;
        this.notes = notes;
        var languageResult = normalizeLanguages(defaultLanguage, supportedLanguages);
        this.defaultLanguage = languageResult.defaultLang();
        this.supportedLanguages = languageResult.supportedLangs();
    }

    private static String normalizeString(String value, boolean toLowerCase) {
        if (value == null)
            return null;
        String trimmed = value.trim();
        return toLowerCase ? trimmed.toLowerCase() : trimmed;
    }

    private static LanguageResult normalizeLanguages(Language defaultLanguage, Set<Language> supportedLanguages) {
        if (defaultLanguage == null && supportedLanguages != null && !supportedLanguages.isEmpty()) {
            Language firstSupported = supportedLanguages.iterator().next();
            return new LanguageResult(firstSupported, supportedLanguages);
        }
        Language finalDefault = (defaultLanguage != null) ? defaultLanguage : Language.TR;
        if (supportedLanguages == null || supportedLanguages.isEmpty()) {
            return new LanguageResult(finalDefault, Set.of(finalDefault));
        }
        if (!supportedLanguages.contains(finalDefault)) {
            var mutableLanguages = new java.util.HashSet<>(supportedLanguages);
            mutableLanguages.add(finalDefault);
            return new LanguageResult(finalDefault, Set.copyOf(mutableLanguages));
        }

        return new LanguageResult(finalDefault, supportedLanguages);
    }

    private record LanguageResult(Language defaultLang, Set<Language> supportedLangs) {
    }
}
