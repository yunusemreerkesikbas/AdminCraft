package com.backend.presentation.dto.request;

import com.backend.domain.enums.Language;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record CreateSiteRequest(
    @NotBlank(message = "Site name is required")
    @Size(max = 255, message = "Site name must not exceed 255 characters")
    String siteName,
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    String description,
    
    @NotNull(message = "Default language is required")
    Language defaultLanguage,
    
    Set<Language> enabledLanguages,
    
    String domain,
    String theme,
    String logoUrl,
    String faviconUrl,
    String primaryColor,
    String secondaryColor,
    String fontFamily,
    String metaTitle,
    String metaDescription,
    String metaKeywords,
    String googleAnalyticsId,
    String customCode,
    Boolean isActive
) {
    public CreateSiteRequest {
        if (siteName != null && siteName.trim().isEmpty()) {
            throw new IllegalArgumentException("Site name cannot be empty");
        }
        if (enabledLanguages != null && !enabledLanguages.isEmpty() && defaultLanguage != null && !enabledLanguages.contains(defaultLanguage)) {
            throw new IllegalArgumentException("Default language must be included in enabled languages");
        }
    }
}