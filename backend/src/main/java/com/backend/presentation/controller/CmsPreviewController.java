package com.backend.presentation.controller;

import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.application.cms.preview.CmsPreviewApplicationService;
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

    @PreAuthorize("hasRole('TENANT_ADMIN')")
    @PostMapping("/tickets")
    @Operation(summary = "Issue preview ticket",
        description = "Returns a short-lived HMAC-signed token that the storefront iframe uses to fetch DRAFT content")
    public ResponseEntity<ApiResponse<PreviewTicketResponse>> issueTicket(
        @RequestBody(required = false) @Valid PreviewTicketIssueRequest request) {
        Long userId = currentUserId();
        Long pageId = Optional.ofNullable(request).map(PreviewTicketIssueRequest::pageId).orElse(null);
        PreviewTicketResponse response = applicationService.issueTicket(userId, pageId);
        return ResponseEntity.ok(ApiResponse.success("Preview ticket issued", response));
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
