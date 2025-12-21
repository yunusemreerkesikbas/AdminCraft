package com.backend.application.service;

import com.backend.application.dto.request.NavigationEntryI18nRequest;
import com.backend.application.dto.request.NavigationNodeI18nRequest;
import com.backend.application.dto.response.NavigationEntryI18nResponse;
import com.backend.application.dto.response.NavigationNodeI18nResponse;
import com.backend.domain.enums.Language;

public interface NavigationI18nService {

  NavigationNodeI18nResponse getNodeI18n(Long nodeId, Language language);

  NavigationNodeI18nResponse upsertNodeI18n(Long nodeId, Language language, NavigationNodeI18nRequest request);

  NavigationEntryI18nResponse getEntryI18n(Long entryId, Language language);

  NavigationEntryI18nResponse upsertEntryI18n(Long entryId, Language language, NavigationEntryI18nRequest request);
}
