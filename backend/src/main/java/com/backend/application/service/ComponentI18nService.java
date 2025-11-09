package com.backend.application.service;

import com.backend.domain.enums.Language;
import com.backend.presentation.dto.request.ComponentI18nRequest;
import com.backend.presentation.dto.response.ComponentI18nResponse;
import java.util.List;

public interface ComponentI18nService {
    ComponentI18nResponse upsertComponentI18n(Long componentId, Language language, ComponentI18nRequest request);
    ComponentI18nResponse getComponentI18n(Long componentId, Language language);
    List<ComponentI18nResponse> getComponentI18nByComponentId(Long componentId);
    ComponentI18nResponse publishComponentI18n(Long componentId, Language language);
    ComponentI18nResponse unpublishComponentI18n(Long componentId, Language language);
    void deleteComponentI18n(Long componentId, Language language);
}
