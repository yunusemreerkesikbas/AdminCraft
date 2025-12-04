package com.backend.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class ComponentControllerIntegrationTest {

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

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  @WithMockUser(roles = "TENANT_ADMIN")
  void listComponents_ShouldReturnFlatStructureWithTypeName() throws Exception {
    MvcResult result = mockMvc.perform(get("/components")
        .header("Accept-Language", "tr"))
        .andExpect(status().isOk())
        .andReturn();

    String responseJson = result.getResponse().getContentAsString();

    assertThat(responseJson).contains("\"success\":true");
    assertThat(responseJson).contains("componentTypeName");
  }

  @Test
  @WithMockUser(roles = "TENANT_ADMIN")
  void listComponents_ResponseShouldHaveRequiredFields() throws Exception {
    MvcResult result = mockMvc.perform(get("/components")
        .header("Accept-Language", "en"))
        .andExpect(status().isOk())
        .andReturn();

    String responseJson = result.getResponse().getContentAsString();

    assertThat(responseJson).contains("\"id\":");
    assertThat(responseJson).contains("\"name\":");
    assertThat(responseJson).contains("\"componentTypeId\":");
    assertThat(responseJson).contains("\"componentTypeName\":");
    assertThat(responseJson).contains("\"code\":");
    assertThat(responseJson).contains("\"status\":");
  }

  @Test
  @WithMockUser(roles = "TENANT_ADMIN")
  void listComponents_ShouldNotReturnNestedStructure() throws Exception {
    MvcResult result = mockMvc.perform(get("/components")
        .header("Accept-Language", "tr"))
        .andExpect(status().isOk())
        .andReturn();

    String responseJson = result.getResponse().getContentAsString();

    assertThat(responseJson).doesNotContain("\"component\":{");
    assertThat(responseJson).doesNotContain("\"translations\":[");
  }
}
