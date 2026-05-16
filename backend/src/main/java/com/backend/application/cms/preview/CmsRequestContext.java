package com.backend.application.cms.preview;

import org.springframework.stereotype.Component;

@Component
public class CmsRequestContext {

  private static final ThreadLocal<Boolean> PREVIEW_MODE = ThreadLocal.withInitial(() -> Boolean.FALSE);
  private static final ThreadLocal<Long> PREVIEW_PAGE_ID = new ThreadLocal<>();

  public boolean isPreview() {
    Boolean value = PREVIEW_MODE.get();
    return value != null && value;
  }

  public Long getPreviewPageId() {
    return PREVIEW_PAGE_ID.get();
  }

  public void enablePreview(Long pageId) {
    PREVIEW_MODE.set(Boolean.TRUE);
    PREVIEW_PAGE_ID.set(pageId);
  }

  public void clear() {
    PREVIEW_MODE.remove();
    PREVIEW_PAGE_ID.remove();
  }
}
