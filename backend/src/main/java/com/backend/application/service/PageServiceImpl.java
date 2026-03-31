package com.backend.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
import com.backend.domain.enums.ActivityAction;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.PageStatus;
import com.backend.domain.enums.RobotTag;
import com.backend.domain.exception.PageNotFoundException;
import com.backend.domain.repository.PageI18nRepository;
import com.backend.domain.repository.PageRepository;
import com.backend.domain.util.UuidUidGenerator;
import com.backend.application.dto.response.PageDetailResponse;
import com.backend.application.dto.response.PageListResponse;
import com.backend.application.dto.response.PageResponse;
import com.backend.shared.common.SecurityHelper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PageServiceImpl implements PageService {

    private final PageRepository pageRepository;
    private final PageI18nRepository pageI18nRepository;
    private final PageI18nService pageI18nService;
    private final PageTemplateService pageTemplateService;
    private final SiteActivityPublisher activityPublisher;
    private final SecurityHelper securityHelper;

    @Override
    @Transactional
    public PageResponse createPage(PageCreateRequest request, Long userId) {
        Page page = new Page();
        page.setUuid(UuidUidGenerator.generateUuid());
        page.setUid(ensureUniqueUid(request.uid()));
        page.setTemplateId(request.templateId());
        page.setStatus(request.status() != null ? request.status() : PageStatus.DRAFT);
        page.setStyleClasses(request.styleClasses());
        page.setRobotTag(request.robotTag() != null ? request.robotTag() : RobotTag.INDEX_FOLLOW);
        page.setCreatedAt(LocalDateTime.now());
        page.setUpdatedAt(LocalDateTime.now());
        page.setCreatedBy(userId);
        page.setUpdatedBy(userId);

        page = pageRepository.save(page);
        if (request.templateId() != null) {
            pageTemplateService.assignTemplateToPage(page.getId(), request.templateId());
            page = pageRepository.findById(page.getId()).orElse(page);
        }
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
        String templateUid = page.getTemplateId() != null
                ? pageTemplateService.getById(page.getTemplateId()).getUid()
                : null;
        return PageDetailResponse.from(page, i18nList, templateUid);
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
    public List<PageListResponse> getAllPagesWithTranslations() {
        List<Page> pages = pageRepository.findAll();
        List<PageI18n> allTranslations = pageI18nRepository.findAll();
        Map<Long, List<PageI18n>> translationsByPage = allTranslations.stream()
                .collect(Collectors.groupingBy(PageI18n::getPageId));
        return pages.stream()
                .map(page -> PageListResponse.from(
                        page,
                        translationsByPage.getOrDefault(page.getId(), java.util.Collections.emptyList())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PageResponse updatePage(Long id, PageCreateRequest request, Long userId) {
        Page page = pageRepository.findById(id)
                .orElseThrow(() -> new PageNotFoundException(id));

        Long oldTemplateId = page.getTemplateId();
        Long newTemplateId = request.templateId();

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
        if (!Objects.equals(oldTemplateId, newTemplateId)) {
            pageTemplateService.assignTemplateToPage(id, newTemplateId);
            page = pageRepository.findById(id).orElse(page);
        }
        return PageResponse.from(page);
    }

    @Override
    @Transactional
    public void deletePage(Long id) {
        Page page = pageRepository.findById(id).orElse(null);
        String pageName = page != null ? page.getUid() : String.valueOf(id);
        pageI18nService.deletePageI18n(id);
        pageRepository.deleteById(id);
        activityPublisher.publishPageEvent(id, pageName, ActivityAction.DELETED,
                securityHelper.getCurrentUserIdOrNull(), null, null);
    }

    // ==================== Composite Operations (Sprint 34 Pattern)
    // ====================

    @Override
    @Transactional
    public PageDetailResponse createComposite(CreatePageCompositeRequest request, Long userId) {
        // 1. Create the page (without templateId - will be set by assignTemplateToPage)
        Page page = new Page();
        page.setUuid(UuidUidGenerator.generateUuid());
        page.setUid(ensureUniqueUid(request.uid()));
        page.setStatus(request.status() != null ? request.status() : PageStatus.DRAFT);
        page.setStyleClasses(request.styleClasses());
        page.setRobotTag(request.robotTag() != null ? request.robotTag() : RobotTag.INDEX_FOLLOW);
        page.setCreatedAt(LocalDateTime.now());
        page.setUpdatedAt(LocalDateTime.now());
        page.setCreatedBy(userId);
        page.setUpdatedBy(userId);

        page = pageRepository.save(page);
        final Long pageId = page.getId();

        // 2. Assign template and create slots (if templateId provided)
        if (request.templateId() != null) {
            pageTemplateService.assignTemplateToPage(pageId, request.templateId());
            // Refresh page to get updated templateId
            page = pageRepository.findById(pageId).orElse(page);
        }

        // 3. Create translations
        upsertTranslations(pageId, request.translations(), request.status());

        // 4. Return composite response
        List<PageI18n> i18nList = pageI18nRepository.findByPageId(pageId);
        String templateUid = page.getTemplateId() != null
                ? pageTemplateService.getById(page.getTemplateId()).getUid()
                : null;
        String pageName = i18nList.stream().map(PageI18n::getName).filter(n -> n != null && !n.isBlank())
                .findFirst().orElse(page.getUid());
        activityPublisher.publishPageEvent(page.getId(), pageName, ActivityAction.CREATED, userId, null, null);
        return PageDetailResponse.from(page, i18nList, templateUid);
    }

    @Override
    @Transactional
    public PageDetailResponse updateComposite(Long id, UpdatePageCompositeRequest request, Long userId) {
        // 1. Update the page
        Page page = pageRepository.findById(id)
                .orElseThrow(() -> new PageNotFoundException(id));

        Long oldTemplateId = page.getTemplateId();
        Long newTemplateId = request.templateId();

        if (request.uid() != null && !request.uid().isBlank()) {
            String newUid = ensureUniqueUidForUpdate(id, request.uid().trim());
            page.setUid(newUid);
        }
        if (request.status() != null)
            page.setStatus(request.status());
        if (request.styleClasses() != null)
            page.setStyleClasses(request.styleClasses());
        if (request.robotTag() != null)
            page.setRobotTag(request.robotTag());

        page.setUpdatedAt(LocalDateTime.now());
        page.setUpdatedBy(userId);

        page = pageRepository.save(page);

        // 2. Handle template change - create missing slots
        boolean templateChanged = (newTemplateId != null && !newTemplateId.equals(oldTemplateId))
                || (newTemplateId == null && oldTemplateId != null);
        if (templateChanged) {
            pageTemplateService.assignTemplateToPage(id, newTemplateId);
            // Refresh page to get updated templateId
            page = pageRepository.findById(id).orElse(page);
        }

        // 3. Update translations
        upsertTranslations(id, request.translations(), page.getStatus());

        // 4. Return composite response
        List<PageI18n> i18nList = pageI18nRepository.findByPageId(id);
        String templateUid = page.getTemplateId() != null
                ? pageTemplateService.getById(page.getTemplateId()).getUid()
                : null;
        String pageName = i18nList.stream().map(PageI18n::getName).filter(n -> n != null && !n.isBlank())
                .findFirst().orElse(page.getUid());
        activityPublisher.publishPageEvent(id, pageName, ActivityAction.UPDATED, userId, null, null);
        return PageDetailResponse.from(page, i18nList, templateUid);
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
                    null);

            pageI18nService.upsertPageI18n(pageId, lang, pageI18nRequest);
        }
    }

    private String ensureUniqueUid(String desiredUid) {
        if (desiredUid == null || desiredUid.isEmpty()) {
            return generateUniqueUidForPage();
        }

        String uid = desiredUid;
        if (!pageRepository.existsByUid(uid)) {
            return uid;
        }

        int counter = 1;
        while (pageRepository.existsByUid(uid + "-" + counter)) {
            counter++;
        }
        return uid + "-" + counter;
    }

    private String ensureUniqueUidForUpdate(Long pageId, String desiredUid) {
        if (desiredUid == null || desiredUid.isEmpty()) {
            return pageRepository.findById(pageId).map(Page::getUid).orElseGet(this::generateUniqueUidForPage);
        }
        Optional<Page> existing = pageRepository.findByUid(desiredUid);
        if (existing.isEmpty() || existing.get().getId().equals(pageId)) {
            return desiredUid;
        }
        int counter = 1;
        while (pageRepository.existsByUid(desiredUid + "-" + counter)) {
            counter++;
        }
        return desiredUid + "-" + counter;
    }

    private String generateUniqueUidForPage() {
        String uid;
        int attempts = 0;
        do {
            uid = UuidUidGenerator.generateUid();
            attempts++;
            if (attempts > 10) {
                uid = uid + attempts; // slight perturbation
            }
        } while (pageRepository.existsByUid(uid));
        return uid;
    }

}
