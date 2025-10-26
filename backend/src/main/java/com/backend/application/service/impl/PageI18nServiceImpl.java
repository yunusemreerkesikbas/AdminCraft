package com.backend.application.service.impl;

import com.backend.application.service.PageI18nService;
import com.backend.domain.entity.Page;
import com.backend.domain.entity.PageI18n;
import com.backend.domain.entity.Tenant;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.PageStatus;
import com.backend.domain.exception.PageCannotBePublishedException;
import com.backend.domain.exception.PageNotFoundException;
import com.backend.domain.exception.TenantMismatchException;
import com.backend.domain.exception.TenantNotFoundException;
import com.backend.domain.repository.PageI18nRepository;
import com.backend.domain.repository.PageRepository;
import com.backend.domain.repository.TenantRepository;
import com.backend.presentation.dto.request.PageI18nRequest;
import com.backend.presentation.dto.request.PagePublishRequest;
import com.backend.presentation.dto.response.PageI18nResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PageI18nServiceImpl implements PageI18nService {

    private final PageI18nRepository pageI18nRepository;
    private final PageRepository pageRepository;
    private final TenantRepository tenantRepository;

    @Override
    @Transactional(readOnly = true)
    public PageI18nResponse getPageI18n(Long pageId, Language language, Long tenantId) {
        validatePageExists(pageId, tenantId);

        return pageI18nRepository.findByPageIdAndLanguage(pageId, language)
                .map(PageI18nResponse::from)
                .orElseGet(() -> getFallbackLanguageI18n(pageId, tenantId));
    }

    @Override
    @Transactional
    public PageI18nResponse upsertPageI18n(Long pageId, Language language, Long tenantId, PageI18nRequest request) {
        validatePageExists(pageId, tenantId);
        validateLanguageMatch(language, request.language());

        if (request.urlPath() != null && !request.urlPath().trim().isEmpty()) {
            validateUrlPathUniqueness(tenantId, language, request.urlPath(), pageId);
        }

        PageI18n pageI18n = pageI18nRepository
                .findByPageIdAndLanguage(pageId, language)
                .map(existing -> updateExistingPageI18n(existing, request))
                .orElseGet(() -> createNewPageI18n(pageId, tenantId, language, request));

        pageI18n = pageI18nRepository.save(pageI18n);
        return PageI18nResponse.from(pageI18n);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PageI18nResponse> getAllPageI18n(Long pageId, Long tenantId) {
        validatePageExists(pageId, tenantId);

        return pageI18nRepository.findByPageId(pageId)
                .stream()
                .map(PageI18nResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PageI18nResponse publishPageI18n(Long pageId, Language language, Long tenantId, PagePublishRequest request) {
        validatePageExists(pageId, tenantId);

        PageI18n pageI18n = pageI18nRepository
                .findByPageIdAndLanguage(pageId, language)
                .orElseThrow(() -> new PageNotFoundException(
                        "PageI18n not found for pageId: " + pageId + " and language: " + language));

        validateCanPublish(pageI18n);

        if (request.isImmediatePublish()) {
            pageI18n.publish();
        } else {
            validateScheduledTime(request.scheduledAt());
            pageI18n.schedule(request.scheduledAt());
        }

        pageI18n = pageI18nRepository.save(pageI18n);
        return PageI18nResponse.from(pageI18n);
    }

    @Override
    @Transactional
    public PageI18nResponse unpublishPageI18n(Long pageId, Language language, Long tenantId) {
        validatePageExists(pageId, tenantId);

        PageI18n pageI18n = pageI18nRepository
                .findByPageIdAndLanguage(pageId, language)
                .orElseThrow(() -> new PageNotFoundException(
                        "PageI18n not found for pageId: " + pageId + " and language: " + language));

        pageI18n.unpublish();
        pageI18n = pageI18nRepository.save(pageI18n);
        return PageI18nResponse.from(pageI18n);
    }

    @Override
    @Transactional
    public void deletePageI18n(Long pageId) {
        pageI18nRepository.deleteByPageId(pageId);
    }

    private void validatePageExists(Long pageId, Long tenantId) {
        pageRepository.findById(pageId)
                .orElseThrow(() -> new PageNotFoundException(pageId));
    }

    private void validateTenantMatch(Long expectedTenantId, Long actualTenantId) {
        if (!expectedTenantId.equals(actualTenantId)) {
            throw new TenantMismatchException(expectedTenantId, actualTenantId);
        }
    }

    private void validateLanguageMatch(Language expected, Language actual) {
        if (!expected.equals(actual)) {
            throw new IllegalArgumentException(
                    "Language mismatch: URL parameter is " + expected + " but request body has " + actual);
        }
    }

    private void validateUrlPathUniqueness(Long tenantId, Language language, String urlPath, Long pageId) {
        pageI18nRepository.findByLanguageAndUrlPath(language, urlPath)
                .ifPresent(existing -> {
                    if (!existing.getPageId().equals(pageId)) {
                        throw new IllegalArgumentException(
                                "URL path '" + urlPath + "' already exists for language " + language);
                    }
                });
    }

    private void validateCanPublish(PageI18n pageI18n) {
        if (!pageI18n.canBePublished()) {
            throw new PageCannotBePublishedException(
                    "PageI18n cannot be published. Missing required fields: title and/or urlPath. " +
                            "PageId: " + pageI18n.getPageId() + ", Language: " + pageI18n.getLanguage());
        }
    }

    private void validateScheduledTime(LocalDateTime scheduledAt) {
        if (scheduledAt != null && scheduledAt.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException(
                    "Scheduled time must be in the future. Provided: " + scheduledAt);
        }
    }

    private PageI18nResponse getFallbackLanguageI18n(Long pageId, Long tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException(tenantId));

        return pageI18nRepository
                .findByPageIdAndLanguage(pageId, tenant.getDefaultLanguage())
                .map(pageI18n -> PageI18nResponse.from(pageI18n, true))
                .orElseThrow(() -> new PageNotFoundException(
                        "No i18n found for pageId: " + pageId + " in any language"));
    }

    private PageI18n updateExistingPageI18n(PageI18n existing, PageI18nRequest request) {
        if (request.urlPath() != null)
            existing.setUrlPath(request.urlPath());
        if (request.title() != null)
            existing.setTitle(request.title());
        if (request.subtitle() != null)
            existing.setSubtitle(request.subtitle());
        if (request.metaTitle() != null)
            existing.setMetaTitle(request.metaTitle());
        if (request.metaDescription() != null)
            existing.setMetaDescription(request.metaDescription());
        if (request.description() != null)
            existing.setDescription(request.description());
        if (request.descriptionHtml() != null)
            existing.setDescriptionHtml(
                    com.backend.shared.common.HtmlSanitizer.sanitizeRichText(request.descriptionHtml()));
        if (request.status() != null)
            existing.setStatus(request.status());

        return existing;
    }

    private PageI18n createNewPageI18n(Long pageId, Long tenantId, Language language, PageI18nRequest request) {
        PageI18n pageI18n = new PageI18n();
        pageI18n.setPageId(pageId);
        pageI18n.setLanguage(language);
        pageI18n.setUuid(com.backend.infrastructure.util.UuidUidGenerator.generateUuid());
        pageI18n.setUid(generateUniqueUidForI18n(tenantId));
        pageI18n.setUrlPath(request.urlPath());
        pageI18n.setTitle(request.title());
        pageI18n.setSubtitle(request.subtitle());
        pageI18n.setMetaTitle(request.metaTitle());
        pageI18n.setMetaDescription(request.metaDescription());
        pageI18n.setDescription(request.description());
        pageI18n.setDescriptionHtml(
                com.backend.shared.common.HtmlSanitizer.sanitizeRichText(request.descriptionHtml()));
        pageI18n.setStatus(request.status() != null ? request.status() : PageStatus.DRAFT);
        pageI18n.setUpdatedAt(LocalDateTime.now());

        return pageI18n;
    }

    private String generateUniqueUidForI18n(Long tenantId) {
        String uid;
        int attempts = 0;
        do {
            uid = com.backend.infrastructure.util.UuidUidGenerator.generateUid();
            attempts++;
            if (attempts > 10) {
                uid = uid + attempts;
            }
        } while (pageI18nRepository.findByUid(uid).isPresent());
        return uid;
    }
}
