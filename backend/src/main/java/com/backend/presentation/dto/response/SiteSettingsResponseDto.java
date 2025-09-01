package com.backend.presentation.dto.response;

import com.backend.presentation.dto.request.SiteSettingsGlobalDto;
import com.backend.presentation.dto.request.SiteSettingsI18nDto;

public record SiteSettingsResponseDto(
    SiteSettingsGlobalDto global,
    SiteSettingsI18nDto i18n,
    String language) {
}
