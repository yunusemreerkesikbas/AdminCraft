package com.backend.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SiteSettingsI18nDto(
    @NotBlank(message = "validation.required") @Size(max = 100, message = "validation.length") String siteName,

    @Size(max = 160, message = "validation.length") String tagline,

    String seo, // JSON string

    @Size(max = 500, message = "validation.length") String footerText,

    @Size(max = 200, message = "validation.length") String headerTopbarText,

    String addressLocalized // JSON string
) {
}
