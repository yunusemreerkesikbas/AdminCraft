package com.backend.application.service;

import com.backend.presentation.dto.request.PageCreateRequest;
import com.backend.presentation.dto.response.PageListResponse;
import com.backend.presentation.dto.response.PageResponse;
import com.backend.presentation.dto.response.PageDetailResponse;
import java.util.List;

public interface PageService {
    PageResponse createPage(PageCreateRequest request, Long userId);
    PageResponse getPageById(Long id, Long tenantId);
    PageDetailResponse getPageWithI18n(Long id, Long tenantId);
    List<PageResponse> getAllPages(Long tenantId);
    List<PageListResponse> getAllPagesWithTranslations(Long tenantId);
    PageResponse updatePage(Long id, PageCreateRequest request, Long userId);
    void deletePage(Long id, Long tenantId);
}
