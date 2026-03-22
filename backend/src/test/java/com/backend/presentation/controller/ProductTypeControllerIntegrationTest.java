package com.backend.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import com.backend.application.service.ProductTypeService;
import com.backend.domain.entity.ProductAttributeDefinition;
import com.backend.domain.entity.ProductType;
import com.backend.domain.enums.ProductFieldType;
import com.backend.domain.exception.BusinessRuleViolationException;
import com.backend.domain.repository.TenantRepository;
import com.backend.infrastructure.tenant.TenantContext;
import com.backend.presentation.config.TestSecurityConfig;
import com.backend.testutil.builders.ProductAttributeDefinitionTestDataBuilder;
import com.backend.testutil.builders.ProductTypeTestDataBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Integration tests for ProductTypeController.
 * Tests HTTP endpoints, request validation, and response formatting.
 * Special attention to BusinessRuleViolationException returning 409 Conflict.
 */
@WebMvcTest(ProductTypeController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({ TestSecurityConfig.class, com.backend.shared.common.GlobalExceptionHandler.class })
class ProductTypeControllerIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private ProductTypeService productTypeService;

        @MockBean
        private com.backend.infrastructure.security.JwtAuthenticationFilter jwtAuthenticationFilter;

        @MockBean
        private com.backend.infrastructure.tenant.TenantFilter tenantFilter;

        @MockBean
        private TenantRepository tenantRepository;

        private ProductType testProductType;
        private ProductAttributeDefinition testAttributeDefinition;

        @BeforeEach
        void setUp() {
                TenantContext tenantContext = new TenantContext(tenantRepository);
                tenantContext.setTenantId("1");
                tenantContext.setTenantDbName("ac_tenant_1");

                ProductTypeTestDataBuilder.resetIdCounter();
                ProductAttributeDefinitionTestDataBuilder.resetIdCounter();

                setupMockSecurityContext();

                testProductType = ProductTypeTestDataBuilder.aProductType()
                                .withId(1L)
                                .withCode("general")
                                .withName("General Product")
                                .withCategory("electronics")
                                .build();

                testAttributeDefinition = ProductAttributeDefinitionTestDataBuilder.anAttributeDefinition()
                                .withId(1L)
                                .withProductType(testProductType)
                                .withCode("color")
                                .withName("Color")
                                .withFieldType(ProductFieldType.TEXT)
                                .build();

                testProductType.getAttributeDefinitions().add(testAttributeDefinition);
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

        // ==================== GET /api/products/types Tests ====================

        @Nested
        @DisplayName("GET /api/products/types - List Product Types")
        class ListTypesTests {

                @Test
                @DisplayName("Should return paginated product type list")
                void listTypes_Success() throws Exception {
                        // Given
                        Page<ProductType> typePage = new PageImpl<>(List.of(testProductType), PageRequest.of(0, 20), 1);
                        when(productTypeService.search(isNull(), any(Pageable.class))).thenReturn(typePage);

                        // When & Then
                        mockMvc.perform(get("/products/types")
                                        .param("page", "0")
                                        .param("size", "20")
                                        .contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.result").value("SUCCESS"))
                                        .andExpect(jsonPath("$.data.content").isArray());
                }
        }

        // ==================== GET /api/products/types/{id} Tests ====================

        @Nested
        @DisplayName("GET /api/products/types/{id} - Get Product Type")
        class GetTypeTests {

                @Test
                @DisplayName("Should return product type with attributes")
                void getType_Success() throws Exception {
                        // Given
                        when(productTypeService.findByIdWithAttributes(1L)).thenReturn(Optional.of(testProductType));

                        // #region agent log
                        String responseBody = null;
                        int status = 0;
                        try {
                                var result = mockMvc.perform(get("/products/types/1")
                                                .contentType(MediaType.APPLICATION_JSON))
                                                .andReturn();
                                status = result.getResponse().getStatus();
                                responseBody = result.getResponse().getContentAsString();

                                try (var writer = new java.io.FileWriter(
                                                "c:\\Users\\emreerkesikbas\\Documents\\Craftive\\.cursor\\debug.log",
                                                true)) {
                                        writer.write(new com.fasterxml.jackson.databind.ObjectMapper()
                                                        .writeValueAsString(java.util.Map.of(
                                                                        "timestamp", System.currentTimeMillis(),
                                                                        "location",
                                                                        "ProductTypeControllerIntegrationTest.getType_Success",
                                                                        "message", "Response details after request",
                                                                        "data", java.util.Map.of(
                                                                                        "status", status,
                                                                                        "responseBody",
                                                                                        responseBody != null
                                                                                                        ? responseBody
                                                                                                        : "null",
                                                                                        "contentType",
                                                                                        result.getResponse()
                                                                                                        .getContentType() != null
                                                                                                                        ? result.getResponse()
                                                                                                                                        .getContentType()
                                                                                                                        : "null",
                                                                                        "handler",
                                                                                        result.getHandler() != null
                                                                                                        ? result.getHandler()
                                                                                                                        .getClass()
                                                                                                                        .getName()
                                                                                                        : "null"),
                                                                        "sessionId", "debug-session",
                                                                        "hypothesisId", "A"))
                                                        + "\n");
                                } catch (Exception e) {
                                }
                        } catch (Exception e) {
                                try (var writer = new java.io.FileWriter(
                                                "c:\\Users\\emreerkesikbas\\Documents\\Craftive\\.cursor\\debug.log",
                                                true)) {
                                        writer.write(new com.fasterxml.jackson.databind.ObjectMapper()
                                                        .writeValueAsString(java.util.Map.of(
                                                                        "timestamp", System.currentTimeMillis(),
                                                                        "location",
                                                                        "ProductTypeControllerIntegrationTest.getType_Success",
                                                                        "message", "Exception during request",
                                                                        "data",
                                                                        java.util.Map.of("exception",
                                                                                        e.getClass().getName(),
                                                                                        "message", e.getMessage()),
                                                                        "sessionId", "debug-session",
                                                                        "hypothesisId", "A"))
                                                        + "\n");
                                } catch (Exception ex) {
                                }
                        }
                        // #endregion agent log

                        // When & Then
                        mockMvc.perform(get("/products/types/1")
                                        .contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.result").value("SUCCESS"));
                }

                @Test
                @DisplayName("Should return 404 when product type not found")
                void getType_NotFound() throws Exception {
                        // Given
                        when(productTypeService.findByIdWithAttributes(999L)).thenReturn(Optional.empty());

                        // When & Then
                        mockMvc.perform(get("/products/types/999")
                                        .contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isNotFound());
                }
        }

        // ==================== POST /api/products/types Tests ====================

        @Nested
        @DisplayName("POST /api/products/types - Create Product Type")
        class CreateTypeTests {

                @Test
                @DisplayName("Should create product type with valid request")
                void createType_Success() throws Exception {
                        // Given
                        Map<String, Object> request = new HashMap<>();
                        request.put("code", "new_type");
                        request.put("name", "New Type");
                        request.put("category", "general");

                        when(productTypeService.create(anyString(), anyString(), anyString(), any()))
                                        .thenReturn(testProductType);

                        // When & Then
                        mockMvc.perform(post("/products/types")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isCreated())
                                        .andExpect(jsonPath("$.result").value("SUCCESS"));
                }

                @Test
                @DisplayName("Should return 400 when code is duplicate")
                void createType_DuplicateCode() throws Exception {
                        // Given
                        Map<String, Object> request = new HashMap<>();
                        request.put("code", "existing_code");
                        request.put("name", "Test");

                        when(productTypeService.create(eq("existing_code"), anyString(), any(), any()))
                                        .thenThrow(new IllegalArgumentException("Product type code already exists"));

                        // When & Then
                        mockMvc.perform(post("/products/types")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isBadRequest());
                }
        }

        // ==================== DELETE /api/products/types/{id} Tests
        // ====================

        @Nested
        @DisplayName("DELETE /api/products/types/{id} - Delete Product Type")
        class DeleteTypeTests {

                @Test
                @DisplayName("Should delete product type successfully")
                void deleteType_Success() throws Exception {
                        // Given
                        doNothing().when(productTypeService).delete(1L);

                        // When & Then
                        mockMvc.perform(delete("/products/types/1")
                                        .contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.result").value("SUCCESS"));
                }

                @Test
                @DisplayName("Should return 409 Conflict when product type has products")
                void deleteType_HasProducts() throws Exception {
                        // Given - BusinessRuleViolationException should return 409
                        doThrow(new BusinessRuleViolationException(
                                        "Cannot delete product type with 5 products assigned"))
                                        .when(productTypeService).delete(1L);

                        // When & Then
                        mockMvc.perform(delete("/products/types/1")
                                        .contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isConflict()); // 409 NOT 400!
                }
        }

        // ==================== POST /api/products/types/{id}/attributes Tests
        // ====================

        @Nested
        @DisplayName("POST /api/products/types/{id}/attributes - Add Attribute")
        class AddAttributeTests {

                @Test
                @DisplayName("Should add attribute successfully")
                void addAttribute_Success() throws Exception {
                        // Given
                        Map<String, Object> request = new HashMap<>();
                        request.put("name", "Size");
                        request.put("fieldType", "TEXT");

                        when(productTypeService.addAttribute(eq(1L), anyString(),
                                        any(ProductFieldType.class)))
                                        .thenReturn(testAttributeDefinition);

                        // When & Then
                        mockMvc.perform(post("/products/types/1/attributes")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isCreated())
                                        .andExpect(jsonPath("$.result").value("SUCCESS"));
                }

                @Test
                @DisplayName("Should return 400 when attribute code is duplicate")
                void addAttribute_DuplicateCode() throws Exception {
                        // Given
                        Map<String, Object> request = new HashMap<>();
                        request.put("name", "Existing");
                        request.put("fieldType", "TEXT");

                        when(productTypeService.addAttribute(eq(1L), anyString(),
                                        any(ProductFieldType.class)))
                                        .thenThrow(new IllegalArgumentException("Attribute code already exists"));

                        // When & Then
                        mockMvc.perform(post("/products/types/1/attributes")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isBadRequest());
                }
        }

        // ==================== PUT /api/products/types/{id}/attributes/{attrId} Tests
        // ====================

        @Nested
        @DisplayName("PUT /api/products/types/{id}/attributes/{attrId} - Update Attribute")
        class UpdateAttributeTests {

                @Test
                @DisplayName("Should update attribute successfully")
                void updateAttribute_Success() throws Exception {
                        // Given
                        Map<String, Object> request = new HashMap<>();
                        request.put("name", "Updated Name");
                        request.put("fieldType", "NUMBER");

                        when(productTypeService.updateAttribute(eq(1L), eq(1L), anyString(),
                                        any(ProductFieldType.class)))
                                        .thenReturn(testAttributeDefinition);

                        // When & Then
                        mockMvc.perform(put("/products/types/1/attributes/1")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.result").value("SUCCESS"));
                }

                @Test
                @DisplayName("Should return 400 when attribute not found")
                void updateAttribute_NotFound() throws Exception {
                        // Given
                        Map<String, Object> request = new HashMap<>();
                        request.put("name", "Updated");

                        when(productTypeService.updateAttribute(eq(1L), eq(999L), anyString(),
                                        any(ProductFieldType.class)))
                                        .thenThrow(new IllegalArgumentException("Attribute not found"));

                        // When & Then
                        mockMvc.perform(put("/products/types/1/attributes/999")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isBadRequest());
                }
        }

        // ==================== DELETE /api/products/types/{id}/attributes/{attrId}
        // Tests ====================

        @Nested
        @DisplayName("DELETE /api/products/types/{id}/attributes/{attrId} - Delete Attribute")
        class DeleteAttributeTests {

                @Test
                @DisplayName("Should delete attribute successfully")
                void deleteAttribute_Success() throws Exception {
                        // Given
                        doNothing().when(productTypeService).deleteAttribute(1L, 1L);

                        // When & Then
                        mockMvc.perform(delete("/products/types/1/attributes/1")
                                        .contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.result").value("SUCCESS"));
                }

                @Test
                @DisplayName("Should return 400 when attribute belongs to wrong product type")
                void deleteAttribute_WrongProductType() throws Exception {
                        // Given
                        doThrow(new IllegalArgumentException("Attribute does not belong to this product type"))
                                        .when(productTypeService).deleteAttribute(1L, 999L);

                        // When & Then
                        mockMvc.perform(delete("/products/types/1/attributes/999")
                                        .contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isBadRequest());
                }
        }
}
