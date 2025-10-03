package com.backend.application.service;

import com.backend.presentation.dto.request.PageCreateRequest;
import com.backend.presentation.dto.response.PageResponse;
import com.backend.presentation.dto.response.PageWithI18nResponse;
import java.util.List;

public interface PageService {
    PageResponse createPage(PageCreateRequest request, Long userId);
    PageResponse getPageById(Long id, Long tenantId);
    PageWithI18nResponse getPageWithI18n(Long id, Long tenantId);
    List<PageResponse> getAllPages(Long tenantId);
    PageResponse updatePage(Long id, PageCreateRequest request, Long userId);
    void deletePage(Long id, Long tenantId);
    PageResponse setHomePage(Long id, Long tenantId);
}
