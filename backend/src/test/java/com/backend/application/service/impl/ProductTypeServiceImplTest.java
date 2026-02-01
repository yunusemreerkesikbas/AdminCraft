package com.backend.application.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.backend.application.service.ProductTypeServiceImpl;
import com.backend.domain.entity.ProductAttributeDefinition;
import com.backend.domain.entity.ProductType;
import com.backend.domain.enums.ProductFieldType;
import com.backend.domain.exception.BusinessRuleViolationException;
import com.backend.domain.repository.ProductAttributeDefinitionRepository;
import com.backend.domain.repository.ProductRepository;
import com.backend.domain.repository.ProductTypeRepository;
import com.backend.testutil.BaseServiceTest;
import com.backend.testutil.builders.ProductAttributeDefinitionTestDataBuilder;
import com.backend.testutil.builders.ProductTypeTestDataBuilder;

/**
 * Unit tests for ProductTypeServiceImpl.
 * Tests all CRUD operations, validation logic, and business rules.
 * Special attention to BusinessRuleViolationException on delete.
 */
class ProductTypeServiceImplTest extends BaseServiceTest {

    @Mock
    private ProductTypeRepository productTypeRepository;

    @Mock
    private ProductAttributeDefinitionRepository attributeDefinitionRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductTypeServiceImpl productTypeService;

    private ProductType testProductType;
    private ProductAttributeDefinition testAttributeDefinition;

    @BeforeEach
    void setUp() {
        ProductTypeTestDataBuilder.resetIdCounter();
        ProductAttributeDefinitionTestDataBuilder.resetIdCounter();

        testProductType = ProductTypeTestDataBuilder.aProductType()
                .withId(1L)
                .withCode("general_product")
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
    }

    // ==================== create() Tests ====================

    @Nested
    @DisplayName("create() Tests")
    class CreateTests {

        @Test
        @DisplayName("Should create product type successfully")
        void create_Success() {
            // Given
            when(productTypeRepository.existsByCode("new_type")).thenReturn(false);
            when(productTypeRepository.save(any(ProductType.class))).thenAnswer(inv -> {
                ProductType pt = inv.getArgument(0);
                pt.setId(10L);
                return pt;
            });

            // When
            ProductType result = productTypeService.create("new_type", "New Type", "category", TEST_USER_ID);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(10L);
            verify(productTypeRepository).existsByCode("new_type");
            verify(productTypeRepository).save(argThat(pt -> "new_type".equals(pt.getCode()) &&
                    "New Type".equals(pt.getName()) &&
                    "category".equals(pt.getCategory())));
        }

        @Test
        @DisplayName("Should throw exception when code is duplicate")
        void create_ThrowsException_WhenCodeDuplicate() {
            // Given
            when(productTypeRepository.existsByCode("existing_code")).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> productTypeService.create("existing_code", "Name", "category", TEST_USER_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("already exists");
        }

        @Test
        @DisplayName("Should create product type with category")
        void create_WithCategory_Success() {
            // Given
            when(productTypeRepository.existsByCode("categorized")).thenReturn(false);
            when(productTypeRepository.save(any(ProductType.class))).thenAnswer(inv -> {
                ProductType pt = inv.getArgument(0);
                pt.setId(11L);
                return pt;
            });

            // When
            ProductType result = productTypeService.create("categorized", "Categorized Type", "special_category",
                    TEST_USER_ID);

            // Then
            assertThat(result).isNotNull();
            verify(productTypeRepository).save(argThat(pt -> "special_category".equals(pt.getCategory())));
        }

        @Test
        @DisplayName("Should throw exception when tenant context is not active")
        void create_ThrowsException_WhenTenantNotActive() {
            // Given
            simulateInactiveTenantContext();

            // When & Then
            assertThatThrownBy(() -> productTypeService.create("code", "Name", "category", TEST_USER_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Tenant context");
        }
    }

    // ==================== update() Tests ====================

    @Nested
    @DisplayName("update() Tests")
    class UpdateTests {

        @Test
        @DisplayName("Should update product type successfully")
        void update_Success() {
            // Given
            when(productTypeRepository.findById(1L)).thenReturn(Optional.of(testProductType));
            when(productTypeRepository.save(any(ProductType.class))).thenReturn(testProductType);

            // When
            ProductType result = productTypeService.update(1L, "Updated Name", "updated_category", TEST_USER_ID);

            // Then
            assertThat(result).isNotNull();
            verify(productTypeRepository).save(argThat(pt -> "Updated Name".equals(pt.getName()) &&
                    "updated_category".equals(pt.getCategory())));
        }

        @Test
        @DisplayName("Should throw exception when product type not found")
        void update_ThrowsException_WhenNotFound() {
            // Given
            when(productTypeRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> productTypeService.update(999L, "Name", "category", TEST_USER_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Product type not found");
        }

        @Test
        @DisplayName("Should throw exception when tenant context is not active")
        void update_ThrowsException_WhenTenantNotActive() {
            // Given
            simulateInactiveTenantContext();

            // When & Then
            assertThatThrownBy(() -> productTypeService.update(1L, "Name", "category", TEST_USER_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Tenant context");
        }
    }

    // ==================== delete() Tests - CRITICAL BUSINESS RULE
    // ====================

    @Nested
    @DisplayName("delete() Tests - BusinessRuleViolationException")
    class DeleteTests {

        @Test
        @DisplayName("Should delete product type successfully when no products exist")
        void delete_Success_WhenNoProducts() {
            // Given
            when(productTypeRepository.findById(1L)).thenReturn(Optional.of(testProductType));
            when(productRepository.countByProductTypeId(1L)).thenReturn(0L);
            doNothing().when(productTypeRepository).delete(testProductType);

            // When
            productTypeService.delete(1L);

            // Then
            verify(productTypeRepository).findById(1L);
            verify(productRepository).countByProductTypeId(1L);
            verify(productTypeRepository).delete(testProductType);
        }

        @Test
        @DisplayName("Should throw BusinessRuleViolationException when products exist - NOT IllegalArgumentException")
        void delete_ThrowsBusinessRuleException_WhenHasProducts() {
            // Given
            when(productTypeRepository.findById(1L)).thenReturn(Optional.of(testProductType));
            when(productRepository.countByProductTypeId(1L)).thenReturn(5L);

            // When & Then
            assertThatThrownBy(() -> productTypeService.delete(1L))
                    .isInstanceOf(BusinessRuleViolationException.class)
                    .isNotInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Cannot delete");
        }

        @Test
        @DisplayName("Should include product count in error message")
        void delete_ErrorMessageIncludesProductCount() {
            // Given
            when(productTypeRepository.findById(1L)).thenReturn(Optional.of(testProductType));
            when(productRepository.countByProductTypeId(1L)).thenReturn(10L);

            // When & Then
            assertThatThrownBy(() -> productTypeService.delete(1L))
                    .isInstanceOf(BusinessRuleViolationException.class)
                    .hasMessageContaining("10");
        }

        @Test
        @DisplayName("Should fail with multiple products")
        void delete_FailsWithMultipleProducts() {
            // Given
            when(productTypeRepository.findById(1L)).thenReturn(Optional.of(testProductType));
            when(productRepository.countByProductTypeId(1L)).thenReturn(100L);

            // When & Then
            assertThatThrownBy(() -> productTypeService.delete(1L))
                    .isInstanceOf(BusinessRuleViolationException.class);

            verify(productTypeRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when product type not found")
        void delete_ThrowsException_WhenTypeNotFound() {
            // Given
            when(productTypeRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> productTypeService.delete(999L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Product type not found");
        }

        @Test
        @DisplayName("Should throw exception when tenant context is not active")
        void delete_ThrowsException_WhenTenantNotActive() {
            // Given
            simulateInactiveTenantContext();

            // When & Then
            assertThatThrownBy(() -> productTypeService.delete(1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Tenant context");
        }
    }

    // ==================== addAttribute() Tests ====================

    @Nested
    @DisplayName("addAttribute() Tests")
    class AddAttributeTests {

        @Test
        @DisplayName("Should add attribute successfully")
        void addAttribute_Success() {
            // Given
            when(productTypeRepository.findById(1L)).thenReturn(Optional.of(testProductType));
            when(attributeDefinitionRepository.existsByProductTypeIdAndCode(eq(1L), anyString())).thenReturn(false);
            when(attributeDefinitionRepository.save(any(ProductAttributeDefinition.class))).thenAnswer(inv -> {
                ProductAttributeDefinition def = inv.getArgument(0);
                def.setId(100L);
                return def;
            });

            // When
            ProductAttributeDefinition result = productTypeService.addAttribute(
                    1L, "New Attribute", ProductFieldType.TEXT);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(100L);
            verify(attributeDefinitionRepository).save(argThat(def -> "New Attribute".equals(def.getName()) &&
                    def.getFieldType() == ProductFieldType.TEXT));
        }

        @Test
        @DisplayName("Should generate code from name")
        void addAttribute_GeneratesCodeFromName() {
            // Given
            when(productTypeRepository.findById(1L)).thenReturn(Optional.of(testProductType));
            when(attributeDefinitionRepository.existsByProductTypeIdAndCode(eq(1L), anyString())).thenReturn(false);
            when(attributeDefinitionRepository.save(any(ProductAttributeDefinition.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            // When
            productTypeService.addAttribute(1L, "Auto Sort", ProductFieldType.TEXT);

            // Then
            verify(attributeDefinitionRepository).save(argThat(def -> "Auto Sort".equals(def.getName()) &&
                    def.getCode() != null &&
                    !def.getCode().isEmpty()));
        }

        @Test
        @DisplayName("Should create attribute with correct field type")
        void addAttribute_WithFieldType() {
            // Given
            when(productTypeRepository.findById(1L)).thenReturn(Optional.of(testProductType));
            when(attributeDefinitionRepository.existsByProductTypeIdAndCode(eq(1L), anyString())).thenReturn(false);
            when(attributeDefinitionRepository.save(any(ProductAttributeDefinition.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            // When
            productTypeService.addAttribute(1L, "Explicit Sort", ProductFieldType.NUMBER);

            // Then
            verify(attributeDefinitionRepository).save(argThat(def -> "Explicit Sort".equals(def.getName()) &&
                    def.getFieldType() == ProductFieldType.NUMBER));
        }

        @Test
        @DisplayName("Should throw exception when generated code is duplicate")
        void addAttribute_ThrowsException_WhenCodeDuplicate() {
            // Given
            when(productTypeRepository.findById(1L)).thenReturn(Optional.of(testProductType));
            when(attributeDefinitionRepository.existsByProductTypeIdAndCode(eq(1L), anyString())).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> productTypeService.addAttribute(
                    1L, "Name", ProductFieldType.TEXT))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw exception when product type not found")
        void addAttribute_ThrowsException_WhenTypeNotFound() {
            // Given
            when(productTypeRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> productTypeService.addAttribute(
                    999L, "Name", ProductFieldType.TEXT))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Product type not found");
        }
    }

    // ==================== updateAttribute() Tests ====================

    @Nested
    @DisplayName("updateAttribute() Tests")
    class UpdateAttributeTests {

        @Test
        @DisplayName("Should update attribute successfully")
        void updateAttribute_Success() {
            // Given
            testAttributeDefinition.setProductType(testProductType);
            when(attributeDefinitionRepository.findById(1L)).thenReturn(Optional.of(testAttributeDefinition));
            when(attributeDefinitionRepository.save(any(ProductAttributeDefinition.class)))
                    .thenReturn(testAttributeDefinition);

            // When
            ProductAttributeDefinition result = productTypeService.updateAttribute(
                    1L, 1L, "Updated Name", ProductFieldType.NUMBER);

            // Then
            assertThat(result).isNotNull();
            verify(attributeDefinitionRepository).save(argThat(def -> "Updated Name".equals(def.getName()) &&
                    def.getFieldType() == ProductFieldType.NUMBER));
        }

        @Test
        @DisplayName("Should throw exception when attribute not found")
        void updateAttribute_ThrowsException_WhenNotFound() {
            // Given
            when(attributeDefinitionRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> productTypeService.updateAttribute(
                    1L, 999L, "Name", ProductFieldType.TEXT))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Attribute not found");
        }

        @Test
        @DisplayName("Should throw exception when attribute belongs to wrong product type")
        void updateAttribute_ThrowsException_WhenWrongProductType() {
            // Given
            ProductType otherProductType = ProductTypeTestDataBuilder.aProductType()
                    .withId(999L)
                    .withCode("other_type")
                    .build();
            testAttributeDefinition.setProductType(otherProductType);

            when(attributeDefinitionRepository.findById(1L)).thenReturn(Optional.of(testAttributeDefinition));

            // When & Then
            assertThatThrownBy(() -> productTypeService.updateAttribute(
                    1L, 1L, "Name", ProductFieldType.TEXT))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("does not belong");
        }
    }

    // ==================== deleteAttribute() Tests ====================

    @Nested
    @DisplayName("deleteAttribute() Tests")
    class DeleteAttributeTests {

        @Test
        @DisplayName("Should delete attribute successfully")
        void deleteAttribute_Success() {
            // Given
            testAttributeDefinition.setProductType(testProductType);
            when(attributeDefinitionRepository.findById(1L)).thenReturn(Optional.of(testAttributeDefinition));
            doNothing().when(attributeDefinitionRepository).delete(testAttributeDefinition);

            // When
            productTypeService.deleteAttribute(1L, 1L);

            // Then
            verify(attributeDefinitionRepository).findById(1L);
            verify(attributeDefinitionRepository).delete(testAttributeDefinition);
        }

        @Test
        @DisplayName("Should throw exception when attribute not found")
        void deleteAttribute_ThrowsException_WhenNotFound() {
            // Given
            when(attributeDefinitionRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> productTypeService.deleteAttribute(1L, 999L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Attribute not found");
        }

        @Test
        @DisplayName("Should throw exception when attribute belongs to wrong product type")
        void deleteAttribute_ThrowsException_WhenWrongProductType() {
            // Given
            ProductType otherProductType = ProductTypeTestDataBuilder.aProductType()
                    .withId(999L)
                    .withCode("other_type")
                    .build();
            testAttributeDefinition.setProductType(otherProductType);

            when(attributeDefinitionRepository.findById(1L)).thenReturn(Optional.of(testAttributeDefinition));

            // When & Then
            assertThatThrownBy(() -> productTypeService.deleteAttribute(1L, 1L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("does not belong");

            verify(attributeDefinitionRepository, never()).delete(any());
        }
    }

    // ==================== Query Methods Tests ====================

    @Nested
    @DisplayName("Query Methods Tests")
    class QueryMethodsTests {

        @Test
        @DisplayName("findById should return empty when not found")
        void findById_ReturnsEmpty_WhenNotFound() {
            // Given
            when(productTypeRepository.findById(999L)).thenReturn(Optional.empty());

            // When
            Optional<ProductType> result = productTypeService.findById(999L);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("findById should return product type when found")
        void findById_ReturnsProductType_WhenFound() {
            // Given
            when(productTypeRepository.findById(1L)).thenReturn(Optional.of(testProductType));

            // When
            Optional<ProductType> result = productTypeService.findById(1L);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("findByIdWithAttributes should include attribute definitions")
        void findByIdWithAttributes_IncludesAttributes() {
            // Given
            testProductType.getAttributeDefinitions().add(testAttributeDefinition);
            when(productTypeRepository.findByIdWithAttributes(1L)).thenReturn(Optional.of(testProductType));

            // When
            Optional<ProductType> result = productTypeService.findByIdWithAttributes(1L);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getAttributeDefinitions()).hasSize(1);
        }

        @Test
        @DisplayName("findByCode should return product type")
        void findByCode_ReturnsProductType() {
            // Given
            when(productTypeRepository.findByCode("general_product")).thenReturn(Optional.of(testProductType));

            // When
            Optional<ProductType> result = productTypeService.findByCode("general_product");

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getCode()).isEqualTo("general_product");
        }

        @Test
        @DisplayName("findByUid should return product type")
        void findByUid_ReturnsProductType() {
            // Given
            when(productTypeRepository.findByUid("product_type_1")).thenReturn(Optional.of(testProductType));

            // When
            Optional<ProductType> result = productTypeService.findByUid("product_type_1");

            // Then
            assertThat(result).isPresent();
        }

        @Test
        @DisplayName("findAll should return all product types")
        void findAll_ReturnsAllProductTypes() {
            // Given
            ProductType anotherType = ProductTypeTestDataBuilder.aProductType()
                    .withId(2L)
                    .withCode("another_type")
                    .build();
            List<ProductType> allTypes = List.of(testProductType, anotherType);
            when(productTypeRepository.findAll()).thenReturn(allTypes);

            // When
            List<ProductType> result = productTypeService.findAll();

            // Then
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("findAllPaged should return paginated results")
        void findAllPaged_ReturnsPaginatedResults() {
            // Given
            Page<ProductType> typePage = new PageImpl<>(List.of(testProductType), PageRequest.of(0, 10), 1);
            when(productTypeRepository.findAllPaged(any(Pageable.class))).thenReturn(typePage);

            // When
            Page<ProductType> result = productTypeService.findAllPaged(PageRequest.of(0, 10));

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("search should filter by query")
        void search_FiltersWithQuery() {
            // Given
            Page<ProductType> typePage = new PageImpl<>(List.of(testProductType));
            when(productTypeRepository.searchPaged(eq("general"), any(Pageable.class))).thenReturn(typePage);

            // When
            Page<ProductType> result = productTypeService.search("general", PageRequest.of(0, 10));

            // Then
            assertThat(result).isNotNull();
            verify(productTypeRepository).searchPaged("general", PageRequest.of(0, 10));
        }

        @Test
        @DisplayName("findByCategory should filter by category")
        void findByCategory_FiltersByCategory() {
            // Given
            List<ProductType> electronicTypes = List.of(testProductType);
            when(productTypeRepository.findByCategory("electronics")).thenReturn(electronicTypes);

            // When
            List<ProductType> result = productTypeService.findByCategory("electronics");

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getCategory()).isEqualTo("electronics");
        }

        @Test
        @DisplayName("getAttributes should return ordered attributes")
        void getAttributes_ReturnsOrderedAttributes() {
            // Given
            ProductAttributeDefinition attr1 = ProductAttributeDefinitionTestDataBuilder.anAttributeDefinition()
                    .withId(1L).withCode("attr1").build();
            ProductAttributeDefinition attr2 = ProductAttributeDefinitionTestDataBuilder.anAttributeDefinition()
                    .withId(2L).withCode("attr2").build();

            when(attributeDefinitionRepository.findByProductTypeId(1L))
                    .thenReturn(List.of(attr1, attr2));

            // When
            List<ProductAttributeDefinition> result = productTypeService.getAttributes(1L);

            // Then
            assertThat(result).hasSize(2);
        }
    }
}
