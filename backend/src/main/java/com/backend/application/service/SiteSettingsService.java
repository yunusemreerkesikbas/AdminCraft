package com.backend.application.service;

import java.util.Map;

import com.backend.application.dto.SiteSettingsAppDto.SiteSettingsAppGlobalDto;
import com.backend.application.dto.SiteSettingsAppDto.SiteSettingsAppI18nDto;
import com.backend.application.dto.SiteSettingsAppDto.SiteSettingsAppResponseDto;
import com.backend.domain.enums.Language;

public interface SiteSettingsService {

  /**
   * Returns all site settings for admin interface
   * 
   * @param tenantId the tenant ID for isolation
   * @return global settings and all supported languages
   */
  SiteSettingsAppResponseDto getAdminSettings(Long tenantId);

  /**
   * Partially updates site settings
   * 
   * @param tenantId  the tenant ID
   * @param global    global settings to update (nullable)
   * @param languages language-specific settings to update (nullable)
   * @param updatedBy user ID who is making the update
   * @return updated settings
   */
  SiteSettingsAppResponseDto patchSettings(Long tenantId, SiteSettingsAppGlobalDto global,
      Map<Language, SiteSettingsAppI18nDto> languages, Long updatedBy);
}
