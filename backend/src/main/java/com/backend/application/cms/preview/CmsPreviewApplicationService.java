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
    private final CmsPreviewProperties properties;
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
        String platformBaseUrl = properties.getStorefrontBaseUrl();
        if (platformBaseUrl != null && !platformBaseUrl.isBlank()) {
            validateStorefrontUrl(platformBaseUrl);
            return platformBaseUrl;
        }

        String tenantBaseUrl = siteSettingRepository
            .findByTenantIdAndSettingKeyAndLanguageIsNull(tenantId, SiteSettingKeys.GLOBAL_CANONICAL_BASE_URL)
            .map(s -> s.getSettingValue())
            .filter(value -> value != null && !value.isBlank())
            .orElseThrow(() -> new IllegalStateException(
                "No storefront base URL available: configure app.cms.preview.storefront-base-url"
                    + " or set global.canonicalBaseUrl on tenant " + tenantId));
        validateStorefrontUrl(tenantBaseUrl);
        return tenantBaseUrl;
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
}
