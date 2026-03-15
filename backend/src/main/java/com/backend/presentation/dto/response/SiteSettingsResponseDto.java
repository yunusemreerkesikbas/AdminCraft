package com.backend.presentation.dto.response;

import com.backend.presentation.dto.request.SiteSettingsI18nDto;

import java.util.Map;

/**
 * Response DTO matching the API contract specified in CLAUDE.md Sprint 9
 * Structure:
 * {
 *   "global": { ... },
 *   "languages": {
 *     "tr": { ... },
 *     "en": { ... }
 *   }
 * }
 */
public record SiteSettingsResponseDto(
    SiteSettingsGlobalResponseDto global,
    Map<String, SiteSettingsI18nDto> languages) {
}
