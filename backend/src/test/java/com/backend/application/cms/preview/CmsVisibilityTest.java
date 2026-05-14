package com.backend.application.cms.preview;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.backend.domain.enums.ComponentStatus;
import com.backend.domain.enums.PageStatus;

class CmsVisibilityTest {

  @Test
  void pageStatuses_ShouldExposeOnlyPublishedForLiveStorefront() {
    assertThat(CmsVisibility.pageStatuses(false))
        .containsExactly(PageStatus.PUBLISHED);
  }

  @Test
  void pageStatuses_ShouldExposeDraftAndPublishedFallbackForPreview() {
    assertThat(CmsVisibility.pageStatuses(true))
        .containsExactlyInAnyOrder(PageStatus.DRAFT, PageStatus.PUBLISHED);
  }

  @Test
  void componentStatuses_ShouldExposeOnlyPublishedForLiveStorefront() {
    assertThat(CmsVisibility.componentStatuses(false))
        .containsExactly(ComponentStatus.PUBLISHED);
  }

  @Test
  void componentStatuses_ShouldExposeDraftAndPublishedFallbackForPreview() {
    assertThat(CmsVisibility.componentStatuses(true))
        .containsExactlyInAnyOrder(ComponentStatus.DRAFT, ComponentStatus.PUBLISHED);
  }
}
