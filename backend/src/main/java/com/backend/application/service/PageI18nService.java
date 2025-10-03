package com.backend.application.service;

import com.backend.domain.enums.Language;
import com.backend.presentation.dto.request.PageI18nRequest;
import com.backend.presentation.dto.request.PagePublishRequest;
import com.backend.presentation.dto.response.PageI18nResponse;
import java.util.List;

public interface PageI18nService {
    PageI18nResponse getPageI18n(Long pageId, Language language, Long tenantId);

    PageI18nResponse upsertPageI18n(Long pageId, Language language, Long tenantId, PageI18nRequest request);

    List<PageI18nResponse> getAllPageI18n(Long pageId, Long tenantId);

    PageI18nResponse publishPageI18n(Long pageId, Language language, Long tenantId, PagePublishRequest request);

    PageI18nResponse unpublishPageI18n(Long pageId, Language language, Long tenantId);

    void deletePageI18n(Long pageId);
}
