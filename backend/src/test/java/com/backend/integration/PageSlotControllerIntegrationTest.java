package com.backend.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.sql.Connection;
import java.sql.Statement;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Disabled("TODO: Complete integration tests for Sprint 26 Content Slot System")
public class PageSlotControllerIntegrationTest {

  private static final String TEST_TENANT_SUBDOMAIN = "test-tenant";
  private static final Long TEST_TENANT_ID = 1L;
  private static final String TEST_TENANT_DB = "ac_tenant_1";

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
    registry.add("spring.flyway.url", mysql::getJdbcUrl);
    registry.add("spring.flyway.user", mysql::getUsername);
    registry.add("spring.flyway.password", mysql::getPassword);
  }

  @BeforeAll
  static void setupTenant() throws Exception {
    try (Connection conn = mysql.createConnection("")) {
      try (Statement stmt = conn.createStatement()) {
        stmt.execute("CREATE DATABASE IF NOT EXISTS " + TEST_TENANT_DB);

        stmt.execute("""
            INSERT INTO platform_management.tenants
            (subdomain, company_name, database_name, status, default_language, has_admin_user, created_at, updated_at)
            VALUES
            ('%s', 'Test Company', '%s', 'ACTIVE', 'TR', false, NOW(), NOW())
            ON DUPLICATE KEY UPDATE status = 'ACTIVE'
            """.formatted(TEST_TENANT_SUBDOMAIN, TEST_TENANT_DB));

        stmt.execute("USE " + TEST_TENANT_DB);

        createSchemaForSlotTests(stmt);
        insertTestDataForSlotTests(stmt);
      }
    }
  }

  private static void createSchemaForSlotTests(Statement stmt) throws Exception {
    stmt.execute("""
        CREATE TABLE IF NOT EXISTS pages (
          id BIGINT AUTO_INCREMENT PRIMARY KEY,
          uuid VARCHAR(36) NOT NULL UNIQUE,
          uid VARCHAR(100) NOT NULL UNIQUE,
          status VARCHAR(20) DEFAULT 'DRAFT',
          featured_image VARCHAR(500),
          style_classes VARCHAR(500),
          sort_order INT DEFAULT 0,
          robot_tag VARCHAR(50) DEFAULT 'INDEX_FOLLOW',
          template_uid VARCHAR(50),
          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
        )
        """);

    stmt.execute("""
        CREATE TABLE IF NOT EXISTS page_slots (
          id BIGINT AUTO_INCREMENT PRIMARY KEY,
          uuid VARCHAR(36) NOT NULL UNIQUE,
          uid VARCHAR(50) NOT NULL UNIQUE,
          page_id BIGINT NULL,
          slot_name VARCHAR(50) NOT NULL,
          position VARCHAR(20) NOT NULL,
          sort_order INT DEFAULT 0,
          is_active BOOLEAN DEFAULT TRUE,
          is_shared BOOLEAN DEFAULT FALSE,
          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
          UNIQUE KEY uk_page_slot (page_id, slot_name),
          FOREIGN KEY (page_id) REFERENCES pages(id) ON DELETE CASCADE
        )
        """);

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

    stmt.execute("""
        CREATE TABLE IF NOT EXISTS slot_components (
          id BIGINT AUTO_INCREMENT PRIMARY KEY,
          slot_id BIGINT NOT NULL,
          component_id BIGINT NOT NULL,
          sort_order INT DEFAULT 0,
          is_visible BOOLEAN DEFAULT TRUE,
          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
          UNIQUE KEY uk_slot_component (slot_id, component_id),
          FOREIGN KEY (slot_id) REFERENCES page_slots(id) ON DELETE CASCADE,
          FOREIGN KEY (component_id) REFERENCES components(id) ON DELETE CASCADE
        )
        """);
  }

  private static void insertTestDataForSlotTests(Statement stmt) throws Exception {
    stmt.execute("""
        INSERT INTO pages (uuid, uid, status)
        VALUES ('%s', 'homepage', 'PUBLISHED')
        """.formatted(UUID.randomUUID().toString()));

    stmt.execute("""
        INSERT INTO component_types (uuid, uid, name, category)
        VALUES ('%s', 'hero-type', 'Hero Banner', 'MARKETING')
        """.formatted(UUID.randomUUID().toString()));

    stmt.execute("""
        INSERT INTO components (uuid, uid, name, code, component_type_id, status, is_visible)
        VALUES ('%s', 'hero-component', 'Hero Banner', 'HERO_001', 1, 'PUBLISHED', true)
        """.formatted(UUID.randomUUID().toString()));
  }

  @Autowired
  private MockMvc mockMvc;

  @Nested
  @DisplayName("Slot CRUD Operations")
  class SlotCrudTests {

    @Test
    @WithMockUser(roles = "TENANT_ADMIN")
    @DisplayName("createSlot_ShouldReturnCreatedSlot")
    void createSlot_ShouldReturnCreatedSlot() throws Exception {
      String requestBody = """
          {
            "slotName": "MainContent",
            "position": "center",
            "sortOrder": 0
          }
          """;

      mockMvc.perform(post("/pages/1/slots")
          .header("X-Tenant-ID", TEST_TENANT_ID)
          .contentType(MediaType.APPLICATION_JSON)
          .content(requestBody))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.result").value("SUCCESS"))
          .andExpect(jsonPath("$.data.slotName").value("MainContent"))
          .andExpect(jsonPath("$.data.position").value("center"));
    }

    @Test
    @WithMockUser(roles = "TENANT_ADMIN")
    @DisplayName("listSlots_ShouldReturnSlotsWithComponents")
    void listSlots_ShouldReturnSlotsWithComponents() throws Exception {
      mockMvc.perform(get("/pages/1/slots")
          .header("X-Tenant-ID", TEST_TENANT_ID))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.result").value("SUCCESS"))
          .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser(roles = "TENANT_ADMIN")
    @DisplayName("deleteSlot_ShouldRemoveSlot")
    void deleteSlot_ShouldRemoveSlot() throws Exception {
      mockMvc.perform(delete("/pages/1/slots/TestSlot")
          .header("X-Tenant-ID", TEST_TENANT_ID))
          .andExpect(status().isOk());
    }
  }

  @Nested
  @DisplayName("Slot Component Operations")
  class SlotComponentTests {

    @Test
    @WithMockUser(roles = "TENANT_ADMIN")
    @DisplayName("addComponent_ShouldAssociateComponentToSlot")
    void addComponent_ShouldAssociateComponentToSlot() throws Exception {
      String requestBody = """
          {
            "componentId": 1
          }
          """;

      mockMvc.perform(post("/pages/1/slots/MainContent/components")
          .header("X-Tenant-ID", TEST_TENANT_ID)
          .contentType(MediaType.APPLICATION_JSON)
          .content(requestBody))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.result").value("SUCCESS"));
    }

    @Test
    @WithMockUser(roles = "TENANT_ADMIN")
    @DisplayName("removeComponent_ShouldDisassociateComponent")
    void removeComponent_ShouldDisassociateComponent() throws Exception {
      mockMvc.perform(delete("/pages/1/slots/MainContent/components/1")
          .header("X-Tenant-ID", TEST_TENANT_ID))
          .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "TENANT_ADMIN")
    @DisplayName("reorderComponents_ShouldUpdateSortOrder")
    void reorderComponents_ShouldUpdateSortOrder() throws Exception {
      String requestBody = """
          {
            "items": [2, 1, 3]
          }
          """;

      mockMvc.perform(put("/pages/1/slots/MainContent/reorder")
          .header("X-Tenant-ID", TEST_TENANT_ID)
          .contentType(MediaType.APPLICATION_JSON)
          .content(requestBody))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.result").value("SUCCESS"));
    }
  }

  @Nested
  @DisplayName("Shared Slot Operations")
  class SharedSlotTests {

    @Test
    @WithMockUser(roles = "TENANT_ADMIN")
    @DisplayName("sharedSlots_ShouldBeIncludedInAllPages")
    void sharedSlots_ShouldBeIncludedInAllPages() throws Exception {
      String requestBody = """
          {
            "slotName": "Header",
            "position": "top",
            "sortOrder": 0
          }
          """;

      mockMvc.perform(post("/pages/shared/slots")
          .header("X-Tenant-ID", TEST_TENANT_ID)
          .contentType(MediaType.APPLICATION_JSON)
          .content(requestBody))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.data.isShared").value(true));

      mockMvc.perform(get("/pages/1/slots")
          .header("X-Tenant-ID", TEST_TENANT_ID))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data[?(@.slotName=='Header')]").exists());
    }

    @Test
    @WithMockUser(roles = "TENANT_ADMIN")
    @DisplayName("getSharedSlots_ShouldReturnOnlySharedSlots")
    void getSharedSlots_ShouldReturnOnlySharedSlots() throws Exception {
      mockMvc.perform(get("/pages/shared/slots")
          .header("X-Tenant-ID", TEST_TENANT_ID))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.result").value("SUCCESS"));
    }
  }

  @Nested
  @DisplayName("CMS Page Delivery")
  class CmsPageDeliveryTests {

    @Test
    @DisplayName("cmsPage_ShouldReturnSlotsWithComponentData")
    void cmsPage_ShouldReturnSlotsWithComponentData() throws Exception {
      mockMvc.perform(get("/cms/pages/homepage")
          .header("X-Tenant-ID", TEST_TENANT_ID)
          .param("lang", "TR"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.result").value("SUCCESS"))
          .andExpect(jsonPath("$.data.uid").value("homepage"))
          .andExpect(jsonPath("$.data.slots").exists());
    }

    @Test
    @DisplayName("cmsPagesBatch_ShouldReturnMultiplePages")
    void cmsPagesBatch_ShouldReturnMultiplePages() throws Exception {
      mockMvc.perform(get("/cms/pages")
          .header("X-Tenant-ID", TEST_TENANT_ID)
          .param("uids", "homepage,nonexistent")
          .param("lang", "TR"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.result").value("SUCCESS"))
          .andExpect(jsonPath("$.data.data.homepage").exists())
          .andExpect(jsonPath("$.data.meta.requested").value(2))
          .andExpect(jsonPath("$.data.meta.found").value(1));
    }
  }
}
