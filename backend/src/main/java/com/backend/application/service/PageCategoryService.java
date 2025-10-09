package com.backend.application.service;

import com.backend.application.command.CreatePageCategoryCommand;
import com.backend.application.command.UpdatePageCategoryCommand;
import com.backend.application.command.UpsertPageCategoryI18nCommand;
import com.backend.application.query.PageCategoryDetailQuery;
import com.backend.domain.entity.PageCategory;
import com.backend.domain.entity.PageCategoryI18n;
import com.backend.domain.enums.Language;
import com.backend.presentation.dto.response.*;

import java.util.List;
import java.util.Optional;

public interface PageCategoryService {
    PageCategoryDetailResponse create(CreatePageCategoryCommand command, Long tenantId);

    PageCategoryDetailResponse update(Long id, UpdatePageCategoryCommand command, Long tenantId);

    void delete(Long id, Long tenantId);

    Optional<PageCategory> findById(Long id, Long tenantId);

    List<PageCategoryListResponse> listByTenant(Long tenantId);

    PageCategoryDetailResponse getDetailById(PageCategoryDetailQuery query);

    PageCategoryI18nResponse upsertI18n(Long categoryId, Language language, UpsertPageCategoryI18nCommand command,
            Long tenantId);

    Optional<PageCategoryI18n> getI18n(Long categoryId, Language language, Long tenantId);
}
