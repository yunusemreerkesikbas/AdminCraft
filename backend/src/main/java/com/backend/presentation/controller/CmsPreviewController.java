package com.backend.presentation.controller;

import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.application.cms.preview.CmsPreviewProperties;
import com.backend.application.cms.preview.CmsPreviewTicket;
import com.backend.application.cms.preview.CmsPreviewTicketService;
import com.backend.domain.constants.SiteSettingKeys;
import com.backend.domain.port.TenantContextPort;
import com.backend.domain.repository.SiteSettingRepository;
import com.backend.presentation.dto.request.IssuePreviewTicketRequest;
import com.backend.presentation.dto.response.PreviewTicketResponse;
import com.backend.shared.common.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Issues short-lived signed preview tickets for the SmartEdit admin shell.
 * Tickets are tenant-bound and verified server-side by {@code CmsDeliveryController}
 * before serving DRAFT content.
 */
@RestController
@RequestMapping("/cms/preview")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "CMS Preview", description = "Signed preview tickets for SmartEdit-style admin editing")
public class CmsPreviewController {

  private final CmsPreviewTicketService ticketService;
  private final CmsPreviewProperties properties;
  private final TenantContextPort tenantContext;
  private final SiteSettingRepository siteSettingRepository;

  @PreAuthorize("hasRole('TENANT_ADMIN')")
  @PostMapping("/tickets")
  @Transactional(transactionManager = "tenantTransactionManager", readOnly = true)
  @Operation(summary = "Issue preview ticket",
      description = "Returns a short-lived HMAC-signed token that the storefront iframe uses to fetch DRAFT content")
  public ResponseEntity<ApiResponse<PreviewTicketResponse>> issueTicket(
      @RequestBody(required = false) IssuePreviewTicketRequest request) {
    long tenantId = Long.parseLong(tenantContext.getTenantId());
    Long userId = currentUserId();
    Long pageId = Optional.ofNullable(request).map(IssuePreviewTicketRequest::pageId).orElse(null);

    CmsPreviewTicket ticket = ticketService.issue(tenantId, userId, pageId);
    String token = ticketService.encode(ticket);
    String storefrontBaseUrl = resolveStorefrontBaseUrl(tenantId);

    log.debug("Issued CMS preview ticket: tenantId={}, userId={}, pageId={}, exp={}",
        tenantId, userId, pageId, ticket.expiresAt());

    PreviewTicketResponse response = new PreviewTicketResponse(
        token, ticket.expiresAt(), storefrontBaseUrl);
    return ResponseEntity.ok(ApiResponse.success("Preview ticket issued", response));
  }

  /**
   * Resolve the storefront origin to embed in the SmartEdit iframe.
   *
   * <p>Priority order:</p>
   * <ol>
   *   <li>{@code app.cms.preview.storefront-base-url} (platform config) — wins
   *       whenever it is set. In dev this points at {@code http://localhost:3000};
   *       in prod ops can pin a single shared preview origin here.</li>
   *   <li>Tenant {@code global.canonicalBaseUrl} site setting — used only when
   *       the platform config is left blank. Suitable for subdomain-per-tenant
   *       deployments where each tenant's storefront lives at its own canonical
   *       host. Note: {@code canonicalBaseUrl} is intended for SEO/sitemap and
   *       may carry a placeholder in dev tenants — that is why platform config
   *       wins by default.</li>
   * </ol>
   */
  private String resolveStorefrontBaseUrl(long tenantId) {
    String platformBaseUrl = properties.getStorefrontBaseUrl();
    if (platformBaseUrl != null && !platformBaseUrl.isBlank()) {
      return platformBaseUrl;
    }

    String tenantBaseUrl = siteSettingRepository
        .findByTenantIdAndLanguageIsNull(tenantId)
        .stream()
        .filter(s -> SiteSettingKeys.GLOBAL_CANONICAL_BASE_URL.equals(s.getSettingKey()))
        .map(s -> s.getSettingValue())
        .filter(value -> value != null && !value.isBlank())
        .findFirst()
        .orElseThrow(() -> new IllegalStateException(
            "No storefront base URL available: configure app.cms.preview.storefront-base-url"
                + " or set global.canonicalBaseUrl on tenant " + tenantId));

    log.debug("Using tenant {} canonicalBaseUrl as SmartEdit storefront origin", tenantId);
    return tenantBaseUrl;
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
