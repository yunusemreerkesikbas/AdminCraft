package com.backend.application.service;

import com.backend.presentation.dto.request.PageCreateRequest;
import com.backend.presentation.dto.response.PageListResponse;
import com.backend.presentation.dto.response.PageResponse;
import com.backend.presentation.dto.response.PageDetailResponse;
import java.util.List;

public interface PageService {
    PageResponse createPage(PageCreateRequest request, Long userId);
    PageResponse getPageById(Long id);
    PageDetailResponse getPageWithI18n(Long id);
    List<PageResponse> getAllPages();
    List<PageListResponse> getAllPagesWithTranslations();
    PageResponse updatePage(Long id, PageCreateRequest request, Long userId);
    void deletePage(Long id);
}
