package com.backend.application.cms.preview;

import java.time.Instant;

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
