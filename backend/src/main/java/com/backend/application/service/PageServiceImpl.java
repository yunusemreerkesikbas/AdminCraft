package com.backend.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.domain.entity.Page;
import com.backend.domain.entity.PageI18n;
import com.backend.domain.enums.PageStatus;
import com.backend.domain.exception.PageNotFoundException;
import com.backend.domain.repository.PageI18nRepository;
import com.backend.domain.repository.PageRepository;
import com.backend.presentation.dto.request.PageCreateRequest;
import com.backend.presentation.dto.response.PageDetailResponse;
import com.backend.presentation.dto.response.PageResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PageServiceImpl implements PageService {

    private final PageRepository pageRepository;
    private final PageI18nRepository pageI18nRepository;
    private final PageI18nService pageI18nService;

    @Override
    @Transactional
    public PageResponse createPage(PageCreateRequest request, Long userId) {
        Page page = new Page();
        page.setUuid(com.backend.infrastructure.util.UuidUidGenerator.generateUuid());
        page.setUid(generateUniqueUidForPage());
        page.setTemplateId(request.templateId());
        page.setStatus(request.status() != null ? request.status() : PageStatus.DRAFT);
        page.setFeaturedImage(request.featuredImage());
        page.setStyleClasses(request.styleClasses());
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
    public PageResponse getPageById(Long id) {
        Page page = pageRepository.findById(id)
                .orElseThrow(() -> new PageNotFoundException(id));

        return PageResponse.from(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageDetailResponse getPageWithI18n(Long id) {
        Page page = pageRepository.findById(id)
                .orElseThrow(() -> new PageNotFoundException(id));

        List<PageI18n> i18nList = pageI18nRepository.findByPageId(id);
        return PageDetailResponse.from(page, i18nList);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PageResponse> getAllPages() {
        return pageRepository.findAll()
                .stream()
                .map(PageResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<com.backend.presentation.dto.response.PageListResponse> getAllPagesWithTranslations() {
        List<Page> pages = pageRepository.findAll();
        List<PageI18n> allTranslations = pageI18nRepository.findAll();
        java.util.Map<Long, List<PageI18n>> translationsByPage = allTranslations.stream()
                .collect(Collectors.groupingBy(PageI18n::getPageId));
        return pages.stream()
                .map(page -> com.backend.presentation.dto.response.PageListResponse.from(
                        page,
                        translationsByPage.getOrDefault(page.getId(), java.util.Collections.emptyList())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PageResponse updatePage(Long id, PageCreateRequest request, Long userId) {
        Page page = pageRepository.findById(id)
                .orElseThrow(() -> new PageNotFoundException(id));

        // templateId can be set to null intentionally (to clear template)
        page.setTemplateId(request.templateId());
        if (request.status() != null)
            page.setStatus(request.status());
        if (request.featuredImage() != null)
            page.setFeaturedImage(request.featuredImage());
        if (request.styleClasses() != null)
            page.setStyleClasses(request.styleClasses());
        if (request.sortOrder() != null)
            page.setSortOrder(request.sortOrder());

        page.setUpdatedAt(LocalDateTime.now());
        page.setUpdatedBy(userId);

        page = pageRepository.save(page);
        return PageResponse.from(page);
    }

    @Override
    @Transactional
    public void deletePage(Long id) {
        pageI18nService.deletePageI18n(id);
        pageRepository.deleteById(id);
    }

    private String generateUniqueUidForPage() {
        String uid;
        int attempts = 0;
        do {
            uid = com.backend.infrastructure.util.UuidUidGenerator.generateUid();
            attempts++;
            if (attempts > 10) {
                uid = uid + attempts; // slight perturbation
            }
        } while (pageRepository.existsByUid(uid));
        return uid;
    }
}
