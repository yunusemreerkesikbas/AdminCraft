package com.backend.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.backend.domain.enums.PageStatus;

class PageI18nTest {

  @Test
  void canBePublished_ShouldAllowPublishedPageI18nForIdempotentSmartEditPublish() {
    PageI18n pageI18n = new PageI18n();
    pageI18n.setStatus(PageStatus.PUBLISHED);
    pageI18n.setTitle("Homepage");
    pageI18n.setCanonicalUrl("/");

    assertThat(pageI18n.canBePublished()).isTrue();
  }

  @Test
  void canBePublished_ShouldRejectPublishedPageI18nWhenRequiredFieldsAreMissing() {
    PageI18n pageI18n = new PageI18n();
    pageI18n.setStatus(PageStatus.PUBLISHED);

    assertThat(pageI18n.canBePublished()).isFalse();
  }
}
