package com.backend.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.backend.application.dto.tenant.TenantModuleResponse;
import com.backend.domain.entity.Tenant;
import com.backend.domain.enums.Language;
import com.backend.domain.repository.TenantRepository;
import com.backend.infrastructure.persistence.platform.entity.ModuleCatalog;
import com.backend.infrastructure.persistence.platform.entity.TenantModule;
import com.backend.infrastructure.persistence.platform.repository.TenantModuleRepository;

@ExtendWith(MockitoExtension.class)
class TenantModuleServiceTest {

        @Mock
        private TenantRepository tenantRepository;

        @Mock
        private TenantModuleRepository tenantModuleRepository;

        @Mock
        private com.backend.infrastructure.tenant.TenantContext tenantContext;

        @InjectMocks
        private TenantServiceImpl tenantService;

        private Tenant testTenant;
        private List<TenantModule> testModules;

        @BeforeEach
        void setUp() {
                testTenant = new Tenant();
                testTenant.setId(1L);
                testTenant.setSubdomain("test-tenant");
                testTenant.setCompanyName("Test Company");

                ModuleCatalog coreCatalog = ModuleCatalog.builder()
                                .code("core")
                                .name("Core System")
                                .build();

                ModuleCatalog productCatalog = ModuleCatalog.builder()
                .code("product")
                .name("Product Catalog")
                                .build();

                TenantModule coreModule = TenantModule.builder()
                                .id(1L)
                                .tenantId(1L)
                                .moduleCode("core")
                                .status("enabled")
                                .installedAt(LocalDateTime.now())
                                .moduleCatalog(coreCatalog)
                                .build();

                TenantModule productModule = TenantModule.builder()
                                .id(2L)
                                .tenantId(1L)
                .moduleCode("product")
                                .status("enabled")
                                .installedAt(LocalDateTime.now())
                                .moduleCatalog(productCatalog)
                                .build();

                testModules = Arrays.asList(coreModule, productModule);
        }

        @Test
        void shouldReturnTenantModulesWhenTenantExists() {
                // Given
                when(tenantRepository.findById(1L)).thenReturn(Optional.of(testTenant));
                when(tenantModuleRepository.findByTenantIdAndStatus(1L, "enabled"))
                                .thenReturn(testModules);

                // When
                List<TenantModuleResponse> result = tenantService.getTenantModules(1L, Language.EN);

                // Then
                assertThat(result).hasSize(2);
                assertThat(result.get(0).getModuleCode()).isEqualTo("core");
                assertThat(result.get(0).getModuleName()).isEqualTo("Core System");
                assertThat(result.get(0).getStatus()).isEqualTo("enabled");
        assertThat(result.get(1).getModuleCode()).isEqualTo("product");
        assertThat(result.get(1).getModuleName()).isEqualTo("Product Catalog");

                verify(tenantRepository).findById(1L);
                verify(tenantModuleRepository).findByTenantIdAndStatus(1L, "enabled");
        }

        @Test
        void shouldThrowExceptionWhenTenantNotFound() {
                // Given
                when(tenantRepository.findById(999L)).thenReturn(Optional.empty());

                // When & Then
                assertThatThrownBy(() -> tenantService.getTenantModules(999L, Language.EN))
                                .isInstanceOf(com.backend.domain.exception.TenantNotFoundException.class)
                                .hasMessageContaining("Tenant not found with ID: 999");

                verify(tenantRepository).findById(999L);
                verify(tenantModuleRepository, never()).findByTenantIdAndStatus(anyLong(), anyString());
        }

        @Test
        void shouldReturnEmptyListWhenTenantHasNoModules() {
                // Given
                when(tenantRepository.findById(1L)).thenReturn(Optional.of(testTenant));
                when(tenantModuleRepository.findByTenantIdAndStatus(1L, "enabled"))
                                .thenReturn(List.of());

                // When
                List<TenantModuleResponse> result = tenantService.getTenantModules(1L, Language.EN);

                // Then
                assertThat(result).isEmpty();
                verify(tenantRepository).findById(1L);
                verify(tenantModuleRepository).findByTenantIdAndStatus(1L, "enabled");
        }

        @Test
        void shouldHandleModuleWithoutCatalog() {
                // Given
                TenantModule moduleWithoutCatalog = TenantModule.builder()
                                .id(3L)
                                .tenantId(1L)
                .moduleCode("product")
                                .status("enabled")
                                .installedAt(LocalDateTime.now())
                                .moduleCatalog(null)
                                .build();

                when(tenantRepository.findById(1L)).thenReturn(Optional.of(testTenant));
                when(tenantModuleRepository.findByTenantIdAndStatus(1L, "enabled"))
                                .thenReturn(List.of(moduleWithoutCatalog));

                // When
                List<TenantModuleResponse> result = tenantService.getTenantModules(1L, Language.EN);

                // Then
                assertThat(result).hasSize(1);
        assertThat(result.get(0).getModuleCode()).isEqualTo("product");
        assertThat(result.get(0).getModuleName()).isEqualTo("product");
        }

        @Test
        void shouldOnlyReturnEnabledModules() {
                // Given
                when(tenantRepository.findById(1L)).thenReturn(Optional.of(testTenant));
                when(tenantModuleRepository.findByTenantIdAndStatus(1L, "enabled"))
                                .thenReturn(testModules);

                // When
                List<TenantModuleResponse> result = tenantService.getTenantModules(1L, Language.EN);

                // Then
                assertThat(result).hasSize(2);
                assertThat(result).extracting(TenantModuleResponse::getStatus)
                                .allMatch(status -> status.equals("enabled"));
                assertThat(result).extracting(TenantModuleResponse::getModuleCode)
                                .doesNotContain("disabled_module");
        }

    @Test
    void shouldFilterOutInternalCoreExecutionModules() {
        TenantModule internalModule = TenantModule.builder()
                .id(3L)
                .tenantId(1L)
                .moduleCode("pagebuilder")
                .status("enabled")
                .installedAt(LocalDateTime.now())
                .moduleCatalog(ModuleCatalog.builder().code("pagebuilder").name("Page Builder").build())
                .build();

        when(tenantRepository.findById(1L)).thenReturn(Optional.of(testTenant));
        when(tenantModuleRepository.findByTenantIdAndStatus(1L, "enabled"))
                .thenReturn(List.of(internalModule));

        List<TenantModuleResponse> result = tenantService.getTenantModules(1L, Language.EN);

        assertThat(result).isEmpty();
    }
}
