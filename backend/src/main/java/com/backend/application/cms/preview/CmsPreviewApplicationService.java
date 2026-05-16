package com.backend.application.cms.preview;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.domain.constants.SiteSettingKeys;
import com.backend.domain.port.TenantContextPort;
import com.backend.domain.repository.SiteSettingRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CmsPreviewApplicationService {

    private final CmsPreviewTicketService ticketService;
    private final SiteSettingRepository siteSettingRepository;
    private final TenantContextPort tenantContext;

    @Transactional(transactionManager = "tenantTransactionManager", readOnly = true)
    public PreviewTicketResult issueTicket(Long userId, Long pageId) {
        String tenantIdStr = tenantContext.getTenantId();
        if (tenantIdStr == null || tenantIdStr.isBlank()) {
            throw new IllegalStateException("Tenant context is not set");
        }
        long tenantId = Long.parseLong(tenantIdStr);
        CmsPreviewTicket ticket = ticketService.issue(tenantId, userId, pageId);
        String token = ticketService.encode(ticket);
        String storefrontBaseUrl = resolveStorefrontBaseUrl(tenantId);

        log.debug("Issued CMS preview ticket: tenantId={}, userId={}, pageId={}, exp={}",
            tenantId, userId, pageId, ticket.expiresAt());

        return new PreviewTicketResult(token, ticket.expiresAt(), storefrontBaseUrl);
    }

    private String resolveStorefrontBaseUrl(long tenantId) {
        String tenantBaseUrl = siteSettingRepository
            .findByTenantIdAndSettingKeyAndLanguageIsNull(tenantId, SiteSettingKeys.GLOBAL_CANONICAL_BASE_URL)
            .map(s -> s.getSettingValue())
            .filter(value -> value != null && !value.isBlank())
            .orElseThrow(() -> new IllegalStateException(
                "Storefront URL not configured: set Canonical Base URL in Site Dashboard → SEO for tenant " + tenantId));
        validateStorefrontUrl(tenantBaseUrl);
        return extractOrigin(tenantBaseUrl);
    }

    private void validateStorefrontUrl(String url) {
        try {
            java.net.URI uri = new java.net.URI(url);
            String scheme = uri.getScheme();
            if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
                throw new IllegalArgumentException(
                    "Storefront base URL must use http or https scheme: " + url);
            }
            if (uri.getHost() == null || uri.getHost().isBlank()) {
                throw new IllegalArgumentException(
                    "Storefront base URL must have a valid host: " + url);
            }
        } catch (java.net.URISyntaxException ex) {
            throw new IllegalArgumentException("Storefront base URL is not a valid URI: " + url);
        }
    }

    private String extractOrigin(String url) {
        try {
            java.net.URI uri = new java.net.URI(url);
            int port = uri.getPort();
            return port == -1
                ? uri.getScheme() + "://" + uri.getHost()
                : uri.getScheme() + "://" + uri.getHost() + ":" + port;
        } catch (java.net.URISyntaxException ex) {
            throw new IllegalStateException("Failed to extract origin from storefront URL: " + url, ex);
        }
    }
}
