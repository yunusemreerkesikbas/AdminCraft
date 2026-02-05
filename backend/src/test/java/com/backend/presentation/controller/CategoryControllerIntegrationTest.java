package com.backend.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import com.backend.application.service.CategoryService;
import com.backend.domain.entity.Category;
import com.backend.domain.repository.TenantRepository;
import com.backend.infrastructure.security.JwtAuthenticationFilter;
import com.backend.infrastructure.tenant.TenantContext;
import com.backend.infrastructure.tenant.TenantFilter;
import com.backend.presentation.config.TestSecurityConfig;
import com.backend.testutil.builders.CategoryTestDataBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Integration tests for CategoryController.
 * Tests HTTP endpoints, request validation, and response formatting.
 */
@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({ TestSecurityConfig.class, com.backend.shared.common.GlobalExceptionHandler.class })
class CategoryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private TenantFilter tenantFilter;

    @MockBean
    private TenantRepository tenantRepository;

    private Category testCategory;
    private Category parentCategory;

    @BeforeEach
    void setUp() {
        TenantContext tenantContext = new TenantContext(tenantRepository);
        tenantContext.setTenantId("1");
        tenantContext.setTenantDbName("ac_tenant_1");

        CategoryTestDataBuilder.resetIdCounter();

        setupMockSecurityContext();

        parentCategory = CategoryTestDataBuilder.aRootCategory()
                .withId(1L)
                .withCode("parent")
                .build();

        testCategory = CategoryTestDataBuilder.aCategory()
                .withId(2L)
                .withCode("test_cat")
                .withParentId(1L)
                .build();
    }

    @AfterEach
    void tearDown() {
        TenantContext ctx = new TenantContext(tenantRepository);
        ctx.clear();
        SecurityContextHolder.clearContext();
    }

    private void setupMockSecurityContext() {
        Map<String, Object> details = new HashMap<>();
        details.put("userId", 1L);
        details.put("tenantId", 1L);
        details.put("role", "TENANT_ADMIN");
        details.put("email", "test@example.com");

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                "test@example.com",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_TENANT_ADMIN")));
        authentication.setDetails(details);

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
    // ==================== GET /api/categories Tests ====================

    @Nested
    @DisplayName("GET /api/categories - Get Category Tree")
    class GetTreeTests {

        @Test
        @DisplayName("Should return category tree")
        void getTree_Success() throws Exception {
            // Given
            when(categoryService.getTree()).thenReturn(List.of(parentCategory));

            // When & Then
            mockMvc.perform(get("/products/categories")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    // ==================== GET /api/categories/{id} Tests ====================

    @Nested
    @DisplayName("GET /api/categories/{id} - Get Category")
    class GetCategoryTests {

        @Test
        @DisplayName("Should return category when found")
        void getCategory_Success() throws Exception {
            // Given
            when(categoryService.findByIdWithI18n(1L)).thenReturn(Optional.of(parentCategory));

            // When & Then
            mockMvc.perform(get("/products/categories/1")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value("SUCCESS"));
        }

        @Test
        @DisplayName("Should return 404 when category not found")
        void getCategory_NotFound() throws Exception {
            // Given
            when(categoryService.findByIdWithI18n(999L)).thenReturn(Optional.empty());

            // When & Then
            mockMvc.perform(get("/products/categories/999")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }

    // ==================== POST /api/categories/composite Tests
    // ====================

    @Nested
    @DisplayName("POST /api/categories/composite - Create Category")
    class CreateCategoryTests {

        @Test
        @DisplayName("Should create category with valid request")
        void createCategory_Success() throws Exception {
            // Given
            Map<String, Object> request = createValidCategoryRequest();
            when(categoryService.createComposite(anyString(), any(), any(), any(), anyMap(), any()))
                    .thenReturn(testCategory);

            // When & Then
            mockMvc.perform(post("/products/categories/composite")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.result").value("SUCCESS"));
        }

        @Test
        @DisplayName("Should return 400 when code is duplicate")
        void createCategory_DuplicateCode() throws Exception {
            // Given
            Map<String, Object> request = createValidCategoryRequest();
            when(categoryService.createComposite(anyString(), any(), any(), any(), anyMap(), any()))
                    .thenThrow(new IllegalArgumentException("Category code already exists"));

            // When & Then
            mockMvc.perform(post("/products/categories/composite")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        private Map<String, Object> createValidCategoryRequest() {
            Map<String, Object> request = new HashMap<>();
            request.put("code", "new_category");
            request.put("isVisible", true);

            Map<String, Map<String, String>> translations = new HashMap<>();
            Map<String, String> trTranslation = new HashMap<>();
            trTranslation.put("name", "Yeni Kategori");
            translations.put("TR", trTranslation);
            request.put("translations", translations);

            return request;
        }
    }

    // ==================== PUT /api/categories/{id}/composite Tests
    // ====================

    @Nested
    @DisplayName("PUT /api/categories/{id}/composite - Update Category")
    class UpdateCategoryTests {

        @Test
        @DisplayName("Should update category with valid request")
        void updateCategory_Success() throws Exception {
            // Given
            Map<String, Object> request = new HashMap<>();
            request.put("isVisible", false);

            Map<String, Map<String, String>> translations = new HashMap<>();
            Map<String, String> trTranslation = new HashMap<>();
            trTranslation.put("name", "Güncel Kategori");
            translations.put("TR", trTranslation);
            request.put("translations", translations);

            when(categoryService.updateComposite(eq(1L), any(), any(), any(), anyMap(), any()))
                    .thenReturn(parentCategory);

            // When & Then
            mockMvc.perform(put("/products/categories/1/composite")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value("SUCCESS"));
        }

        @Test
        @DisplayName("Should return 400 when trying to set self as parent")
        void updateCategory_CircularParent() throws Exception {
            // Given
            Map<String, Object> request = new HashMap<>();
            request.put("parentId", 1L); // Same as category ID

            when(categoryService.updateComposite(eq(1L), eq(1L), any(), any(), any(), any()))
                    .thenThrow(new IllegalArgumentException("Category cannot be its own parent"));

            // When & Then
            mockMvc.perform(put("/products/categories/1/composite")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ==================== DELETE /api/categories/{id} Tests ====================

    @Nested
    @DisplayName("DELETE /api/categories/{id} - Delete Category")
    class DeleteCategoryTests {

        @Test
        @DisplayName("Should delete category successfully")
        void deleteCategory_Success() throws Exception {
            // Given
            doNothing().when(categoryService).delete(2L);

            // When & Then
            mockMvc.perform(delete("/products/categories/2")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value("SUCCESS"));
        }

        @Test
        @DisplayName("Should return 409 when category has children")
        void deleteCategory_HasChildren() throws Exception {
            // Given
            doThrow(new IllegalStateException("Cannot delete category with children"))
                    .when(categoryService).delete(1L);

            // When & Then
            mockMvc.perform(delete("/products/categories/1")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Should return 409 when category has products")
        void deleteCategory_HasProducts() throws Exception {
            // Given
            doThrow(new IllegalStateException("Cannot delete category with products"))
                    .when(categoryService).delete(2L);

            // When & Then
            mockMvc.perform(delete("/products/categories/2")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isConflict());
        }
    }
}
