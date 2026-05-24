package com.backend.presentation.controller;

import java.util.Map;
import java.util.Optional;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.application.cms.preview.CmsDraftOverrideService;
import com.backend.application.cms.preview.CmsPreviewApplicationService;
import com.backend.application.cms.preview.PreviewTicketResult;
import com.backend.application.cms.preview.SmartEditDraftOverviewResponse;
import com.backend.domain.enums.Language;
import com.backend.presentation.dto.request.PreviewTicketIssueRequest;
import com.backend.presentation.dto.response.PreviewTicketResponse;
import com.backend.shared.common.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/cms/preview")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "CMS Preview", description = "Signed preview tickets for SmartEdit-style admin editing")
public class CmsPreviewController {

    private final CmsPreviewApplicationService applicationService;
    private final CmsDraftOverrideService cmsDraftOverrideService;
    private final MessageSource messageSource;

    @PreAuthorize("hasRole('TENANT_ADMIN')")
    @PostMapping("/tickets")
    @Operation(summary = "Issue preview ticket",
        description = "Returns a short-lived HMAC-signed token that the storefront iframe uses to fetch DRAFT content")
    public ResponseEntity<ApiResponse<PreviewTicketResponse>> issueTicket(
        @RequestBody(required = false) @Valid PreviewTicketIssueRequest request,
        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
        Long userId = currentUserId();
        Long pageId = Optional.ofNullable(request).map(PreviewTicketIssueRequest::pageId).orElse(null);
        PreviewTicketResult result = applicationService.issueTicket(userId, pageId);
        PreviewTicketResponse response = new PreviewTicketResponse(
            result.ticket(), result.expiresAt(), result.storefrontBaseUrl());
        return ResponseEntity.ok(ApiResponse.success(message("cms.preview.ticket.issue.success", lang), response));
    }

    @PreAuthorize("hasRole('TENANT_ADMIN')")
    @GetMapping("/pages/{pageId}/drafts")
    @Operation(summary = "List SmartEdit drafts for a page and language")
    public ResponseEntity<ApiResponse<SmartEditDraftOverviewResponse>> listPageDrafts(
        @PathVariable Long pageId,
        @RequestParam Language language,
        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
        Locale locale = Locale.forLanguageTag(lang);
        return ResponseEntity.ok(ApiResponse.success(
            message("cms.preview.drafts.list.success", lang),
            cmsDraftOverrideService.listPageDrafts(pageId, language, locale)));
    }

    @PreAuthorize("hasRole('TENANT_ADMIN')")
    @GetMapping("/pages/{pageId}/publish-review")
    @Operation(summary = "Build field-level SmartEdit publish review")
    public ResponseEntity<ApiResponse<SmartEditDraftOverviewResponse>> publishReview(
        @PathVariable Long pageId,
        @RequestParam Language language,
        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
        Locale locale = Locale.forLanguageTag(lang);
        return ResponseEntity.ok(ApiResponse.success(
            message("cms.preview.publishReview.success", lang),
            cmsDraftOverrideService.buildPublishReview(pageId, language, locale)));
    }

    @PreAuthorize("hasRole('TENANT_ADMIN')")
    @DeleteMapping("/drafts/{draftId}")
    @Operation(summary = "Discard one SmartEdit draft override")
    public ResponseEntity<ApiResponse<Void>> discardDraft(
        @PathVariable Long draftId,
        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
        cmsDraftOverrideService.discardDraft(draftId, currentUserId());
        return ResponseEntity.ok(ApiResponse.success(message("cms.preview.draft.discard.success", lang), null));
    }

    @PreAuthorize("hasRole('TENANT_ADMIN')")
    @DeleteMapping("/pages/{pageId}/drafts")
    @Operation(summary = "Discard current-language SmartEdit drafts for a page")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> discardPageDrafts(
        @PathVariable Long pageId,
        @RequestParam Language language,
        @RequestHeader(value = "Accept-Language", defaultValue = "tr") String lang) {
        int deletedCount = cmsDraftOverrideService.discardPageDrafts(pageId, language, currentUserId());
        return ResponseEntity.ok(ApiResponse.success(
            message("cms.preview.drafts.discard.success", lang),
            Map.of("deletedCount", deletedCount)));
    }

    private String message(String key, String lang) {
        return messageSource.getMessage(key, null, Locale.forLanguageTag(lang));
    }

    private Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getDetails() instanceof Map<?, ?> details)) {
            return null;
        }
        Object userId = details.get("userId");
        if (userId instanceof Number number) {
            return number.longValue();
        }
        return null;
    }
}
