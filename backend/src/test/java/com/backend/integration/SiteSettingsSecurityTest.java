package com.backend.integration;

import com.backend.domain.entity.SiteSetting;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.SettingType;
import com.backend.domain.repository.SiteSettingRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Site Settings security and tenant isolation
 * Tests critical security requirements identified in code review
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
public class SiteSettingsSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SiteSettingRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    private static final Long TENANT_1_ID = 1L;
    private static final Long TENANT_2_ID = 2L;
    private static final Long USER_1_ID = 1L;

    @BeforeEach
    void setUp() {
        // Create test data for different tenants
        createTestSettings(TENANT_1_ID, "tenant1@example.com");
        createTestSettings(TENANT_2_ID, "tenant2@example.com");
    }

    @Test
    @DisplayName("Should require authentication to access settings")
    void shouldRequireAuthenticationToAccessSettings() throws Exception {
        mockMvc.perform(get("/api/site-settings"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should require TENANT_ADMIN role to access settings")
    @WithMockUser(roles = "USER")
    void shouldRequireTenantAdminRoleToAccessSettings() throws Exception {
        mockMvc.perform(get("/api/site-settings"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should only return settings for current tenant")
    @WithMockUser(roles = "TENANT_ADMIN", username = "admin1")
    void shouldOnlyReturnSettingsForCurrentTenant() throws Exception {
        // Mock security helper to return tenant 1
        mockMvc.perform(get("/api/site-settings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.global.contactEmail", is("tenant1@example.com")))
                .andExpect(jsonPath("$.data.global.contactEmail", not("tenant2@example.com")));
    }

    @Test
    @DisplayName("Should prevent cross-tenant data access via URL manipulation")
    @WithMockUser(roles = "TENANT_ADMIN", username = "admin1")
    void shouldPreventCrossTenantDataAccess() throws Exception {
        // Even if we try to access tenant 2's data, should only get tenant 1's data
        mockMvc.perform(get("/api/site-settings?tenantId=" + TENANT_2_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.global.contactEmail", is("tenant1@example.com")));
    }

    @Test
    @DisplayName("Should validate URL whitelist security")
    @WithMockUser(roles = "TENANT_ADMIN", username = "admin1")
    void shouldValidateUrlWhitelistSecurity() throws Exception {
        Map<String, Object> maliciousPayload = new HashMap<>();
        Map<String, String> global = new HashMap<>();
        global.put("canonicalBaseUrl", "https://malicious-site.com/steal-data");
        maliciousPayload.put("global", global);

        mockMvc.perform(patch("/api/site-settings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(maliciousPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("validation")));
    }

    @Test
    @DisplayName("Should validate phone number E.164 format")
    @WithMockUser(roles = "TENANT_ADMIN", username = "admin1")
    void shouldValidatePhoneNumberFormat() throws Exception {
        Map<String, Object> invalidPayload = new HashMap<>();
        Map<String, String> global = new HashMap<>();
        global.put("contactPhone", "invalid-phone-123");
        invalidPayload.put("global", global);

        mockMvc.perform(patch("/api/site-settings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("E.164")));
    }

    @Test
    @DisplayName("Should validate robots.txt format")
    @WithMockUser(roles = "TENANT_ADMIN", username = "admin1")
    void shouldValidateRobotsFormat() throws Exception {
        Map<String, Object> invalidPayload = new HashMap<>();
        Map<String, String> global = new HashMap<>();
        global.put("robots", "invalid-robots-directive");
        invalidPayload.put("global", global);

        mockMvc.perform(patch("/api/site-settings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidPayload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("index,follow")));
    }

    @Test
    @DisplayName("Should return proper API response structure")
    @WithMockUser(roles = "TENANT_ADMIN", username = "admin1")
    void shouldReturnProperApiResponseStructure() throws Exception {
        mockMvc.perform(get("/api/site-settings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.global").exists())
                .andExpect(jsonPath("$.data.languages").exists())
                .andExpect(jsonPath("$.data.languages.tr").exists())
                .andExpect(jsonPath("$.data.languages.en").exists());
    }

    @Test
    @DisplayName("Should handle partial updates correctly")
    @WithMockUser(roles = "TENANT_ADMIN", username = "admin1")
    void shouldHandlePartialUpdatesCorrectly() throws Exception {
        // Update only global email
        Map<String, Object> partialUpdate = new HashMap<>();
        Map<String, String> global = new HashMap<>();
        global.put("contactEmail", "updated@tenant1.com");
        partialUpdate.put("global", global);

        mockMvc.perform(patch("/api/site-settings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(partialUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.global.contactEmail", is("updated@tenant1.com")));
    }

    private void createTestSettings(Long tenantId, String email) {
        // Create global settings
        SiteSetting emailSetting = createSetting(tenantId, "global.contactEmail", email, null, SettingType.TEXT);
        SiteSetting phoneSetting = createSetting(tenantId, "global.contactPhone", "+1234567890", null, SettingType.TEXT);
        
        // Create language-specific settings
        SiteSetting siteNameTr = createSetting(tenantId, "i18n.siteName", "Site Adı", Language.TR, SettingType.I18N_TEXT);
        SiteSetting siteNameEn = createSetting(tenantId, "i18n.siteName", "Site Name", Language.EN, SettingType.I18N_TEXT);

        repository.saveAll(List.of(emailSetting, phoneSetting, siteNameTr, siteNameEn));
    }

    private SiteSetting createSetting(Long tenantId, String key, String value, Language language, SettingType type) {
        SiteSetting setting = new SiteSetting();
        setting.setTenantId(tenantId);
        setting.setSettingKey(key);
        setting.setSettingValue(value);
        setting.setLanguage(language);
        setting.setSettingType(type);
        setting.setCategory("general");
        setting.setIsPublic(false);
        setting.setSortOrder(0);
        setting.setUpdatedBy(USER_1_ID);
        setting.setUpdatedAt(LocalDateTime.now());
        return setting;
    }
}