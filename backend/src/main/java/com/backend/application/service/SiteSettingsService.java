package com.backend.application.service;

import com.backend.application.dto.SiteSettingsCommand;
import com.backend.application.dto.SiteSettingsQuery;

public interface SiteSettingsService {

  /**
   * Returns all site settings for admin interface
   * @param tenantId the tenant ID for isolation
   * @return global settings and all supported languages
   */
  SiteSettingsQuery getAdminSettings(Long tenantId);

  /**
   * Partially updates site settings
   * @param command the update command with all necessary data
   * @return updated settings
   */
  SiteSettingsQuery patchSettings(SiteSettingsCommand command);
}
