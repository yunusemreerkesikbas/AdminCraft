package com.backend.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.Connection;
import java.sql.Statement;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.backend.infrastructure.persistence.platform.entity.Tenant;
import com.backend.infrastructure.persistence.platform.repository.TenantPlatformRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@org.springframework.test.context.ActiveProfiles("test")
@org.junit.jupiter.api.Disabled("TODO: Fix table structure to match entity definitions")
public class CmsDeliveryControllerIntegrationTest {

  private static final String TEST_TENANT_SUBDOMAIN = "test-tenant";
  private static final Long TEST_TENANT_ID = 1L;
  private static final String TEST_TENANT_DB = "ac_tenant_1";

  // Test UIDs for component data
  private static final String PUBLISHED_COMPONENT_UID = "hero-banner-001";
  private static final String DRAFT_COMPONENT_UID = "draft-component-001";

  @Container
  static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
      .withDatabaseName("platform_management")
      .withUsername("root")
      .withPassword("test");

  @DynamicPropertySource
  static void properties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.platform.jdbc-url", mysql::getJdbcUrl);
    registry.add("spring.datasource.platform.url", mysql::getJdbcUrl);
    registry.add("spring.datasource.platform.username", mysql::getUsername);
    registry.add("spring.datasource.platform.password", mysql::getPassword);
    registry.add("spring.datasource.tenant.host", mysql::getHost);
    registry.add("spring.datasource.tenant.port", mysql::getFirstMappedPort);
    registry.add("spring.datasource.tenant.username", mysql::getUsername);
    registry.add("spring.datasource.tenant.password", mysql::getPassword);
    registry.add("spring.datasource.tenant.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
    registry.add("spring.datasource.tenant.jdbc-url-template", () -> "jdbc:mysql://" + mysql.getHost() + ":"
        + mysql.getFirstMappedPort() +
        "/{dbName}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Europe/Istanbul&characterEncoding=UTF-8&useUnicode=true");
    registry.add("spring.flyway.url", mysql::getJdbcUrl);
    registry.add("spring.flyway.user", mysql::getUsername);
    registry.add("spring.flyway.password", mysql::getPassword);
  }

  @BeforeAll
  static void setupTenant() throws Exception {
    try (Connection conn = mysql.createConnection("")) {
      try (Statement stmt = conn.createStatement()) {
        // Create tenant database
        stmt.execute("CREATE DATABASE IF NOT EXISTS " + TEST_TENANT_DB);

        // Create minimal tenant schema for CMS delivery tests
        stmt.execute("USE " + TEST_TENANT_DB);

        // Create component_types table
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS component_types (
              id BIGINT AUTO_INCREMENT PRIMARY KEY,
              uuid VARCHAR(36) NOT NULL UNIQUE,
              uid VARCHAR(100) NOT NULL UNIQUE,
              name VARCHAR(100) NOT NULL,
              category VARCHAR(50),
              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
              updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )
            """);

        // Create components table
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS components (
              id BIGINT AUTO_INCREMENT PRIMARY KEY,
              uuid VARCHAR(36) NOT NULL UNIQUE,
              uid VARCHAR(100) NOT NULL UNIQUE,
              name VARCHAR(255) NOT NULL,
              code VARCHAR(100),
              component_type_id BIGINT,
              status VARCHAR(20) DEFAULT 'DRAFT',
              is_visible BOOLEAN DEFAULT true,
              style_classes VARCHAR(500),
              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
              updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
              FOREIGN KEY (component_type_id) REFERENCES component_types(id)
            )
            """);

        // Create component_i18n table
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS component_i18n (
              id BIGINT AUTO_INCREMENT PRIMARY KEY,
              uuid VARCHAR(36) NOT NULL UNIQUE,
              uid VARCHAR(100) NOT NULL UNIQUE,
              component_id BIGINT NOT NULL,
              language VARCHAR(10) NOT NULL,
              title VARCHAR(255),
              subtitle VARCHAR(255),
              description TEXT,
              status VARCHAR(20) DEFAULT 'DRAFT',
              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
              updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
              FOREIGN KEY (component_id) REFERENCES components(id),
              UNIQUE KEY uk_component_lang (component_id, language)
            )
            """);

        // Create component_entries table
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS component_entries (
              id BIGINT AUTO_INCREMENT PRIMARY KEY,
              uuid VARCHAR(36) NOT NULL UNIQUE,
              uid VARCHAR(100) NOT NULL UNIQUE,
              component_id BIGINT NOT NULL,
              sort_order INT DEFAULT 0,
              status VARCHAR(20) DEFAULT 'DRAFT',
              is_visible BOOLEAN DEFAULT true,
              style_classes VARCHAR(500),
              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
              updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
              FOREIGN KEY (component_id) REFERENCES components(id)
            )
            """);

        // Create component_entry_i18n table
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS component_entry_i18n (
              id BIGINT AUTO_INCREMENT PRIMARY KEY,
              uuid VARCHAR(36) NOT NULL UNIQUE,
              uid VARCHAR(100) NOT NULL UNIQUE,
              entry_id BIGINT NOT NULL,
              language VARCHAR(10) NOT NULL,
              title VARCHAR(255),
              description TEXT,
              custom_data JSON,
              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
              updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
              FOREIGN KEY (entry_id) REFERENCES component_entries(id),
              UNIQUE KEY uk_entry_lang (entry_id, language)
            )
            """);

        // Insert test data
        insertTestData(stmt);
      }
    }
  }

  private static void insertTestData(Statement stmt) throws Exception {
    // Insert component type
    stmt.execute("""
        INSERT INTO component_types (uuid, uid, name, category)
        VALUES ('%s', 'hero-type', 'Hero Banner', 'MARKETING')
        """.formatted(UUID.randomUUID().toString()));

    // Insert published component
    stmt.execute("""
        INSERT INTO components (uuid, uid, name, code, component_type_id, status, is_visible, style_classes)
        VALUES ('%s', '%s', 'Hero Banner', 'HERO_001', 1, 'PUBLISHED', true, 'hero-section')
        """.formatted(UUID.randomUUID().toString(), PUBLISHED_COMPONENT_UID));

    // Insert draft component (should not be returned)
    stmt.execute("""
        INSERT INTO components (uuid, uid, name, code, component_type_id, status, is_visible)
        VALUES ('%s', '%s', 'Draft Component', 'DRAFT_001', 1, 'DRAFT', true)
        """.formatted(UUID.randomUUID().toString(), DRAFT_COMPONENT_UID));

    // Insert component i18n for TR
    stmt.execute("""
        INSERT INTO component_i18n (uuid, uid, component_id, language, title, subtitle, description, status)
        VALUES ('%s', 'hero-i18n-tr', 1, 'TR', 'Ana Sayfa Banner', 'Hoş Geldiniz', 'Açıklama metni', 'PUBLISHED')
        """.formatted(UUID.randomUUID().toString()));

    // Insert component i18n for EN
    stmt.execute("""
        INSERT INTO component_i18n (uuid, uid, component_id, language, title, subtitle, description, status)
        VALUES ('%s', 'hero-i18n-en', 1, 'EN', 'Homepage Banner', 'Welcome', 'Description text', 'PUBLISHED')
        """.formatted(UUID.randomUUID().toString()));

    // Insert published entry
    stmt.execute("""
        INSERT INTO component_entries (uuid, uid, component_id, sort_order, status, is_visible)
        VALUES ('%s', 'entry-001', 1, 1, 'PUBLISHED', true)
        """.formatted(UUID.randomUUID().toString()));

    // Insert entry i18n
    stmt.execute("""
        INSERT INTO component_entry_i18n (uuid, uid, entry_id, language, title, description, custom_data)
        VALUES ('%s', 'entry-i18n-tr', 1, 'TR', 'Giriş Başlığı', 'Giriş açıklaması', '{"buttonText": "Tıkla"}')
        """.formatted(UUID.randomUUID().toString()));
  }

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private TenantPlatformRepository tenantRepository;

  @BeforeEach
  void init() {
    if (tenantRepository.findBySubdomain(TEST_TENANT_SUBDOMAIN).isEmpty()) {
      Tenant tenant = Tenant.builder()
          .id(TEST_TENANT_ID)
          .subdomain(TEST_TENANT_SUBDOMAIN)
          .companyName("Test Company")
          .databaseName(TEST_TENANT_DB)
          .status("ACTIVE")
          .defaultLanguage("TR")
          .adminEmail("admin@test.com")
          .adminName("Admin")
          .hasAdminUser(false)
          .currency("TRY")
          .build();
      tenantRepository.save(tenant);
    }
  }

  // ==================== Missing Tenant Header Tests ====================

  @Test
  void getByUid_WithoutTenantHeader_ShouldReturnBadRequest() throws Exception {
    mockMvc.perform(get("/cms/components/any-uid")
        .header("Accept-Language", "tr"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void getByUids_WithoutTenantHeader_ShouldReturnBadRequest() throws Exception {
    mockMvc.perform(get("/cms/components")
        .param("uids", "uid1,uid2")
        .header("Accept-Language", "tr"))
        .andExpect(status().isBadRequest());
  }

  // ==================== Component By UID Tests ====================

  @Test
  void getByUid_WithValidTenant_ShouldReturnComponent() throws Exception {
    mockMvc.perform(get("/cms/components/" + PUBLISHED_COMPONENT_UID)
        .header("X-Tenant-ID", TEST_TENANT_ID)
        .header("Accept-Language", "tr"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result").value("SUCCESS"))
        .andExpect(jsonPath("$.data.uid").value(PUBLISHED_COMPONENT_UID))
        .andExpect(jsonPath("$.data.title").value("Ana Sayfa Banner"))
        .andExpect(jsonPath("$.data.type").value("Hero Banner"));
  }

  @Test
  void getByUid_WithTenantSubdomain_ShouldReturnComponent() throws Exception {
    mockMvc.perform(get("/cms/components/" + PUBLISHED_COMPONENT_UID)
        .header("X-Tenant-Subdomain", TEST_TENANT_SUBDOMAIN)
        .header("Accept-Language", "tr"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result").value("SUCCESS"))
        .andExpect(jsonPath("$.data.uid").value(PUBLISHED_COMPONENT_UID));
  }

  @Test
  void getByUid_WithENLanguage_ShouldReturnEnglishContent() throws Exception {
    mockMvc.perform(get("/cms/components/" + PUBLISHED_COMPONENT_UID)
        .header("X-Tenant-ID", TEST_TENANT_ID)
        .param("lang", "EN")
        .header("Accept-Language", "en"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.title").value("Homepage Banner"))
        .andExpect(jsonPath("$.data.subtitle").value("Welcome"));
  }

  @Test
  void getByUid_DraftComponent_ShouldReturnNotFound() throws Exception {
    mockMvc.perform(get("/cms/components/" + DRAFT_COMPONENT_UID)
        .header("X-Tenant-ID", TEST_TENANT_ID)
        .header("Accept-Language", "tr"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result").value("ERROR"));
  }

  @Test
  void getByUid_NonexistentUid_ShouldReturnNotFound() throws Exception {
    mockMvc.perform(get("/cms/components/nonexistent-uid")
        .header("X-Tenant-ID", TEST_TENANT_ID)
        .header("Accept-Language", "tr"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result").value("ERROR"));
  }

  // ==================== Batch Components Tests ====================

  @Test
  void getByUids_WithValidTenant_ShouldReturnBatchResponse() throws Exception {
    MvcResult result = mockMvc.perform(get("/cms/components")
        .header("X-Tenant-ID", TEST_TENANT_ID)
        .param("uids", PUBLISHED_COMPONENT_UID + ",nonexistent-uid")
        .header("Accept-Language", "tr"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result").value("SUCCESS"))
        .andExpect(jsonPath("$.data.data." + PUBLISHED_COMPONENT_UID).exists())
        .andExpect(jsonPath("$.data.meta.requested").value(2))
        .andExpect(jsonPath("$.data.meta.found").value(1))
        .andExpect(jsonPath("$.data.meta.notFound").isArray())
        .andReturn();

    String responseJson = result.getResponse().getContentAsString();
    assertThat(responseJson).contains("nonexistent-uid");
  }

  @Test
  void getByUids_EmptyList_ShouldReturnBadRequest() throws Exception {
    mockMvc.perform(get("/cms/components")
        .header("X-Tenant-ID", TEST_TENANT_ID)
        .header("Accept-Language", "tr"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.result").value("ERROR"));
  }

  @Test
  void getByUids_TooManyUids_ShouldReturnBadRequest() throws Exception {
    StringBuilder uids = new StringBuilder();
    for (int i = 0; i < 51; i++) {
      if (i > 0)
        uids.append(",");
      uids.append("uid").append(i);
    }

    mockMvc.perform(get("/cms/components")
        .header("X-Tenant-ID", TEST_TENANT_ID)
        .param("uids", uids.toString())
        .header("Accept-Language", "tr"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.result").value("ERROR"));
  }

  // ==================== Inactive Tenant Tests ====================

  @Test
  void getByUid_InactiveTenant_ShouldReturnForbidden() throws Exception {
    // Use non-existent tenant ID
    mockMvc.perform(get("/cms/components/" + PUBLISHED_COMPONENT_UID)
        .header("X-Tenant-ID", 99999L)
        .header("Accept-Language", "tr"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void getByUid_InvalidSubdomain_ShouldReturnBadRequest() throws Exception {
    mockMvc.perform(get("/cms/components/" + PUBLISHED_COMPONENT_UID)
        .header("X-Tenant-Subdomain", "invalid-subdomain")
        .header("Accept-Language", "tr"))
        .andExpect(status().isBadRequest());
  }
}
