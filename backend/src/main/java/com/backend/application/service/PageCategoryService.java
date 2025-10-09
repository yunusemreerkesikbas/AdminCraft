package com.backend.application.service;

import com.backend.domain.entity.PageCategory;
import com.backend.domain.entity.PageCategoryI18n;
import com.backend.domain.enums.Language;
import com.backend.presentation.dto.request.CreatePageCategoryRequest;
import com.backend.presentation.dto.request.UpdatePageCategoryRequest;
import com.backend.presentation.dto.request.UpsertPageCategoryI18nRequest;
import com.backend.presentation.dto.response.*;

import java.util.List;
import java.util.Optional;

public interface PageCategoryService {
    PageCategoryDetailResponse create(CreatePageCategoryRequest request, Long tenantId);

    PageCategoryDetailResponse update(Long id, UpdatePageCategoryRequest request, Long tenantId);

    void delete(Long id, Long tenantId);

    Optional<PageCategory> findById(Long id, Long tenantId);

    List<PageCategoryListResponse> listByTenant(Long tenantId);

    PageCategoryDetailResponse getDetailById(Long id, Long tenantId, boolean includeTranslations);

    PageCategoryI18nResponse upsertI18n(Long categoryId, Language language, UpsertPageCategoryI18nRequest request,
            Long tenantId);

    Optional<PageCategoryI18n> getI18n(Long categoryId, Language language, Long tenantId);
}
