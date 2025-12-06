package com.backend.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class CmsDeliveryControllerIntegrationTest {

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

  @Test
  void getByUid_ShouldBeAccessibleWithoutAuth() throws Exception {
    mockMvc.perform(get("/cms/components/nonexistent-uid")
        .header("Accept-Language", "tr"))
        .andExpect(status().isNotFound());
  }

  @Test
  void getByUid_ShouldReturn404ForInvalidUid() throws Exception {
    mockMvc.perform(get("/cms/components/invalid-uid")
        .param("lang", "TR")
        .header("Accept-Language", "tr"))
        .andExpect(status().isNotFound());
  }

  @Test
  void getByUids_ShouldReturnBadRequestForEmptyUids() throws Exception {
    mockMvc.perform(get("/cms/components")
        .header("Accept-Language", "tr"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.result").value("ERROR"));
  }

  @Test
  void getByUids_ShouldReturnBadRequestForTooManyUids() throws Exception {
    StringBuilder uids = new StringBuilder();
    for (int i = 0; i < 51; i++) {
      if (i > 0)
        uids.append(",");
      uids.append("uid").append(i);
    }

    mockMvc.perform(get("/cms/components")
        .param("uids", uids.toString())
        .header("Accept-Language", "tr"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.result").value("ERROR"));
  }

  @Test
  void getByUids_ShouldReturnMapResponseWithMeta() throws Exception {
    MvcResult result = mockMvc.perform(get("/cms/components")
        .param("uids", "uid1,uid2")
        .header("Accept-Language", "tr"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result").value("SUCCESS"))
        .andExpect(jsonPath("$.data.meta").exists())
        .andExpect(jsonPath("$.data.meta.requested").value(2))
        .andExpect(jsonPath("$.data.meta.notFound").isArray())
        .andReturn();

    String responseJson = result.getResponse().getContentAsString();
    assertThat(responseJson).contains("\"data\":");
    assertThat(responseJson).contains("\"meta\":");
  }

  @Test
  void getByUid_ShouldAcceptLangParameter() throws Exception {
    mockMvc.perform(get("/cms/components/test-uid")
        .param("lang", "EN")
        .header("Accept-Language", "en"))
        .andExpect(status().isNotFound());
  }
}
