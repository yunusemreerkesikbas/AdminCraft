package com.backend.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class TenantMigrationServiceTest {

  private final TenantMigrationService tenantMigrationService = new TenantMigrationService();

  @Test
  void shouldOrderCommerceAfterProduct() {
    List<String> ordered = tenantMigrationService.getOrderedModules(List.of(
        "commerce",
        "product",
        "core",
        "pagebuilder",
        "component_library",
        "media",
        "mail_marketing"));

    assertThat(ordered).containsExactly(
        "core",
        "mail_marketing",
        "media",
        "component_library",
        "pagebuilder",
        "product",
        "commerce");
  }
}
