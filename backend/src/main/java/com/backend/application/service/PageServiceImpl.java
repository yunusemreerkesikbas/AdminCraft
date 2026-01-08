package com.backend.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.application.dto.request.CreatePageCompositeRequest;
import com.backend.application.dto.request.PageCreateRequest;
import com.backend.application.dto.request.PageI18nCompositeRequest;
import com.backend.application.dto.request.PageI18nRequest;
import com.backend.application.dto.request.UpdatePageCompositeRequest;
import com.backend.domain.entity.Page;
import com.backend.domain.entity.PageI18n;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.PageStatus;
import com.backend.domain.enums.RobotTag;
import com.backend.domain.exception.PageNotFoundException;
import com.backend.domain.repository.PageI18nRepository;
import com.backend.domain.repository.PageRepository;
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
        page.setUid((request.uid() != null && !request.uid().isEmpty())
                ? request.uid()
                : generateUniqueUidForPage());
        page.setTemplateId(request.templateId());
        page.setStatus(request.status() != null ? request.status() : PageStatus.DRAFT);
        page.setStyleClasses(request.styleClasses());
        page.setRobotTag(request.robotTag() != null ? request.robotTag() : RobotTag.INDEX_FOLLOW);
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

        page.setTemplateId(request.templateId());
        if (request.status() != null)
            page.setStatus(request.status());
        if (request.styleClasses() != null)
            page.setStyleClasses(request.styleClasses());
        if (request.robotTag() != null)
            page.setRobotTag(request.robotTag());

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

    // ==================== Composite Operations (Sprint 34 Pattern)
    // ====================

    @Override
    @Transactional
    public PageDetailResponse createComposite(CreatePageCompositeRequest request, Long userId) {
        // 1. Create the page
        Page page = new Page();
        page.setUuid(com.backend.infrastructure.util.UuidUidGenerator.generateUuid());
        page.setUid((request.uid() != null && !request.uid().isEmpty())
                ? request.uid()
                : generateUniqueUidForPage());
        page.setTemplateId(request.templateId());
        page.setStatus(request.status() != null ? request.status() : PageStatus.DRAFT);
        page.setStyleClasses(request.styleClasses());
        page.setRobotTag(request.robotTag() != null ? request.robotTag() : RobotTag.INDEX_FOLLOW);
        page.setCreatedAt(LocalDateTime.now());
        page.setUpdatedAt(LocalDateTime.now());
        page.setCreatedBy(userId);
        page.setUpdatedBy(userId);

        page = pageRepository.save(page);
        final Long pageId = page.getId();

        // 2. Create translations
        upsertTranslations(pageId, request.translations(), request.status());

        // 3. Return composite response
        List<PageI18n> i18nList = pageI18nRepository.findByPageId(pageId);
        return PageDetailResponse.from(page, i18nList);
    }

    @Override
    @Transactional
    public PageDetailResponse updateComposite(Long id, UpdatePageCompositeRequest request, Long userId) {
        // 1. Update the page
        Page page = pageRepository.findById(id)
                .orElseThrow(() -> new PageNotFoundException(id));

        if (request.templateId() != null)
            page.setTemplateId(request.templateId());
        if (request.status() != null)
            page.setStatus(request.status());
        if (request.styleClasses() != null)
            page.setStyleClasses(request.styleClasses());
        if (request.robotTag() != null)
            page.setRobotTag(request.robotTag());

        page.setUpdatedAt(LocalDateTime.now());
        page.setUpdatedBy(userId);

        page = pageRepository.save(page);

        // 2. Update translations
        upsertTranslations(id, request.translations(), page.getStatus());

        // 3. Return composite response
        List<PageI18n> i18nList = pageI18nRepository.findByPageId(id);
        return PageDetailResponse.from(page, i18nList);
    }

    /**
     * Helper method to upsert translations from composite request.
     * Reduces code duplication between createComposite and updateComposite.
     */
    private void upsertTranslations(Long pageId, Map<String, PageI18nCompositeRequest> translations,
            PageStatus fallbackStatus) {
        if (translations == null || translations.isEmpty()) {
            return;
        }

        for (Map.Entry<String, PageI18nCompositeRequest> entry : translations.entrySet()) {
            Language lang = Language.valueOf(entry.getKey().toUpperCase());
            PageI18nCompositeRequest i18nReq = entry.getValue();

            PageI18nRequest pageI18nRequest = new PageI18nRequest(
                    lang,
                    i18nReq.name(),
                    i18nReq.canonicalUrl(),
                    i18nReq.title(),
                    i18nReq.description(),
                    i18nReq.status() != null ? i18nReq.status() : fallbackStatus,
                    null
            );

            pageI18nService.upsertPageI18n(pageId, lang, pageI18nRequest);
        }
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
