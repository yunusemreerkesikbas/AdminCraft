package com.backend.presentation.dto.request;

import com.backend.domain.enums.Language;
import jakarta.validation.constraints.*;

import java.util.Set;

public record CreateTenantRequest(
    @NotBlank(message = "validation.subdomain.required")
    @Size(min = 3, max = 50, message = "validation.subdomain.size")
    @Pattern(regexp = "^[a-z0-9]+(-[a-z0-9]+)*$", message = "validation.subdomain.pattern")
    String subdomain,
    
    @NotBlank(message = "validation.company.name.required")
    @Size(max = 100, message = "validation.company.name.size")
    String companyName,
    
    @Email(message = "validation.email.invalid")
    @NotBlank(message = "validation.admin.email.required")
    String adminEmail,
    
    @NotBlank(message = "validation.admin.name.required")
    @Size(max = 100, message = "validation.admin.name.size")
    String adminName,
    
    @Size(max = 20, message = "validation.phone.size")
    String phone,
    
    @NotNull(message = "validation.default.language.required")
    Language defaultLanguage,
    
    Set<Language> supportedLanguages,
    
    @Size(max = 50, message = "validation.timezone.size")
    String timezone,
    
    @Size(max = 3, message = "validation.currency.size")
    String currency,
    
    @Size(max = 1000, message = "validation.notes.size")
    String notes
) {
    // Regular constructor with validation and normalization
    public CreateTenantRequest(
        String subdomain,
        String companyName,
        String adminEmail,
        String adminName,
        String phone,
        Language defaultLanguage,
        Set<Language> supportedLanguages,
        String timezone,
        String currency,
        String notes
    ) {
        // Normalize input data
        this.subdomain = normalizeString(subdomain, true);
        this.companyName = normalizeString(companyName, false);
        this.adminEmail = normalizeString(adminEmail, true);
        this.adminName = normalizeString(adminName, false);
        this.phone = phone;
        this.timezone = normalizeStringWithDefault(timezone, "Europe/Istanbul");
        this.currency = normalizeStringWithDefault(currency, "TRY");
        this.notes = notes;
        
        // Handle language logic properly
        var languageResult = normalizeLanguages(defaultLanguage, supportedLanguages);
        this.defaultLanguage = languageResult.defaultLang();
        this.supportedLanguages = languageResult.supportedLangs();
    }
    
    private static String normalizeString(String value, boolean toLowerCase) {
        if (value == null) return null;
        String trimmed = value.trim();
        return toLowerCase ? trimmed.toLowerCase() : trimmed;
    }
    
    private static String normalizeStringWithDefault(String value, String defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return value.trim();
    }
    
    private static LanguageResult normalizeLanguages(Language defaultLanguage, Set<Language> supportedLanguages) {
        // If supported languages provided but no default, pick first from supported
        if (defaultLanguage == null && supportedLanguages != null && !supportedLanguages.isEmpty()) {
            Language firstSupported = supportedLanguages.iterator().next();
            return new LanguageResult(firstSupported, supportedLanguages);
        }
        
        // If no default language provided, use TR as fallback
        Language finalDefault = (defaultLanguage != null) ? defaultLanguage : Language.TR;
        
        // If no supported languages provided, use default language
        if (supportedLanguages == null || supportedLanguages.isEmpty()) {
            return new LanguageResult(finalDefault, Set.of(finalDefault));
        }
        
        // Ensure default language is in supported languages
        if (!supportedLanguages.contains(finalDefault)) {
            var mutableLanguages = new java.util.HashSet<>(supportedLanguages);
            mutableLanguages.add(finalDefault);
            return new LanguageResult(finalDefault, Set.copyOf(mutableLanguages));
        }
        
        return new LanguageResult(finalDefault, supportedLanguages);
    }
    
    private record LanguageResult(Language defaultLang, Set<Language> supportedLangs) {}
}