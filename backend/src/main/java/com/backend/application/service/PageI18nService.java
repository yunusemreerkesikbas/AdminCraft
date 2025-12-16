package com.backend.application.service;

import com.backend.domain.enums.Language;
import com.backend.application.dto.request.PageI18nRequest;
import com.backend.application.dto.request.PagePublishRequest;
import com.backend.presentation.dto.response.PageI18nResponse;
import java.util.List;

public interface PageI18nService {
    PageI18nResponse getPageI18n(Long pageId, Language language);

    PageI18nResponse upsertPageI18n(Long pageId, Language language, PageI18nRequest request);

    List<PageI18nResponse> getAllPageI18n(Long pageId);

    PageI18nResponse publishPageI18n(Long pageId, Language language, PagePublishRequest request);

    PageI18nResponse unpublishPageI18n(Long pageId, Language language);

    void deletePageI18n(Long pageId);
}
