package com.backend.application.cms.preview;

import org.springframework.stereotype.Component;

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
