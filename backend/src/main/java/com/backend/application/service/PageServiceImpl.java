package com.backend.application.service;

import com.backend.domain.entity.Page;
import com.backend.domain.entity.PageI18n;
import com.backend.domain.enums.PageStatus;
import com.backend.domain.exception.PageNotFoundException;
import com.backend.domain.exception.TenantMismatchException;
import com.backend.domain.repository.PageI18nRepository;
import com.backend.domain.repository.PageRepository;
import com.backend.presentation.dto.request.PageCreateRequest;
import com.backend.presentation.dto.response.PageResponse;
import com.backend.presentation.dto.response.PageWithI18nResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PageServiceImpl implements PageService {

    private final PageRepository pageRepository;
    private final PageI18nRepository pageI18nRepository;
    private final PageI18nService pageI18nService;

    @Override
    @Transactional
    public PageResponse createPage(PageCreateRequest request, Long userId) {
        validateUidUniqueness(request.tenantId(), null);

        Page page = new Page();
        page.setTenantId(request.tenantId());
        page.setUuid(UUID.randomUUID().toString());
        page.setUid(generateUid());
        page.setCategoryId(request.categoryId());
        page.setStatus(request.status() != null ? request.status() : PageStatus.DRAFT);
        page.setFeaturedImage(request.featuredImage());
        page.setStyleClasses(request.styleClasses());
        page.setIsHome(request.isHome() != null ? request.isHome() : false);
        page.setSortOrder(request.sortOrder() != null ? request.sortOrder() : 0);
        page.setCreatedAt(LocalDateTime.now());
        page.setUpdatedAt(LocalDateTime.now());
        page.setCreatedBy(userId);
        page.setUpdatedBy(userId);

        page = pageRepository.save(page);
        return PageResponse.from(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse getPageById(Long id, Long tenantId) {
        Page page = pageRepository.findById(id)
            .orElseThrow(() -> new PageNotFoundException(id));

        validateTenantMatch(page.getTenantId(), tenantId);

        return PageResponse.from(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageWithI18nResponse getPageWithI18n(Long id, Long tenantId) {
        Page page = pageRepository.findById(id)
            .orElseThrow(() -> new PageNotFoundException(id));

        validateTenantMatch(page.getTenantId(), tenantId);

        List<PageI18n> i18nList = pageI18nRepository.findByTenantIdAndPageId(tenantId, id);
        return PageWithI18nResponse.from(page, i18nList);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PageResponse> getAllPages(Long tenantId) {
        return pageRepository.findByTenantId(tenantId)
            .stream()
            .map(PageResponse::from)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PageResponse updatePage(Long id, PageCreateRequest request, Long userId) {
        Page page = pageRepository.findById(id)
            .orElseThrow(() -> new PageNotFoundException(id));

        validateTenantMatch(page.getTenantId(), request.tenantId());

        if (request.categoryId() != null) page.setCategoryId(request.categoryId());
        if (request.status() != null) page.setStatus(request.status());
        if (request.featuredImage() != null) page.setFeaturedImage(request.featuredImage());
        if (request.styleClasses() != null) page.setStyleClasses(request.styleClasses());
        if (request.isHome() != null) page.setIsHome(request.isHome());
        if (request.sortOrder() != null) page.setSortOrder(request.sortOrder());

        page.setUpdatedAt(LocalDateTime.now());
        page.setUpdatedBy(userId);

        page = pageRepository.save(page);
        return PageResponse.from(page);
    }

    @Override
    @Transactional
    public void deletePage(Long id, Long tenantId) {
        Page page = pageRepository.findById(id)
            .orElseThrow(() -> new PageNotFoundException(id));

        validateTenantMatch(page.getTenantId(), tenantId);

        pageI18nService.deletePageI18n(id);
        pageRepository.deleteById(id);
    }

    @Override
    @Transactional
    public PageResponse setHomePage(Long id, Long tenantId) {
        Page page = pageRepository.findById(id)
            .orElseThrow(() -> new PageNotFoundException(id));

        validateTenantMatch(page.getTenantId(), tenantId);

        pageRepository.findHomePage(tenantId).ifPresent(currentHome -> {
            currentHome.unmarkAsHome();
            pageRepository.save(currentHome);
        });

        page.markAsHome();
        page = pageRepository.save(page);

        return PageResponse.from(page);
    }

    private void validateTenantMatch(Long pageTenantId, Long requestTenantId) {
        if (!pageTenantId.equals(requestTenantId)) {
            throw new TenantMismatchException(requestTenantId, pageTenantId);
        }
    }

    private void validateUidUniqueness(Long tenantId, String uid) {
        if (uid != null && pageRepository.existsByTenantIdAndUid(tenantId, uid)) {
            throw new IllegalArgumentException("Page with uid '" + uid + "' already exists for this tenant");
        }
    }

    private String generateUid() {
        return "cmsitem_" + String.format("%08d", (int) (Math.random() * 100000000));
    }
}
