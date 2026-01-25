package com.backend.application.service;

import java.util.Map;

import com.backend.domain.enums.Language;
import com.backend.presentation.dto.request.SiteSettingsGlobalDto;
import com.backend.presentation.dto.request.SiteSettingsI18nDto;
import com.backend.presentation.dto.response.SiteSettingsResponseDto;

public interface SiteSettingsService {

  /**
   * Returns all site settings for admin interface
   * 
   * @param tenantId the tenant ID for isolation
   * @return global settings and all supported languages
   */
  SiteSettingsResponseDto getAdminSettings(Long tenantId);

  /**
   * Partially updates site settings
   * 
   * @param tenantId  the tenant ID
   * @param global    global settings to update (nullable)
   * @param languages language-specific settings to update (nullable)
   * @param updatedBy user ID who is making the update
   * @return updated settings
   */
  SiteSettingsResponseDto patchSettings(Long tenantId, SiteSettingsGlobalDto global,
      Map<Language, SiteSettingsI18nDto> languages, Long updatedBy);
}
