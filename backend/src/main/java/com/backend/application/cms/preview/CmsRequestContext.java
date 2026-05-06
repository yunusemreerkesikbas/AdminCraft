package com.backend.application.cms.preview;

import org.springframework.stereotype.Component;

/**
 * Request-scoped context describing how a CMS delivery call should resolve
 * draft vs. published content. Implemented as a {@link ThreadLocal} so the
 * existing public delivery code path can opt-in without changing its method
 * signatures (mirrors the {@link com.backend.domain.port.TenantContextPort}
 * pattern already used across the codebase).
 *
 * <p>The context is populated by the CMS preview request filter when a valid
 * preview ticket is supplied and cleared at the end of the request.</p>
 */
@Component
public class CmsRequestContext {

  private static final ThreadLocal<Boolean> PREVIEW_MODE = ThreadLocal.withInitial(() -> Boolean.FALSE);

  public boolean isPreview() {
    Boolean value = PREVIEW_MODE.get();
    return value != null && value;
  }

  public void enablePreview() {
    PREVIEW_MODE.set(Boolean.TRUE);
  }

  public void clear() {
    PREVIEW_MODE.remove();
  }
}
