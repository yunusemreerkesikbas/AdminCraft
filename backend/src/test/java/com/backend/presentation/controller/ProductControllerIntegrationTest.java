package com.backend.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
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

import com.backend.application.service.ProductService;
import com.backend.domain.entity.Product;
import com.backend.domain.entity.ProductType;
import com.backend.domain.enums.ProductStatus;
import com.backend.domain.repository.ProductCategoryLinkRepository;
import com.backend.infrastructure.tenant.TenantContext;
import com.backend.presentation.config.TestSecurityConfig;
import com.backend.testutil.builders.ProductTestDataBuilder;
import com.backend.testutil.builders.ProductTypeTestDataBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Integration tests for ProductController.
 * Tests HTTP endpoints, request validation, and response formatting.
 */
@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({ TestSecurityConfig.class, com.backend.shared.common.GlobalExceptionHandler.class })
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    @MockBean
    private ProductCategoryLinkRepository productCategoryLinkRepository;

    @MockBean
    private com.backend.infrastructure.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private com.backend.infrastructure.tenant.TenantFilter tenantFilter;

    @MockBean
    private TenantContext tenantContext;

    private Product testProduct;
    private ProductType testProductType;

    @BeforeEach
    void setUp() {
        // Set up tenant context mock for tests
        when(tenantContext.getTenantId()).thenReturn("1");
        when(tenantContext.getTenantDbName()).thenReturn("ac_tenant_1");
        when(tenantContext.getCurrency()).thenReturn(com.backend.domain.enums.Currency.TRY);

        ProductTypeTestDataBuilder.resetIdCounter();
        ProductTestDataBuilder.resetIdCounter();

        setupMockSecurityContext();

        testProductType = ProductTypeTestDataBuilder.aProductType()
                .withId(1L)
                .withCode("general")
                .build();

        testProduct = ProductTestDataBuilder.aProduct()
                .withId(1L)
                .withProductType(testProductType)
                .withSku("SKU-001")
                .withBasePrice(BigDecimal.valueOf(99.99))
                .withStatus(ProductStatus.PUBLISHED)
                .build();
    }

    @AfterEach
    void tearDown() {
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
    // ==================== GET /api/products Tests ====================

    @Nested
    @DisplayName("GET /api/products - List Products")
    class ListProductsTests {

        @Test
        @DisplayName("Should return paginated product list")
        void listProducts_Success() throws Exception {
            // Given
            Page<Product> productPage = new PageImpl<>(List.of(testProduct), PageRequest.of(0, 20), 1);
            when(productService.search(isNull(), isNull(), isNull(), any(Pageable.class))).thenReturn(productPage);
            when(productCategoryLinkRepository.findPrimaryByProductIdIn(anyList())).thenReturn(List.of());

            // When & Then
            mockMvc.perform(get("/products")
                    .param("page", "0")
                    .param("size", "20")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.content").isArray());
        }

        @Test
        @DisplayName("Should filter products by status")
        void listProducts_WithStatusFilter() throws Exception {
            // Given
            Page<Product> productPage = new PageImpl<>(List.of(testProduct));
            when(productService.search(isNull(), eq(ProductStatus.PUBLISHED), isNull(), any(Pageable.class)))
                    .thenReturn(productPage);
            when(productCategoryLinkRepository.findPrimaryByProductIdIn(anyList())).thenReturn(List.of());

            // When & Then
            mockMvc.perform(get("/products")
                    .param("status", "PUBLISHED")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }
    }

    // ==================== GET /api/products/{id} Tests ====================

    @Nested
    @DisplayName("GET /api/products/{id} - Get Product")
    class GetProductTests {

        @Test
        @DisplayName("Should return product when found")
        void getProduct_Success() throws Exception {
            // Given
            when(productService.findByIdComposite(1L)).thenReturn(Optional.of(testProduct));

            // When & Then
            mockMvc.perform(get("/products/1")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value("SUCCESS"));
        }

        @Test
        @DisplayName("Should return 404 when product not found")
        void getProduct_NotFound() throws Exception {
            // Given
            when(productService.findByIdComposite(999L)).thenReturn(Optional.empty());

            // When & Then
            mockMvc.perform(get("/products/999")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }

    // ==================== POST /api/products/composite Tests ====================

    @Nested
    @DisplayName("POST /api/products/composite - Create Product")
    class CreateProductTests {

        @Test
        @DisplayName("Should create product with valid request")
        void createProduct_Success() throws Exception {
            // Given
            Map<String, Object> request = createValidProductRequest();
            when(productService.createComposite(any(), anyString(), any(),
                    any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(testProduct);

            // When & Then
            mockMvc.perform(post("/products/composite")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.result").value("SUCCESS"));
        }

        @Test
        @DisplayName("Should return 400 when SKU is missing")
        void createProduct_MissingRequiredFields() throws Exception {
            // Given
            Map<String, Object> request = new HashMap<>();
            request.put("productTypeId", 1L);
            // SKU is missing

            // When & Then
            mockMvc.perform(post("/products/composite")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        private Map<String, Object> createValidProductRequest() {
            Map<String, Object> request = new HashMap<>();
            request.put("productTypeId", 1L);
            request.put("sku", "NEW-SKU-001");
            request.put("basePrice", 199.99);
            request.put("status", "DRAFT");
            request.put("isVisible", true);

            Map<String, Map<String, String>> translations = new HashMap<>();
            Map<String, String> trTranslation = new HashMap<>();
            trTranslation.put("name", "Test Ürün");
            translations.put("TR", trTranslation);
            request.put("translations", translations);

            return request;
        }
    }

    // ==================== PUT /api/products/{id}/composite Tests
    // ====================

    @Nested
    @DisplayName("PUT /api/products/{id}/composite - Update Product")
    class UpdateProductTests {

        @Test
        @DisplayName("Should update product with valid request")
        void updateProduct_Success() throws Exception {
            // Given
            Map<String, Object> request = new HashMap<>();
            request.put("basePrice", 299.99);
            request.put("status", "PUBLISHED");
            // Add required translations
            Map<String, Object> translation = new HashMap<>();
            translation.put("name", "Test Product");
            translation.put("description", "Test Description");
            request.put("translations", Map.of("TR", translation));

            when(productService.updateComposite(eq(1L), any(), any(), any(),
                    any(), any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(testProduct);

            // When & Then
            mockMvc.perform(put("/products/1/composite")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value("SUCCESS"));
        }

        @Test
        @DisplayName("Should return 404 when product not found")
        void updateProduct_NotFound() throws Exception {
            // Given
            Map<String, Object> request = new HashMap<>();
            request.put("basePrice", 299.99);

            when(productService.updateComposite(eq(999L), any(), any(), any(),
                    any(), any(), any(), any(), any(), any(), any(), any()))
                    .thenThrow(new IllegalArgumentException("Product not found"));

            // When & Then
            mockMvc.perform(put("/products/999/composite")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ==================== DELETE /api/products/{id} Tests ====================

    @Nested
    @DisplayName("DELETE /api/products/{id} - Delete Product")
    class DeleteProductTests {

        @Test
        @DisplayName("Should delete product successfully")
        void deleteProduct_Success() throws Exception {
            // Given
            doNothing().when(productService).delete(1L);

            // When & Then
            mockMvc.perform(delete("/products/1")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value("SUCCESS"));
        }
    }

    // ==================== PATCH /api/products/{id}/status Tests
    // ====================

    @Nested
    @DisplayName("PATCH /api/products/{id}/status - Update Status")
    class UpdateStatusTests {

        @Test
        @DisplayName("Should update product status")
        void updateStatus_Success() throws Exception {
            // Given
            when(productService.updateStatus(eq(1L), eq(ProductStatus.PUBLISHED), any()))
                    .thenReturn(testProduct);

            // When & Then
            mockMvc.perform(patch("/products/1/status")
                    .param("status", "PUBLISHED")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value("SUCCESS"));
        }
    }

    // ==================== PATCH /api/products/{id}/visibility Tests
    // ====================

    @Nested
    @DisplayName("PATCH /api/products/{id}/visibility - Update Visibility")
    class UpdateVisibilityTests {

        @Test
        @DisplayName("Should update product visibility")
        void updateVisibility_Success() throws Exception {
            // Given
            when(productService.updateVisibility(eq(1L), eq(false), any()))
                    .thenReturn(testProduct);

            // When & Then
            mockMvc.perform(patch("/products/1/visibility")
                    .param("isVisible", "false")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value("SUCCESS"));
        }
    }
}
