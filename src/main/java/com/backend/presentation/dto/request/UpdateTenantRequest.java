package com.backend.presentation.dto.request;

import com.backend.domain.enums.Language;
import jakarta.validation.constraints.*;

import java.util.Set;

public record UpdateTenantRequest(
    @Size(max = 100, message = "validation.company.name.size")
    String companyName,
    
    @Email(message = "validation.email.invalid")
    String adminEmail,
    
    @Size(max = 100, message = "validation.admin.name.size")
    String adminName,
    
    @Size(max = 20, message = "validation.phone.size")
    String phone,
    
    Language defaultLanguage,
    
    Set<Language> supportedLanguages,
    
    @Size(max = 100, message = "validation.custom.domain.size")
    String customDomain,
    
    Boolean sslEnabled,
    
    @Size(max = 50, message = "validation.timezone.size")
    String timezone,
    
    @Size(max = 3, message = "validation.currency.size")
    String currency,
    
    @Size(max = 1000, message = "validation.notes.size")
    String notes
) {
    public UpdateTenantRequest {
        // Trim string values
        if (companyName != null) {
            companyName = companyName.trim();
        }
        if (adminEmail != null) {
            adminEmail = adminEmail.toLowerCase().trim();
        }
        if (adminName != null) {
            adminName = adminName.trim();
        }
        if (customDomain != null) {
            customDomain = customDomain.toLowerCase().trim();
        }
        if (timezone != null) {
            timezone = timezone.trim();
        }
        if (currency != null) {
            currency = currency.toUpperCase().trim();
        }
        if (notes != null) {
            notes = notes.trim();
        }
    }
}