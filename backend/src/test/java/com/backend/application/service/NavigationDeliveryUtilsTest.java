package com.backend.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class NavigationDeliveryUtilsTest {

  @Test
  void resolveLocalizedHref_ShouldAllowRelativeHashAndWhitelistedSchemes() {
    assertThat(NavigationDeliveryUtils.resolveLocalizedHref("/about", false, "tr"))
        .isEqualTo("/tr/about");
    assertThat(NavigationDeliveryUtils.resolveLocalizedHref("#contact", false, "tr"))
        .isEqualTo("#contact");
    assertThat(NavigationDeliveryUtils.resolveLocalizedHref("https://example.com", true, "tr"))
        .isEqualTo("https://example.com");
    assertThat(NavigationDeliveryUtils.resolveLocalizedHref("mailto:hello@example.com", true, "tr"))
        .isEqualTo("mailto:hello@example.com");
    assertThat(NavigationDeliveryUtils.resolveLocalizedHref("tel:+15551234567", true, "tr"))
        .isEqualTo("tel:+15551234567");
  }

  @Test
  void resolveLocalizedHref_ShouldRejectUnsafeSchemesAndControlCharacters() {
    assertThat(NavigationDeliveryUtils.resolveLocalizedHref("javascript:alert(1)", true, "tr"))
        .isNull();
    assertThat(NavigationDeliveryUtils.resolveLocalizedHref("data:text/html,hello", true, "tr"))
        .isNull();
    assertThat(NavigationDeliveryUtils.resolveLocalizedHref("//example.com", true, "tr"))
        .isNull();
    assertThat(NavigationDeliveryUtils.resolveLocalizedHref("/safe\npath", false, "tr"))
        .isNull();
  }
}
