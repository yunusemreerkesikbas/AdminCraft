package com.backend.application.service;

import com.backend.domain.entity.Tenant;
import com.backend.domain.entity.User;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.TenantStatus;
import com.backend.domain.repository.TenantRepository;
import com.backend.domain.repository.UserRepository;
import com.backend.infrastructure.persistence.platform.entity.ProvisioningJob;
import com.backend.infrastructure.persistence.platform.repository.ProvisioningJobRepository;
import com.backend.infrastructure.persistence.platform.repository.TenantModuleRepository;
import com.backend.application.command.CreateTenantCommand;
import com.backend.application.command.UpdateTenantCommand;
import com.backend.presentation.dto.response.TenantDetailResponse;
import com.backend.presentation.dto.response.TenantListResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TenantServiceImpl - Sprint 18
 * Tests new DTO methods and provisioning status logic
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TenantServiceImpl Tests - Sprint 18")
class TenantServiceImplTest {

        @Mock
        private TenantRepository tenantRepository;

        @Mock
        private UserRepository userRepository;

        @Mock
        private ProvisioningService provisioningService;

        @Mock
        private TenantModuleRepository tenantModuleRepository;

        @Mock
        private ProvisioningJobRepository provisioningJobRepository;

        @InjectMocks
        private TenantServiceImpl tenantService;

        private Tenant testTenant;
        private CreateTenantCommand createRequest;
        private UpdateTenantCommand updateRequest;

        @BeforeEach
        void setUp() {
                testTenant = new Tenant();
                testTenant.setId(1L);
                testTenant.setSubdomain("testcompany");
                testTenant.setCompanyName("Test Company");
                testTenant.setDatabaseName("ac_tenant_1");
                testTenant.setStatus(TenantStatus.ACTIVE);
                testTenant.setDefaultLanguage(Language.TR);
                testTenant.setSupportedLanguages(new HashSet<>(Set.of(Language.TR, Language.EN)));
                testTenant.setCustomDomain("testcompany.com");
                testTenant.setStorageUsedMb(100L);
                testTenant.setCreatedAt(LocalDateTime.now());
                testTenant.setUpdatedAt(LocalDateTime.now());

                createRequest = new CreateTenantCommand(
                                "newcompany",
                                "New Company",
                                Language.TR,
                                new HashSet<>(Set.of(Language.TR, Language.EN)),
                                "Test notes");

                updateRequest = new UpdateTenantCommand(
                                "Updated Company",
                                Language.EN,
                                new HashSet<>(Set.of(Language.EN, Language.TR)),
                                "updated.com",
                                "Updated notes");
        }

        // ========================================
        // CREATE TENANT TESTS
        // ========================================

        @Test
        @DisplayName("Should create tenant with detail response successfully")
        void testCreateTenantWithDetail_Success() {
                // Given
                when(tenantRepository.existsBySubdomain(anyString())).thenReturn(false);
                when(tenantRepository.save(any(Tenant.class))).thenReturn(testTenant);
                when(provisioningJobRepository.findFirstByTenantIdOrderByCreatedAtDesc(anyLong()))
                                .thenReturn(Optional.empty());
                when(tenantModuleRepository.countEnabledModulesByTenantId(anyLong()))
                                .thenReturn(0);

                // When
                TenantDetailResponse response = tenantService.createTenantWithDetail(createRequest, Language.TR);

                // Then
                assertThat(response).isNotNull();
                assertThat(response.id()).isEqualTo(1L);
                assertThat(response.companyName()).isEqualTo("Test Company");
                assertThat(response.provisioningStatus()).isEqualTo("idle");
                assertThat(response.provisionedModulesCount()).isEqualTo(0);

                verify(tenantRepository).existsBySubdomain("newcompany");
                verify(tenantRepository).save(any(Tenant.class));
                verify(provisioningJobRepository).findFirstByTenantIdOrderByCreatedAtDesc(1L);
                verify(tenantModuleRepository).countEnabledModulesByTenantId(1L);
        }

        @Test
        @DisplayName("Should throw exception when creating tenant with duplicate subdomain")
        void testCreateTenantWithDetail_DuplicateSubdomain() {
                // Given
                when(tenantRepository.existsBySubdomain(anyString())).thenReturn(true);

                // When/Then
                assertThatThrownBy(() -> tenantService.createTenantWithDetail(createRequest, Language.TR))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("Subdomain already exists");

                verify(tenantRepository).existsBySubdomain("newcompany");
                verify(tenantRepository, never()).save(any(Tenant.class));
        }

        @Test
        @DisplayName("Should throw exception when creating tenant with reserved subdomain")
        void testCreateTenantWithDetail_ReservedSubdomain() {
                CreateTenantCommand reservedRequest = new CreateTenantCommand(
                                "admin",
                                "Company",
                                Language.TR,
                                new HashSet<>(Set.of(Language.TR)),
                                null);

                assertThatThrownBy(() -> tenantService.createTenantWithDetail(reservedRequest, Language.TR))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("reserved");

                verify(tenantRepository, never()).save(any(Tenant.class));
        }

        // ========================================
        // UPDATE TENANT TESTS
        // ========================================

        @Test
        @DisplayName("Should update tenant with detail response successfully")
        void testUpdateTenantWithDetail_Success() {
                // Given
                when(tenantRepository.findById(1L)).thenReturn(Optional.of(testTenant));
                when(tenantRepository.existsByCustomDomainAndIdNot(anyString(), anyLong())).thenReturn(false);
                when(tenantRepository.save(any(Tenant.class))).thenReturn(testTenant);
                when(provisioningJobRepository.findFirstByTenantIdOrderByCreatedAtDesc(anyLong()))
                                .thenReturn(Optional.empty());
                when(tenantModuleRepository.countEnabledModulesByTenantId(anyLong()))
                                .thenReturn(3);

                // When
                TenantDetailResponse response = tenantService.updateTenantWithDetail(1L, updateRequest, Language.EN);

                // Then
                assertThat(response).isNotNull();
                assertThat(response.id()).isEqualTo(1L);
                assertThat(response.provisioningStatus()).isEqualTo("idle");
                assertThat(response.provisionedModulesCount()).isEqualTo(3);

                verify(tenantRepository).findById(1L);
                verify(tenantRepository).save(any(Tenant.class));
        }

        @Test
        @DisplayName("Should throw exception when updating non-existent tenant")
        void testUpdateTenantWithDetail_TenantNotFound() {
                // Given
                when(tenantRepository.findById(999L)).thenReturn(Optional.empty());

                // When/Then
                assertThatThrownBy(() -> tenantService.updateTenantWithDetail(999L, updateRequest, Language.TR))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("Tenant not found");

                verify(tenantRepository).findById(999L);
                verify(tenantRepository, never()).save(any(Tenant.class));
        }

        // ========================================
        // READ OPERATIONS TESTS
        // ========================================

        @Test
        @DisplayName("Should get tenant list by ID successfully")
        void testGetTenantListById_Success() {
                // Given
                when(tenantRepository.findById(1L)).thenReturn(Optional.of(testTenant));
                when(provisioningJobRepository.findFirstByTenantIdOrderByCreatedAtDesc(1L))
                                .thenReturn(Optional.empty());
                when(tenantModuleRepository.countEnabledModulesByTenantId(1L))
                                .thenReturn(2);

                // When
                TenantListResponse response = tenantService.getTenantListById(1L, Language.TR);

                // Then
                assertThat(response).isNotNull();
                assertThat(response.id()).isEqualTo(1L);
                assertThat(response.subdomain()).isEqualTo("testcompany");
                assertThat(response.companyName()).isEqualTo("Test Company");
                assertThat(response.provisioningStatus()).isEqualTo("idle");
                assertThat(response.provisionedModulesCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should get all tenants as list successfully")
        void testGetAllTenantsAsList_Success() {
                // Given
                Tenant tenant2 = new Tenant();
                tenant2.setId(2L);
                tenant2.setSubdomain("company2");
                tenant2.setCompanyName("Company 2");
                tenant2.setStatus(TenantStatus.PENDING);
                tenant2.setDefaultLanguage(Language.EN);
                tenant2.setSupportedLanguages(new HashSet<>(Set.of(Language.EN)));
                tenant2.setCreatedAt(LocalDateTime.now());

                when(tenantRepository.findAll()).thenReturn(Arrays.asList(testTenant, tenant2));
                when(provisioningJobRepository.findFirstByTenantIdOrderByCreatedAtDesc(anyLong()))
                                .thenReturn(Optional.empty());
                when(tenantModuleRepository.countEnabledModulesByTenantId(anyLong()))
                                .thenReturn(0);

                // When
                List<TenantListResponse> responses = tenantService.getAllTenantsAsList(Language.TR);

                // Then
                assertThat(responses).hasSize(2);
                assertThat(responses.get(0).id()).isEqualTo(1L);
                assertThat(responses.get(1).id()).isEqualTo(2L);

                verify(tenantRepository).findAll();
                verify(provisioningJobRepository, times(2)).findFirstByTenantIdOrderByCreatedAtDesc(anyLong());
                verify(tenantModuleRepository, times(2)).countEnabledModulesByTenantId(anyLong());
        }

        @Test
        @DisplayName("Should get tenants by status as list successfully")
        void testGetTenantsByStatusAsList_Success() {
                // Given
                when(tenantRepository.findByStatus(TenantStatus.ACTIVE))
                                .thenReturn(List.of(testTenant));
                when(provisioningJobRepository.findFirstByTenantIdOrderByCreatedAtDesc(1L))
                                .thenReturn(Optional.empty());
                when(tenantModuleRepository.countEnabledModulesByTenantId(1L))
                                .thenReturn(3);

                // When
                List<TenantListResponse> responses = tenantService.getTenantsByStatusAsList(
                                TenantStatus.ACTIVE, Language.TR);

                // Then
                assertThat(responses).hasSize(1);
                assertThat(responses.get(0).status()).isEqualTo(TenantStatus.ACTIVE);
                assertThat(responses.get(0).provisionedModulesCount()).isEqualTo(3);

                verify(tenantRepository).findByStatus(TenantStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should get tenant detail by ID successfully")
        void testGetTenantDetailById_Success() {
                // Given
                when(tenantRepository.findById(1L)).thenReturn(Optional.of(testTenant));
                when(provisioningJobRepository.findFirstByTenantIdOrderByCreatedAtDesc(1L))
                                .thenReturn(Optional.empty());
                when(tenantModuleRepository.countEnabledModulesByTenantId(1L))
                                .thenReturn(5);

                // When
                TenantDetailResponse response = tenantService.getTenantDetailById(1L, Language.TR);

                // Then
                assertThat(response).isNotNull();
                assertThat(response.id()).isEqualTo(1L);
                assertThat(response.databaseName()).isEqualTo("ac_tenant_1");
                assertThat(response.storageUsedMb()).isEqualTo(100L);
                assertThat(response.provisioningStatus()).isEqualTo("idle");
                assertThat(response.provisionedModulesCount()).isEqualTo(5);
        }

        // ========================================
        // PROVISIONING STATUS CALCULATION TESTS
        // ========================================

        @Test
        @DisplayName("Provisioning status should be 'idle' when no job exists")
        void testProvisioningStatus_NoJob_ReturnsIdle() {
                // Given
                when(tenantRepository.findById(1L)).thenReturn(Optional.of(testTenant));
                when(provisioningJobRepository.findFirstByTenantIdOrderByCreatedAtDesc(1L))
                                .thenReturn(Optional.empty());
                when(tenantModuleRepository.countEnabledModulesByTenantId(1L))
                                .thenReturn(0);

                // When
                TenantListResponse response = tenantService.getTenantListById(1L, Language.TR);

                // Then
                assertThat(response.provisioningStatus()).isEqualTo("idle");
        }

        @Test
        @DisplayName("Provisioning status should be 'provisioning' when job is pending")
        void testProvisioningStatus_PendingJob_ReturnsProvisioning() {
                // Given
                ProvisioningJob job = ProvisioningJob.builder()
                                .id(1L)
                                .tenantId(1L)
                                .status("pending")
                                .progress(0)
                                .build();

                when(tenantRepository.findById(1L)).thenReturn(Optional.of(testTenant));
                when(provisioningJobRepository.findFirstByTenantIdOrderByCreatedAtDesc(1L))
                                .thenReturn(Optional.of(job));
                when(tenantModuleRepository.countEnabledModulesByTenantId(1L))
                                .thenReturn(0);

                // When
                TenantListResponse response = tenantService.getTenantListById(1L, Language.TR);

                // Then
                assertThat(response.provisioningStatus()).isEqualTo("provisioning");
        }

        @Test
        @DisplayName("Provisioning status should be 'provisioning' when job is running")
        void testProvisioningStatus_RunningJob_ReturnsProvisioning() {
                // Given
                ProvisioningJob job = ProvisioningJob.builder()
                                .id(1L)
                                .tenantId(1L)
                                .status("running")
                                .progress(50)
                                .build();

                when(tenantRepository.findById(1L)).thenReturn(Optional.of(testTenant));
                when(provisioningJobRepository.findFirstByTenantIdOrderByCreatedAtDesc(1L))
                                .thenReturn(Optional.of(job));
                when(tenantModuleRepository.countEnabledModulesByTenantId(1L))
                                .thenReturn(2);

                // When
                TenantDetailResponse response = tenantService.getTenantDetailById(1L, Language.TR);

                // Then
                assertThat(response.provisioningStatus()).isEqualTo("provisioning");
        }

        @Test
        @DisplayName("Provisioning status should be 'failed' when job failed")
        void testProvisioningStatus_FailedJob_ReturnsFailed() {
                // Given
                ProvisioningJob job = ProvisioningJob.builder()
                                .id(1L)
                                .tenantId(1L)
                                .status("failed")
                                .error("Database creation failed")
                                .build();

                when(tenantRepository.findById(1L)).thenReturn(Optional.of(testTenant));
                when(provisioningJobRepository.findFirstByTenantIdOrderByCreatedAtDesc(1L))
                                .thenReturn(Optional.of(job));
                when(tenantModuleRepository.countEnabledModulesByTenantId(1L))
                                .thenReturn(0);

                // When
                TenantDetailResponse response = tenantService.getTenantDetailById(1L, Language.TR);

                // Then
                assertThat(response.provisioningStatus()).isEqualTo("failed");
        }

        @Test
        @DisplayName("Provisioning status should be 'idle' when job succeeded")
        void testProvisioningStatus_SucceededJob_ReturnsIdle() {
                // Given
                ProvisioningJob job = ProvisioningJob.builder()
                                .id(1L)
                                .tenantId(1L)
                                .status("succeeded")
                                .progress(100)
                                .completedAt(LocalDateTime.now())
                                .build();

                when(tenantRepository.findById(1L)).thenReturn(Optional.of(testTenant));
                when(provisioningJobRepository.findFirstByTenantIdOrderByCreatedAtDesc(1L))
                                .thenReturn(Optional.of(job));
                when(tenantModuleRepository.countEnabledModulesByTenantId(1L))
                                .thenReturn(3);

                // When
                TenantDetailResponse response = tenantService.getTenantDetailById(1L, Language.TR);

                // Then
                assertThat(response.provisioningStatus()).isEqualTo("idle");
        }

        @Test
        @DisplayName("Provisioning status should default to 'idle' for unknown status")
        void testProvisioningStatus_UnknownStatus_ReturnsIdle() {
                // Given
                ProvisioningJob job = ProvisioningJob.builder()
                                .id(1L)
                                .tenantId(1L)
                                .status("unknown_status")
                                .build();

                when(tenantRepository.findById(1L)).thenReturn(Optional.of(testTenant));
                when(provisioningJobRepository.findFirstByTenantIdOrderByCreatedAtDesc(1L))
                                .thenReturn(Optional.of(job));
                when(tenantModuleRepository.countEnabledModulesByTenantId(1L))
                                .thenReturn(0);

                // When
                TenantListResponse response = tenantService.getTenantListById(1L, Language.TR);

                // Then
                assertThat(response.provisioningStatus()).isEqualTo("idle");
        }

        // ========================================
        // MODULE COUNT CALCULATION TESTS
        // ========================================

        @Test
        @DisplayName("Module count should be 0 when no modules exist")
        void testModuleCount_NoModules_ReturnsZero() {
                // Given
                when(tenantRepository.findById(1L)).thenReturn(Optional.of(testTenant));
                when(provisioningJobRepository.findFirstByTenantIdOrderByCreatedAtDesc(1L))
                                .thenReturn(Optional.empty());
                when(tenantModuleRepository.countEnabledModulesByTenantId(1L))
                                .thenReturn(0);

                // When
                TenantListResponse response = tenantService.getTenantListById(1L, Language.TR);

                // Then
                assertThat(response.provisionedModulesCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("Module count should be 3 when three enabled modules exist")
        void testModuleCount_ThreeEnabledModules_ReturnsThree() {
                // Given
                when(tenantRepository.findById(1L)).thenReturn(Optional.of(testTenant));
                when(provisioningJobRepository.findFirstByTenantIdOrderByCreatedAtDesc(1L))
                                .thenReturn(Optional.empty());
                when(tenantModuleRepository.countEnabledModulesByTenantId(1L))
                                .thenReturn(3);

                // When
                TenantDetailResponse response = tenantService.getTenantDetailById(1L, Language.TR);

                // Then
                assertThat(response.provisionedModulesCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("Module count should only count enabled modules")
        void testModuleCount_MixedStatusModules_CountsOnlyEnabled() {
                // Given - Assume repo returns count of only 'enabled' status
                when(tenantRepository.findById(1L)).thenReturn(Optional.of(testTenant));
                when(provisioningJobRepository.findFirstByTenantIdOrderByCreatedAtDesc(1L))
                                .thenReturn(Optional.empty());
                when(tenantModuleRepository.countEnabledModulesByTenantId(1L))
                                .thenReturn(2); // Only enabled modules

                // When
                TenantListResponse response = tenantService.getTenantListById(1L, Language.TR);

                // Then
                assertThat(response.provisionedModulesCount()).isEqualTo(2);
                verify(tenantModuleRepository).countEnabledModulesByTenantId(1L);
        }

        @Test
        @DisplayName("Module count should be 0 when repository returns null")
        void testModuleCount_NullResult_ReturnsZero() {
                // Given
                when(tenantRepository.findById(1L)).thenReturn(Optional.of(testTenant));
                when(provisioningJobRepository.findFirstByTenantIdOrderByCreatedAtDesc(1L))
                                .thenReturn(Optional.empty());
                when(tenantModuleRepository.countEnabledModulesByTenantId(1L))
                                .thenReturn(null);

                // When
                TenantDetailResponse response = tenantService.getTenantDetailById(1L, Language.TR);

                // Then
                assertThat(response.provisionedModulesCount()).isEqualTo(0);
        }

        // ========================================
        // DTO MAPPING VALIDATION TEST
        // ========================================

        @Test
        @DisplayName("DTO mapping should populate all required fields correctly")
        void testDtoMapping_AllFieldsPopulated() {
                // Given
                when(tenantRepository.findById(1L)).thenReturn(Optional.of(testTenant));
                when(provisioningJobRepository.findFirstByTenantIdOrderByCreatedAtDesc(1L))
                                .thenReturn(Optional.empty());
                when(tenantModuleRepository.countEnabledModulesByTenantId(1L))
                                .thenReturn(3);

                // When
                TenantDetailResponse response = tenantService.getTenantDetailById(1L, Language.TR);

                // Then - Verify all key fields
                assertThat(response.id()).isEqualTo(1L);
                assertThat(response.subdomain()).isEqualTo("testcompany");
                assertThat(response.companyName()).isEqualTo("Test Company");
                assertThat(response.databaseName()).isEqualTo("ac_tenant_1");
                assertThat(response.status()).isEqualTo(TenantStatus.ACTIVE);
                assertThat(response.defaultLanguage()).isEqualTo(Language.TR);
                assertThat(response.supportedLanguages()).hasSize(2);
                assertThat(response.provisioningStatus()).isEqualTo("idle");
                assertThat(response.provisionedModulesCount()).isEqualTo(3);
                assertThat(response.customDomain()).isEqualTo("testcompany.com");
                assertThat(response.storageUsedMb()).isEqualTo(100L);
                assertThat(response.createdAt()).isNotNull();
                assertThat(response.updatedAt()).isNotNull();
        }
}
