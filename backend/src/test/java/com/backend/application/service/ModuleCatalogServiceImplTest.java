package com.backend.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.backend.application.dto.provisioning.ModuleCatalogResponse;
import com.backend.infrastructure.persistence.platform.entity.ModuleCatalog;
import com.backend.infrastructure.persistence.platform.repository.ModuleCatalogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class ModuleCatalogServiceImplTest {

  @Mock
  private ModuleCatalogRepository moduleCatalogRepository;

  private ModuleCatalogServiceImpl moduleCatalogService;

  @BeforeEach
  void setUp() {
    moduleCatalogService = new ModuleCatalogServiceImpl(moduleCatalogRepository, new ObjectMapper());
  }

  @Test
  void shouldReturnOnlyCoreAndProductInStableOrder() {
    ModuleCatalog product = ModuleCatalog.builder()
        .code("product")
        .name("Product Catalog")
        .type("b2c")
        .version("1.0.0")
        .deps("[\"core\"]")
        .enabledByDefault(false)
        .description("Optional product module")
        .build();

    ModuleCatalog pageBuilder = ModuleCatalog.builder()
        .code("pagebuilder")
        .name("Page Builder")
        .type("core")
        .version("1.0.0")
        .deps("[\"core\"]")
        .enabledByDefault(false)
        .description("Core capability")
        .build();

    ModuleCatalog core = ModuleCatalog.builder()
        .code("core")
        .name("Core Module")
        .type("core")
        .version("1.0.0")
        .deps(null)
        .enabledByDefault(true)
        .description("Required module")
        .build();

    when(moduleCatalogRepository.findAll()).thenReturn(List.of(product, pageBuilder, core));

    List<ModuleCatalogResponse> result = moduleCatalogService.getAllModules();

    assertThat(result).extracting(ModuleCatalogResponse::getCode)
        .containsExactly("core", "product");
  }
}
