package com.backend.presentation.dto.response;

import com.backend.domain.entity.Tenant;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.ProvisioningStatus;
import com.backend.domain.enums.TenantStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for Tenant Response DTOs - Sprint 18
 * Tests factory methods and field mapping
 */
@DisplayName("Tenant DTO Mapping Tests - Sprint 18")
class TenantDTOTest {

    private Tenant testTenant;
    private final ProvisioningStatus testProvisioningStatus = ProvisioningStatus.IDLE;
    private final Integer testModulesCount = 3;

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
        testTenant.setActivatedAt(LocalDateTime.now());
        testTenant.setLastBackupAt(LocalDateTime.now());
        testTenant.setNotes("Test notes");
    }

    // ========================================
    // TenantListResponse Tests
    // ========================================

    @Test
    @DisplayName("TenantListResponse.from() - Should map all fields correctly")
    void testTenantListResponse_From_AllFieldsMapped() {
        // When
        TenantListResponse response = TenantListResponse.from(
                testTenant,
                Language.TR,
                testProvisioningStatus,
                testModulesCount);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.subdomain()).isEqualTo("testcompany");
        assertThat(response.companyName()).isEqualTo("Test Company");
        assertThat(response.status()).isEqualTo(TenantStatus.ACTIVE);
        assertThat(response.defaultLanguage()).isEqualTo(Language.TR);
        assertThat(response.supportedLanguages()).hasSize(2);
        assertThat(response.customDomain()).isEqualTo("testcompany.com");
        assertThat(response.createdAt()).isNotNull();
        assertThat(response.activatedAt()).isNotNull();
    }

    @Test
    @DisplayName("TenantListResponse.from() - Should include provisioning status")
    void testTenantListResponse_ProvisioningStatusIncluded() {
        // When
        TenantListResponse response = TenantListResponse.from(
                testTenant,
                Language.TR,
                ProvisioningStatus.PROVISIONING,
                testModulesCount);

        // Then
        assertThat(response.provisioningStatus()).isEqualTo("provisioning");
    }

    @Test
    @DisplayName("TenantListResponse.from() - Should include provisioned modules count")
    void testTenantListResponse_ModulesCountIncluded() {
        // When
        TenantListResponse response = TenantListResponse.from(
                testTenant,
                Language.TR,
                testProvisioningStatus,
                5);

        // Then
        assertThat(response.provisionedModulesCount()).isEqualTo(5);
    }

    @Test
    @DisplayName("TenantListResponse.from() - Should NOT include admin PII")
    void testTenantListResponse_NoAdminPII() {
        TenantListResponse response = TenantListResponse.from(
                testTenant,
                Language.TR,
                testProvisioningStatus,
                testModulesCount);

        assertThat(response.getClass().getRecordComponents())
                .extracting("name")
                .doesNotContain("adminEmail", "adminName", "phone");
    }

    // ========================================
    // TenantDetailResponse Tests
    // ========================================

    @Test
    @DisplayName("TenantDetailResponse.from() - Should map all fields correctly")
    void testTenantDetailResponse_From_AllFieldsMapped() {
        // When
        TenantDetailResponse response = TenantDetailResponse.from(
                testTenant,
                Language.EN,
                testProvisioningStatus,
                testModulesCount);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.subdomain()).isEqualTo("testcompany");
        assertThat(response.companyName()).isEqualTo("Test Company");
        assertThat(response.databaseName()).isEqualTo("ac_tenant_1");
        assertThat(response.status()).isEqualTo(TenantStatus.ACTIVE);
        assertThat(response.defaultLanguage()).isEqualTo(Language.TR);
        assertThat(response.supportedLanguages()).hasSize(2);
        assertThat(response.provisioningStatus()).isEqualTo("idle");
        assertThat(response.provisionedModulesCount()).isEqualTo(3);
        assertThat(response.createdAt()).isNotNull();
        assertThat(response.activatedAt()).isNotNull();
        assertThat(response.customDomain()).isEqualTo("testcompany.com");
    }

    @Test
    @DisplayName("TenantDetailResponse.from() - Should include technical fields")
    void testTenantDetailResponse_TechnicalFieldsIncluded() {
        // When
        TenantDetailResponse response = TenantDetailResponse.from(
                testTenant,
                Language.TR,
                testProvisioningStatus,
                testModulesCount);

        assertThat(response.databaseName()).isEqualTo("ac_tenant_1");
        assertThat(response.storageUsedMb()).isEqualTo(100L);
        assertThat(response.fullDomain()).isNotNull();
        assertThat(response.updatedAt()).isNotNull();
        assertThat(response.lastBackupAt()).isNotNull();
        assertThat(response.notes()).isEqualTo("Test notes");
    }

    @Test
    @DisplayName("TenantDetailResponse.from() - Should NOT include admin PII")
    void testTenantDetailResponse_NoAdminPII() {
        TenantDetailResponse response = TenantDetailResponse.from(
                testTenant,
                Language.TR,
                testProvisioningStatus,
                testModulesCount);

        assertThat(response.getClass().getRecordComponents())
                .extracting("name")
                .doesNotContain("adminEmail", "adminName", "phone");
    }
}
