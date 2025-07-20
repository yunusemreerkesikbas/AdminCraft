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
    public CreateTenantRequest {
        // Compact canonical constructor for validation
        if (subdomain != null) {
            subdomain = subdomain.toLowerCase().trim();
        }
        if (companyName != null) {
            companyName = companyName.trim();
        }
        if (adminEmail != null) {
            adminEmail = adminEmail.toLowerCase().trim();
        }
        if (adminName != null) {
            adminName = adminName.trim();
        }
        if (timezone == null || timezone.isEmpty()) {
            timezone = "Europe/Istanbul";
        }
        if (currency == null || currency.isEmpty()) {
            currency = "TRY";
        }
        if (defaultLanguage == null) {
            defaultLanguage = Language.TR;
        }
        if (supportedLanguages == null || supportedLanguages.isEmpty()) {
            supportedLanguages = Set.of(defaultLanguage);
        }
        // Ensure default language is in supported languages
        if (!supportedLanguages.contains(defaultLanguage)) {
            supportedLanguages = Set.of(defaultLanguage);
        }
    }
}