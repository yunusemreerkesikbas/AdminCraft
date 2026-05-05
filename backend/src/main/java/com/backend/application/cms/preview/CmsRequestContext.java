package com.backend.application.cms.preview;

import org.springframework.stereotype.Component;

/**
 * Request-scoped context describing how a CMS delivery call should resolve
 * draft vs. published content. Implemented as a {@link ThreadLocal} so the
 * existing public delivery code path can opt-in without changing its method
 * signatures (mirrors the {@link com.backend.infrastructure.tenant.TenantContext}
 * pattern already used across the codebase).
 *
 * <p>The context is populated by
 * {@link com.backend.presentation.filter.CmsPreviewFilter} when a valid
 * preview ticket is supplied and cleared at the end of the request.</p>
 */
@Component
public class CmsRequestContext {

  private static final ThreadLocal<Boolean> previewMode = ThreadLocal.withInitial(() -> Boolean.FALSE);

  public boolean isPreview() {
    Boolean value = previewMode.get();
    return value != null && value;
  }

  public void enablePreview() {
    previewMode.set(Boolean.TRUE);
  }

  public void clear() {
    previewMode.remove();
  }
}
