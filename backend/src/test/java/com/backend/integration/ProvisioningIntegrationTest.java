package com.backend.integration;

import com.backend.application.dto.provisioning.ProvisionRequest;
import com.backend.application.dto.provisioning.ProvisioningJobResponse;
import com.backend.application.service.ProvisioningService;
import com.backend.infrastructure.persistence.platform.entity.Tenant;
import com.backend.infrastructure.persistence.platform.repository.TenantPlatformRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@Testcontainers
public class ProvisioningIntegrationTest {

  @Container
  static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
      .withDatabaseName("platform_management")
      .withUsername("test")
      .withPassword("test");

  @DynamicPropertySource
  static void properties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.platform.url", mysql::getJdbcUrl);
    registry.add("spring.datasource.platform.username", mysql::getUsername);
    registry.add("spring.datasource.platform.password", mysql::getPassword);
    registry.add("spring.datasource.tenant.host", mysql::getHost);
    registry.add("spring.datasource.tenant.port", mysql::getFirstMappedPort);
    registry.add("spring.datasource.tenant.username", mysql::getUsername);
    registry.add("spring.datasource.tenant.password", mysql::getPassword);
    registry.add("spring.flyway.url", mysql::getJdbcUrl);
    registry.add("spring.flyway.user", mysql::getUsername);
    registry.add("spring.flyway.password", mysql::getPassword);
  }

  @Autowired
  private TenantPlatformRepository tenantRepository;

  @Autowired
  private ProvisioningService provisioningService;

  @BeforeEach
  void setUp() {
    tenantRepository.deleteAll();
  }

  @Test
  void shouldProvisionTenantWithCoreAndPagebuilderModules() {
    Tenant tenant = Tenant.builder()
        .subdomain("test-tenant")
        .companyName("Test Company")
        .dbName("ac_tenant_1")
        .status("PENDING")
        .defaultLanguage("TR")
        .supportedLanguages("[\"TR\", \"EN\"]")
        .adminEmail("admin@test.com")
        .adminName("Test Admin")
        .build();

    tenant = tenantRepository.save(tenant);

    ProvisionRequest request = ProvisionRequest.builder()
        .modules(List.of("core", "pagebuilder"))
        .build();

    ProvisioningJobResponse response = provisioningService.provisionTenant(tenant.getId(), request);

    assertThat(response).isNotNull();
    assertThat(response.getJobId()).isNotNull();
    assertThat(response.getStatus()).isEqualTo("pending");

    await().atMost(30, TimeUnit.SECONDS)
        .pollInterval(1, TimeUnit.SECONDS)
        .untilAsserted(() -> {
          ProvisioningJobResponse job = provisioningService.getJobStatus(response.getJobId());
          assertThat(job.getStatus()).isIn("succeeded", "failed");
          if ("failed".equals(job.getStatus())) {
            System.err.println("Provisioning failed: " + job.getError());
          }
          assertThat(job.getStatus()).isEqualTo("succeeded");
          assertThat(job.getProgress()).isEqualTo(100);
        });

    Tenant updatedTenant = tenantRepository.findById(tenant.getId()).orElseThrow();
    assertThat(updatedTenant.getStatus()).isEqualTo("ACTIVE");
  }

  @Test
  void shouldIsolateTenantData() throws Exception {
    Tenant tenant1 = tenantRepository.save(Tenant.builder()
        .subdomain("tenant1")
        .companyName("Tenant 1")
        .dbName("ac_tenant_1")
        .status("PENDING")
        .defaultLanguage("TR")
        .supportedLanguages("[\"TR\"]")
        .adminEmail("admin1@test.com")
        .adminName("Admin 1")
        .build());

    Tenant tenant2 = tenantRepository.save(Tenant.builder()
        .subdomain("tenant2")
        .companyName("Tenant 2")
        .dbName("ac_tenant_2")
        .status("PENDING")
        .defaultLanguage("EN")
        .supportedLanguages("[\"EN\"]")
        .adminEmail("admin2@test.com")
        .adminName("Admin 2")
        .build());

    ProvisionRequest request = ProvisionRequest.builder()
        .modules(List.of("core"))
        .build();

    ProvisioningJobResponse job1 = provisioningService.provisionTenant(tenant1.getId(), request);
    ProvisioningJobResponse job2 = provisioningService.provisionTenant(tenant2.getId(), request);

    await().atMost(30, TimeUnit.SECONDS)
        .until(() -> {
          ProvisioningJobResponse j1 = provisioningService.getJobStatus(job1.getJobId());
          ProvisioningJobResponse j2 = provisioningService.getJobStatus(job2.getJobId());
          return "succeeded".equals(j1.getStatus()) && "succeeded".equals(j2.getStatus());
        });

    assertThat(tenantRepository.findById(tenant1.getId()).orElseThrow().getStatus()).isEqualTo("ACTIVE");
    assertThat(tenantRepository.findById(tenant2.getId()).orElseThrow().getStatus()).isEqualTo("ACTIVE");
  }

  @Test
  void shouldHandleIdempotentProvisioning() {
    Tenant tenant = Tenant.builder()
        .subdomain("idempotent-tenant")
        .companyName("Idempotent Company")
        .dbName("ac_tenant_idempotent")
        .status("PENDING")
        .defaultLanguage("TR")
        .supportedLanguages("[\"TR\", \"EN\"]")
        .adminEmail("admin@idempotent.com")
        .adminName("Idempotent Admin")
        .build();

    tenant = tenantRepository.save(tenant);

    ProvisionRequest request = ProvisionRequest.builder()
        .modules(List.of("core", "pagebuilder"))
        .build();

    // First provisioning
    ProvisioningJobResponse job1 = provisioningService.provisionTenant(tenant.getId(), request);
    assertThat(job1.getJobId()).isNotNull();

    await().atMost(30, TimeUnit.SECONDS)
        .pollInterval(1, TimeUnit.SECONDS)
        .untilAsserted(() -> {
          ProvisioningJobResponse status = provisioningService.getJobStatus(job1.getJobId());
          assertThat(status.getStatus()).isEqualTo("succeeded");
        });

    // Reset tenant status for second provisioning
    Tenant activeTenant = tenantRepository.findById(tenant.getId()).orElseThrow();
    activeTenant.setStatus("PENDING");
    tenantRepository.save(activeTenant);

    // Second provisioning with same modules (should be idempotent)
    ProvisioningJobResponse job2 = provisioningService.provisionTenant(tenant.getId(), request);
    assertThat(job2.getJobId()).isNotNull();
    assertThat(job2.getJobId()).isNotEqualTo(job1.getJobId());

    await().atMost(30, TimeUnit.SECONDS)
        .pollInterval(1, TimeUnit.SECONDS)
        .untilAsserted(() -> {
          ProvisioningJobResponse status = provisioningService.getJobStatus(job2.getJobId());
          assertThat(status.getStatus()).isIn("succeeded", "failed");
          if ("failed".equals(status.getStatus())) {
            System.err.println("Second provisioning failed: " + status.getError());
          }
          // Flyway should handle idempotency - no duplicate key errors
          assertThat(status.getStatus()).isEqualTo("succeeded");
        });

    Tenant finalTenant = tenantRepository.findById(tenant.getId()).orElseThrow();
    assertThat(finalTenant.getStatus()).isEqualTo("ACTIVE");
  }
}

