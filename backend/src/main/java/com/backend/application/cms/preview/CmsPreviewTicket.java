package com.backend.application.cms.preview;

import java.time.Instant;

/**
 * Verified preview ticket payload. Minted for an authenticated admin user;
 * carried by the storefront iframe back to the public CMS delivery API so it
 * may serve DRAFT content for that single tenant.
 */
public record CmsPreviewTicket(
    long tenantId,
    Long userId,
    Long pageId,
    Instant issuedAt,
    Instant expiresAt) {

  public boolean matchesTenant(long currentTenantId) {
    return tenantId == currentTenantId;
  }

  public boolean isExpired(Instant now) {
    return now.isAfter(expiresAt);
  }
}
