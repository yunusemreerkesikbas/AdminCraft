package com.backend.application.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.application.dto.request.PageI18nRequest;
import com.backend.application.dto.request.PagePublishRequest;
import com.backend.application.service.PageI18nService;
import com.backend.domain.entity.Page;
import com.backend.domain.entity.PageI18n;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.PageStatus;
import com.backend.domain.enums.PageType;
import com.backend.domain.exception.PageCannotBePublishedException;
import com.backend.domain.exception.PageNotFoundException;
import com.backend.domain.repository.PageI18nRepository;
import com.backend.domain.repository.PageRepository;
import com.backend.domain.util.UuidUidGenerator;
import com.backend.presentation.dto.response.PageI18nResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PageI18nServiceImpl implements PageI18nService {

    private final PageI18nRepository pageI18nRepository;
    private final PageRepository pageRepository;

    @Override
    @Transactional(readOnly = true)
    public PageI18nResponse getPageI18n(Long pageId, Language language) {
        getPageOrThrow(pageId);

        return pageI18nRepository.findByPageIdAndLanguage(pageId, language)
                .map(PageI18nResponse::from)
                .orElseGet(() -> getFallbackLanguageI18n(pageId));
    }

    @Override
    @Transactional
    public PageI18nResponse upsertPageI18n(Long pageId, Language language, PageI18nRequest request) {
        getPageOrThrow(pageId);
        validateLanguageMatch(language, request.language());

        if (request.canonicalUrl() != null && !request.canonicalUrl().trim().isEmpty()) {
            validateCanonicalUrlUniqueness(language, request.canonicalUrl(), pageId);
        }

        PageI18n pageI18n = pageI18nRepository
                .findByPageIdAndLanguage(pageId, language)
                .map(existing -> updateExistingPageI18n(existing, request))
                .orElseGet(() -> createNewPageI18n(pageId, language, request));

        pageI18n = pageI18nRepository.save(pageI18n);
        return PageI18nResponse.from(pageI18n);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PageI18nResponse> getAllPageI18n(Long pageId) {
        getPageOrThrow(pageId);

        return pageI18nRepository.findByPageId(pageId)
                .stream()
                .map(PageI18nResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PageI18nResponse publishPageI18n(Long pageId, Language language, PagePublishRequest request) {
        Page page = getPageOrThrow(pageId);

        PageI18n pageI18n = pageI18nRepository
                .findByPageIdAndLanguage(pageId, language)
                .orElseThrow(() -> new PageNotFoundException(
                        "PageI18n not found for pageId: " + pageId + " and language: " + language));

        validateCanPublish(pageI18n);

        if (request.isImmediatePublish()) {
            validateSinglePublishedTemplatePerType(page);
            pageI18n.publish();

            // Update Parent Page PublishedAt
            page.setPublishedAt(LocalDateTime.now());
            page.setScheduledAt(null);
            page.setStatus(PageStatus.PUBLISHED);
            pageRepository.save(page);

        } else {
            // Schedule logic
            pageI18n.schedule(request.scheduledAt());

            page.setScheduledAt(request.scheduledAt());
            page.setStatus(PageStatus.SCHEDULED);
            pageRepository.save(page);
        }

        pageI18n = pageI18nRepository.save(pageI18n);
        return PageI18nResponse.from(pageI18n);
    }

    @Override
    @Transactional
    public PageI18nResponse unpublishPageI18n(Long pageId, Language language) {
        Page page = getPageOrThrow(pageId);

        PageI18n pageI18n = pageI18nRepository
                .findByPageIdAndLanguage(pageId, language)
                .orElseThrow(() -> new PageNotFoundException(
                        "PageI18n not found for pageId: " + pageId + " and language: " + language));

        pageI18n.unpublish();

        page.setPublishedAt(null);
        page.setScheduledAt(null);
        page.setStatus(PageStatus.DRAFT);
        pageRepository.save(page);

        pageI18n = pageI18nRepository.save(pageI18n);
        return PageI18nResponse.from(pageI18n);
    }

    @Override
    @Transactional
    public void deletePageI18n(Long pageId) {
        pageI18nRepository.deleteByPageId(pageId);
    }

    private Page getPageOrThrow(Long pageId) {
        return pageRepository.findById(pageId)
                .orElseThrow(() -> new PageNotFoundException(pageId));
    }

    private void validateLanguageMatch(Language expected, Language actual) {
        if (!expected.equals(actual)) {
            throw new IllegalArgumentException(
                    "Language mismatch: URL parameter is " + expected + " but request body has " + actual);
        }
    }

    private void validateCanonicalUrlUniqueness(Language language, String canonicalUrl, Long pageId) {
        pageI18nRepository.findByLanguageAndCanonicalUrl(language, canonicalUrl)
                .ifPresent(existing -> {
                    if (!existing.getPageId().equals(pageId)) {
                        throw new IllegalArgumentException(
                                "Canonical URL '" + canonicalUrl + "' already exists for language " + language);
                    }
                });
    }

    private void validateCanPublish(PageI18n pageI18n) {
        if (!pageI18n.canBePublished()) {
            throw new PageCannotBePublishedException(
                    "PageI18n cannot be published. Missing required fields: title and/or canonicalUrl. " +
                            "PageId: " + pageI18n.getPageId() + ", Language: " + pageI18n.getLanguage());
        }
    }

    private void validateSinglePublishedTemplatePerType(Page page) {
        PageType pageType = page.getPageType();
        if (pageType == null || !isTemplateSingletonType(pageType)) {
            return;
        }

        pageRepository.findFirstByPageTypeAndStatusOrderByIdAsc(pageType, PageStatus.PUBLISHED)
                .filter(existingPublishedPage -> !existingPublishedPage.getId().equals(page.getId()))
                .ifPresent(existingPublishedPage -> {
                    throw new PageCannotBePublishedException(
                            "Only one published page is allowed for pageType=" + pageType +
                                    ". Existing page id=" + existingPublishedPage.getId() +
                                    ", attempted page id=" + page.getId());
                });
    }

    private boolean isTemplateSingletonType(PageType pageType) {
        return pageType == PageType.PRODUCT || pageType == PageType.CATEGORY || pageType == PageType.SEARCH;
    }

    private PageI18nResponse getFallbackLanguageI18n(Long pageId) {
        // Note: TenantContext routing ensures we're already in the correct tenant
        // database
        // We'll use the first available i18n entry as fallback instead of tenant's
        // default language
        return pageI18nRepository.findByPageId(pageId)
                .stream()
                .findFirst()
                .map(PageI18nResponse::from)
                .orElseThrow(() -> new PageNotFoundException(
                        "No i18n found for pageId: " + pageId + " in any language"));
    }

    private PageI18n updateExistingPageI18n(PageI18n existing, PageI18nRequest request) {
        if (request.name() != null)
            existing.setName(request.name());
        if (request.canonicalUrl() != null)
            existing.setCanonicalUrl(request.canonicalUrl());
        if (request.title() != null)
            existing.setTitle(request.title());
        if (request.description() != null)
            existing.setDescription(request.description());
        if (request.status() != null)
            existing.setStatus(request.status());

        return existing;
    }

    private PageI18n createNewPageI18n(Long pageId, Language language, PageI18nRequest request) {
        PageI18n pageI18n = new PageI18n();
        pageI18n.setPageId(pageId);
        pageI18n.setLanguage(language);
        pageI18n.setUuid(UuidUidGenerator.generateUuid());
        pageI18n.setUid(generateUniqueUidForI18n());
        pageI18n.setName(request.name());
        pageI18n.setCanonicalUrl(request.canonicalUrl());
        pageI18n.setTitle(request.title());
        pageI18n.setDescription(request.description());
        pageI18n.setStatus(request.status() != null ? request.status() : PageStatus.DRAFT);
        pageI18n.setUpdatedAt(LocalDateTime.now());

        return pageI18n;
    }

    private String generateUniqueUidForI18n() {
        String uid;
        int attempts = 0;
        do {
            uid = UuidUidGenerator.generateUid();
            attempts++;
            if (attempts > 10) {
                uid = uid + attempts;
            }
        } while (pageI18nRepository.findByUid(uid).isPresent());
        return uid;
    }
}
