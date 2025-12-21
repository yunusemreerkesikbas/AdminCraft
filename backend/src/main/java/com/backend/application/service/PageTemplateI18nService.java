package com.backend.application.service;

import com.backend.application.dto.request.PageTemplateI18nRequest;
import com.backend.application.dto.response.PageTemplateI18nResponse;
import com.backend.domain.enums.Language;

public interface PageTemplateI18nService {

  PageTemplateI18nResponse getTemplateI18n(Long templateId, Language language);

  PageTemplateI18nResponse upsertTemplateI18n(Long templateId, Language language, PageTemplateI18nRequest request);
}
