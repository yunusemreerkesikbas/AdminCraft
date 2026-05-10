package com.backend.application.cms.preview;

import java.util.Set;

import com.backend.domain.enums.ComponentStatus;
import com.backend.domain.enums.PageStatus;

public final class CmsVisibility {

  private static final Set<PageStatus> PUBLISHED_PAGE_STATUSES = Set.of(PageStatus.PUBLISHED);
  private static final Set<PageStatus> PREVIEW_PAGE_STATUSES = Set.of(PageStatus.DRAFT);

  private static final Set<ComponentStatus> PUBLISHED_COMPONENT_STATUSES = Set.of(ComponentStatus.PUBLISHED);
  private static final Set<ComponentStatus> PREVIEW_COMPONENT_STATUSES = Set.of(ComponentStatus.DRAFT);

  private CmsVisibility() {}

  public static Set<PageStatus> pageStatuses(boolean preview) {
    return preview ? PREVIEW_PAGE_STATUSES : PUBLISHED_PAGE_STATUSES;
  }

  public static Set<ComponentStatus> componentStatuses(boolean preview) {
    return preview ? PREVIEW_COMPONENT_STATUSES : PUBLISHED_COMPONENT_STATUSES;
  }
}
