package com.backend.application.service;

import java.util.List;

import com.backend.application.dto.request.CreatePageCompositeRequest;
import com.backend.application.dto.request.PageCreateRequest;
import com.backend.application.dto.request.UpdatePageCompositeRequest;
import com.backend.application.dto.response.PageDetailResponse;
import com.backend.application.dto.response.PageListResponse;
import com.backend.application.dto.response.PageResponse;

public interface PageService {
    PageResponse createPage(PageCreateRequest request, Long userId);

    PageResponse getPageById(Long id);

    PageDetailResponse getPageWithI18n(Long id);

    List<PageResponse> getAllPages();

    List<PageListResponse> getAllPagesWithTranslations();

    PageResponse updatePage(Long id, PageCreateRequest request, Long userId);

    void deletePage(Long id);

    // Composite operations (Sprint 34 pattern)
    PageDetailResponse createComposite(CreatePageCompositeRequest request, Long userId);

    PageDetailResponse updateComposite(Long id, UpdatePageCompositeRequest request, Long userId);
}
