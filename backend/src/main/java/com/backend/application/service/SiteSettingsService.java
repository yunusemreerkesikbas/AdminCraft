package com.backend.application.service;

import com.backend.domain.enums.Language;
import com.backend.presentation.dto.request.SiteSettingsGlobalDto;
import com.backend.presentation.dto.request.SiteSettingsI18nDto;
import com.backend.presentation.dto.response.SiteSettingsResponseDto;

public interface SiteSettingsService {

  SiteSettingsResponseDto get(Language language);

  SiteSettingsResponseDto patch(Language language,
      SiteSettingsGlobalDto global,
      SiteSettingsI18nDto i18n,
      Long updatedBy);
}
