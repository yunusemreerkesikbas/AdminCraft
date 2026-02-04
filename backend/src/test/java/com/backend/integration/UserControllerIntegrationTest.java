package com.backend.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Integration tests for User Management Controller.
 * 
 * These tests verify:
 * 1. Search + sort work together (paginated search with sorting)
 * 2. Reset password returns expected response shape and status
 * 3. Account lockout after N failed logins returns ACCOUNT_LOCKED with remaining minutes
 * 
 * Note: These tests are currently disabled due to tenant setup complexity.
 * The multi-tenant architecture requires:
 * - Platform DB initialization
 * - Tenant DB creation and migration
 * - X-Tenant-ID header handling
 * - Tenant context setup
 * 
 * For proper testing, consider:
 * - Creating a test helper to provision a test tenant
 * - Adding test data fixtures for users
 * - Implementing tenant context injection for tests
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@org.junit.jupiter.api.Disabled("TODO: Add tenant provisioning test helper and X-Tenant-ID header setup")
public class UserControllerIntegrationTest {

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
        registry.add("spring.datasource.tenant.jdbc-url-template",
                () -> "jdbc:mysql://" + mysql.getHost() + ":" + mysql.getFirstMappedPort()
                        + "/{dbName}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Europe/Istanbul&characterEncoding=UTF-8&useUnicode=true");
        registry.add("spring.flyway.url", mysql::getJdbcUrl);
        registry.add("spring.flyway.user", mysql::getUsername);
        registry.add("spring.flyway.password", mysql::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Test 1: Verify that search and sort work together.
     * 
     * Requirements:
     * - Search parameter filters results
     * - Sort parameter orders filtered results
     * - Pagination works with both search and sort
     */
    @Test
    @WithMockUser(roles = "TENANT_ADMIN")
    void searchUsers_WithSortParameter_ShouldReturnSortedResults() throws Exception {
        // Given: Search for users with "admin" and sort by email ascending
        String searchTerm = "admin";
        String sortParam = "email,asc";

        // When: Perform search with sort
        MvcResult result = mockMvc.perform(get("/users")
                .param("search", searchTerm)
                .param("sort", sortParam)
                .param("page", "0")
                .param("size", "20")
                .header("Accept-Language", "en")
                .header("X-Tenant-ID", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.sortConfig.currentSort").value(sortParam))
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();

        // Then: Response should contain sorted results
        assertThat(responseJson).contains("\"result\":\"SUCCESS\"");
        assertThat(responseJson).contains("\"sortConfig\"");
        assertThat(responseJson).contains("\"currentSort\":\"email,asc\"");
    }

    /**
     * Test 2: Verify reset password sends email and returns success.
     * 
     * Requirements:
     * - Returns 200 OK status
     * - Response result is SUCCESS
     * - data field is null (no password returned)
     * - Success message confirms email was sent
     */
    @Test
    @WithMockUser(roles = "TENANT_ADMIN")
    void resetPassword_ShouldSendEmailAndReturnSuccess() throws Exception {
        // Given: A valid user ID
        Long userId = 1L;

        // When: Reset password (sends email instead of returning password)
        MvcResult result = mockMvc.perform(post("/users/{id}/reset-password", userId)
                .header("Accept-Language", "en")
                .header("X-Tenant-ID", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.data").doesNotExist())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();

        // Then: Response should confirm email was sent (no password in response)
        assertThat(responseJson).contains("\"result\":\"SUCCESS\"");
        assertThat(responseJson).doesNotContain("\"newPassword\"");
    }

    /**
     * Test 3: Verify account lockout after failed login attempts.
     * 
     * Requirements:
     * - After N failed attempts, account is locked
     * - Response contains ACCOUNT_LOCKED error
     * - Response includes remainingMinutes field
     * - HTTP status is appropriate (401 or 403)
     * 
     * Note: This test would require:
     * - Creating a test user
     * - Attempting N failed logins
     * - Verifying the lockout response
     */
    @Test
    void login_AfterMaxFailedAttempts_ShouldReturnAccountLockedWithRemainingMinutes() throws Exception {
        // Given: A user with multiple failed login attempts
        String loginRequest = """
                {
                    "email": "test@example.com",
                    "password": "wrongpassword",
                    "tenantSubdomain": "test-tenant"
                }
                """;

        // When: Attempt login after max failed attempts (simulate by creating user with locked status)
        // Then: Should return ACCOUNT_LOCKED error with remainingMinutes
        
        // This test requires:
        // 1. Create a test user
        // 2. Set failed_login_attempts to MAX_FAILED_ATTEMPTS
        // 3. Set locked_until to current time + lock duration
        // 4. Attempt login
        // 5. Verify response contains:
        //    - Error code for ACCOUNT_LOCKED
        //    - remainingMinutes field
        //    - Appropriate HTTP status
        
        // Example expected response:
        // {
        //   "result": "ERROR",
        //   "message": "Account is locked. Try again in X minutes.",
        //   "error": {
        //     "code": "ACCOUNT_LOCKED",
        //     "remainingMinutes": 30
        //   }
        // }
    }

    /**
     * Test 4: Verify that sort works with different fields.
     * 
     * Requirements:
     * - Sort by createdAt (default)
     * - Sort by email
     * - Sort by email
     * - Sort by role
     * - Sort by isActive
     * - Sort by lastLoginAt
     */
    @Test
    @WithMockUser(roles = "TENANT_ADMIN")
    void listUsers_WithDifferentSortFields_ShouldReturnSortedResults() throws Exception {
        String[] sortFields = { "createdAt,desc", "email,desc", "role,asc", "isActive,desc",
                "lastLoginAt,desc" };

        for (String sort : sortFields) {
            mockMvc.perform(get("/users")
                    .param("sort", sort)
                    .param("page", "0")
                    .param("size", "20")
                    .header("Accept-Language", "en")
                    .header("X-Tenant-ID", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.sortConfig.currentSort").value(sort));
        }
    }

    /**
     * Test 5: Verify pageable response structure.
     * 
     * Requirements:
     * - Response contains content array
     * - Response contains page metadata
     * - Response contains sortConfig
     */
    @Test
    @WithMockUser(roles = "TENANT_ADMIN")
    void listUsers_ShouldReturnPageableResponseWithAllFields() throws Exception {
        mockMvc.perform(get("/users")
                .param("page", "0")
                .param("size", "20")
                .header("Accept-Language", "en")
                .header("X-Tenant-ID", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.page").isNumber())
                .andExpect(jsonPath("$.data.size").isNumber())
                .andExpect(jsonPath("$.data.totalElements").exists())
                .andExpect(jsonPath("$.data.totalPages").exists())
                .andExpect(jsonPath("$.data.sortConfig").exists())
                .andExpect(jsonPath("$.data.sortConfig.currentSort").exists())
                .andExpect(jsonPath("$.data.sortConfig.availableSorts").isArray());
    }
}
