package com.backend.application.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.backend.application.dto.request.ProductI18nDto;
import com.backend.application.dto.request.ProductVariantDto;
import com.backend.application.service.ProductServiceImpl;
import com.backend.domain.entity.Category;
import com.backend.domain.entity.Media;
import com.backend.domain.entity.Product;
import com.backend.domain.entity.ProductVariant;
import com.backend.domain.entity.ProductVariantOption;
import com.backend.domain.entity.ProductVariantOptionValue;
import com.backend.domain.entity.ProductType;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.ProductStatus;
import com.backend.domain.enums.ProductVariantOptionDisplayType;
import com.backend.domain.repository.CategoryRepository;
import com.backend.domain.repository.MediaRepository;
import com.backend.domain.repository.ProductAttributeDefinitionRepository;
import com.backend.domain.repository.ProductAttributeRepository;
import com.backend.domain.repository.ProductCategoryLinkRepository;
import com.backend.domain.repository.ProductI18nRepository;
import com.backend.domain.repository.ProductMediaRepository;
import com.backend.domain.repository.ProductRepository;
import com.backend.domain.repository.ProductTypeRepository;
import com.backend.domain.repository.ProductVariantOptionValueRepository;
import com.backend.domain.repository.ProductVariantRepository;
import com.backend.domain.repository.ResponsiveMediaSetRepository;
import com.backend.presentation.dto.request.ResponsiveMediaRequest;
import com.backend.testutil.BaseServiceTest;
import com.backend.testutil.builders.CategoryTestDataBuilder;
import com.backend.testutil.builders.ProductTestDataBuilder;
import com.backend.testutil.builders.ProductTypeTestDataBuilder;

/**
 * Unit tests for ProductServiceImpl.
 * Tests all CRUD operations, validation logic, and business rules.
 */
@MockitoSettings(strictness = Strictness.LENIENT)
class ProductServiceImplTest extends BaseServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductI18nRepository productI18nRepository;

    @Mock
    private ProductAttributeRepository productAttributeRepository;

    @Mock
    private ProductAttributeDefinitionRepository attributeDefinitionRepository;

    @Mock
    private ProductCategoryLinkRepository productCategoryLinkRepository;

    @Mock
    private ProductMediaRepository productMediaRepository;

    @Mock
    private ProductTypeRepository productTypeRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ResponsiveMediaSetRepository responsiveMediaSetRepository;

    @Mock
    private MediaRepository mediaRepository;

    @Mock
    private com.backend.domain.repository.ProductFieldValueRepository productFieldValueRepository;

    @Mock
    private com.backend.application.service.ProductFieldService productFieldService;

    @Mock
    private ProductVariantRepository productVariantRepository;

    @Mock
    private ProductVariantOptionValueRepository productVariantOptionValueRepository;

    @Mock
    private com.backend.application.service.SiteActivityPublisher activityPublisher;

    @Mock
    private com.backend.shared.common.SecurityHelper securityHelper;

    @InjectMocks
    private ProductServiceImpl productService;

    private ProductType testProductType;
    private Product testProduct;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        ProductTypeTestDataBuilder.resetIdCounter();
        ProductTestDataBuilder.resetIdCounter();
        CategoryTestDataBuilder.resetIdCounter();

        testProductType = ProductTypeTestDataBuilder.aProductType()
                .withId(1L)
                .withCode("general_product")
                .withName("General Product")
                .build();

        testProduct = ProductTestDataBuilder.aProduct()
                .withId(1L)
                .withProductType(testProductType)
                .withSku("SKU-001")
                .build();
        ProductVariant publishableVariant = new ProductVariant();
        publishableVariant.setId(1L);
        publishableVariant.setProduct(testProduct);
        publishableVariant.setSku("SKU-001");
        publishableVariant.setPrice(BigDecimal.valueOf(100));
        publishableVariant.setVatRate(BigDecimal.valueOf(20));
        publishableVariant.setStockQuantity(0);
        publishableVariant.setActive(true);
        testProduct.getVariants().add(publishableVariant);

        testCategory = CategoryTestDataBuilder.aCategory()
                .withId(1L)
                .withCode("electronics")
                .build();

        when(productVariantRepository.existsBySku(anyString())).thenReturn(false);
        when(productVariantRepository.existsBySkuAndProductIdNot(anyString(), anyLong())).thenReturn(false);
        when(productVariantRepository.save(any(ProductVariant.class))).thenAnswer(inv -> inv.getArgument(0));
        when(productVariantRepository.findByProductIdOrderByIdAsc(anyLong())).thenReturn(List.of(publishableVariant));
    }

    // ==================== createComposite() Tests ====================

    @Nested
    @DisplayName("createComposite() Tests")
    class CreateCompositeTests {

        @Test
        @DisplayName("Should create product with all fields successfully")
        void createComposite_WithAllFields_Success() {
            // Given
            Map<Language, ProductI18nDto> translations = new HashMap<>();
            translations.put(Language.TR,
                    new ProductI18nDto("Türkçe Ürün", "Kısa açıklama", "<p>Açıklama</p>", "SEO Title", "SEO Desc"));
            translations.put(Language.EN, new ProductI18nDto("English Product", "Short desc", "<p>Description</p>",
                    "SEO Title EN", "SEO Desc EN"));

            Map<String, Object> attributes = new HashMap<>();
            attributes.put("color", "red");

            List<Long> categoryIds = List.of(1L);
            List<ResponsiveMediaRequest> gallery = List.of(
                    new ResponsiveMediaRequest(10L, null),
                    new ResponsiveMediaRequest(11L, null));

            when(productTypeRepository.findById(1L)).thenReturn(Optional.of(testProductType));
            when(productRepository.existsBySku("SKU-NEW")).thenReturn(false);
            when(productRepository.save(any(Product.class))).thenAnswer(inv -> {
                Product p = inv.getArgument(0);
                p.setId(1L);
                return p;
            });
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
            when(mediaRepository.findById(anyLong())).thenReturn(Optional.of(new Media()));
            when(attributeDefinitionRepository.findByProductTypeId(1L)).thenReturn(List.of());
            when(productRepository.findByIdComposite(1L)).thenReturn(Optional.of(testProduct));

            // When
            Product result = productService.createComposite(
                    1L, "SKU-NEW", BigDecimal.valueOf(199.99),
                    ProductStatus.DRAFT, true, null,
                    translations, attributes, categoryIds, 1L, gallery, null, TEST_USER_ID);

            // Then
            assertThat(result).isNotNull();
            verify(productTypeRepository).findById(1L);
            verify(productRepository).existsBySku("SKU-NEW");
            verify(productRepository).save(any(Product.class));
        }

        @Test
        @DisplayName("Should create product with minimal data (one translation)")
        void createComposite_WithMinimalData_Success() {
            // Given
            Map<Language, ProductI18nDto> translations = new HashMap<>();
            translations.put(Language.TR, new ProductI18nDto("Minimal Ürün"));

            when(productTypeRepository.findById(1L)).thenReturn(Optional.of(testProductType));
            when(productRepository.existsBySku("SKU-MIN")).thenReturn(false);
            when(productRepository.save(any(Product.class))).thenAnswer(inv -> {
                Product p = inv.getArgument(0);
                p.setId(2L);
                return p;
            });
            when(attributeDefinitionRepository.findByProductTypeId(1L)).thenReturn(List.of());
            when(productRepository.findByIdComposite(2L)).thenReturn(Optional.of(testProduct));

            // When
            Product result = productService.createComposite(
                    1L, "SKU-MIN", null,
                    null, null, null,
                    translations, null, null, null, null, null, TEST_USER_ID);

            // Then
            assertThat(result).isNotNull();
            verify(productRepository).save(argThat(product -> product.getStatus() == ProductStatus.DRAFT &&
                    product.getIsVisible() == true));
        }

        @Test
        @DisplayName("Should set default status to DRAFT when null")
        void createComposite_SetsDefaultStatus_WhenNull() {
            // Given
            Map<Language, ProductI18nDto> translations = Map.of(Language.TR, new ProductI18nDto("Test"));

            when(productTypeRepository.findById(1L)).thenReturn(Optional.of(testProductType));
            when(productRepository.existsBySku(anyString())).thenReturn(false);
            when(productRepository.save(any(Product.class))).thenAnswer(inv -> {
                Product p = inv.getArgument(0);
                p.setId(1L);
                return p;
            });
            when(attributeDefinitionRepository.findByProductTypeId(1L)).thenReturn(List.of());
            when(productRepository.findByIdComposite(1L)).thenReturn(Optional.of(testProduct));

            // When
            productService.createComposite(1L, "SKU-TEST", null,
                    null, null, null, translations, null, null, null, null, null, TEST_USER_ID);

            // Then
            verify(productRepository).save(argThat(product -> product.getStatus() == ProductStatus.DRAFT));
        }

        @Test
        @DisplayName("Should set default visibility to true when null")
        void createComposite_SetsDefaultVisibility_WhenNull() {
            // Given
            Map<Language, ProductI18nDto> translations = Map.of(Language.TR, new ProductI18nDto("Test"));

            when(productTypeRepository.findById(1L)).thenReturn(Optional.of(testProductType));
            when(productRepository.existsBySku(anyString())).thenReturn(false);
            when(productRepository.save(any(Product.class))).thenAnswer(inv -> {
                Product p = inv.getArgument(0);
                p.setId(1L);
                return p;
            });
            when(attributeDefinitionRepository.findByProductTypeId(1L)).thenReturn(List.of());
            when(productRepository.findByIdComposite(1L)).thenReturn(Optional.of(testProduct));

            // When
            productService.createComposite(1L, "SKU-TEST", null,
                    null, null, null, translations, null, null, null, null, null, TEST_USER_ID);

            // Then
            verify(productRepository).save(argThat(product -> product.getIsVisible() == true));
        }

        @Test
        @DisplayName("Should create product with multiple translations")
        void createComposite_WithMultipleTranslations_Success() {
            // Given
            Map<Language, ProductI18nDto> translations = new HashMap<>();
            translations.put(Language.TR, new ProductI18nDto("Türkçe"));
            translations.put(Language.EN, new ProductI18nDto("English"));
            translations.put(Language.ES, new ProductI18nDto("Español"));

            when(productTypeRepository.findById(1L)).thenReturn(Optional.of(testProductType));
            when(productRepository.existsBySku(anyString())).thenReturn(false);
            when(productRepository.save(any(Product.class))).thenAnswer(inv -> {
                Product p = inv.getArgument(0);
                p.setId(1L);
                return p;
            });
            when(attributeDefinitionRepository.findByProductTypeId(1L)).thenReturn(List.of());
            when(productRepository.findByIdComposite(1L)).thenReturn(Optional.of(testProduct));

            // When
            Product result = productService.createComposite(1L, "SKU-MULTI", null,
                    null, null, null, translations, null, null, null, null, null, TEST_USER_ID);

            // Then
            assertThat(result).isNotNull();
            verify(productI18nRepository).saveAll(anyList());
        }

        @Test
        @DisplayName("Should throw exception when ProductType not found")
        void createComposite_ThrowsException_WhenProductTypeNotFound() {
            // Given
            Map<Language, ProductI18nDto> translations = Map.of(Language.TR, new ProductI18nDto("Test"));
            when(productTypeRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> productService.createComposite(
                    999L, "SKU-001", null,
                    null, null, null, translations, null, null, null, null, null, TEST_USER_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Product type not found");
        }

        @Test
        @DisplayName("Should throw exception when SKU is duplicate")
        void createComposite_ThrowsException_WhenSkuDuplicate() {
            // Given
            Map<Language, ProductI18nDto> translations = Map.of(Language.TR, new ProductI18nDto("Test"));
            when(productTypeRepository.findById(1L)).thenReturn(Optional.of(testProductType));
            when(productRepository.existsBySku("EXISTING-SKU")).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> productService.createComposite(
                    1L, "EXISTING-SKU", null,
                    null, null, null, translations, null, null, null, null, null, TEST_USER_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("SKU already exists");
        }

        @Test
        @DisplayName("Should throw exception when ResponsiveMediaSet not found")
        void createComposite_ThrowsException_WhenResponsiveMediaNotFound() {
            // Given
            Map<Language, ProductI18nDto> translations = Map.of(Language.TR, new ProductI18nDto("Test"));
            when(productTypeRepository.findById(1L)).thenReturn(Optional.of(testProductType));
            when(productRepository.existsBySku(anyString())).thenReturn(false);
            when(responsiveMediaSetRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> productService.createComposite(
                    1L, "SKU-001", null,
                    null, null, 999L, translations, null, null, null, null, null, TEST_USER_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Responsive media set not found");
        }

        @Test
        @DisplayName("Should throw exception when translations map is empty")
        void createComposite_ThrowsException_WhenTranslationsEmpty() {
            // Given
            Map<Language, ProductI18nDto> translations = new HashMap<>();
            when(productTypeRepository.findById(1L)).thenReturn(Optional.of(testProductType));
            when(productRepository.existsBySku(anyString())).thenReturn(false);
            when(productRepository.save(any(Product.class))).thenAnswer(inv -> {
                Product p = inv.getArgument(0);
                p.setId(1L);
                return p;
            });

            // When & Then
            assertThatThrownBy(() -> productService.createComposite(
                    1L, "SKU-001", null,
                    null, null, null, translations, null, null, null, null, null, TEST_USER_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("At least one translation");
        }

        @Test
        @DisplayName("Should throw exception when no translation has a name")
        void createComposite_ThrowsException_WhenNoTranslationHasName() {
            // Given
            Map<Language, ProductI18nDto> translations = new HashMap<>();
            translations.put(Language.TR, new ProductI18nDto(null, "desc", null, null, null));
            translations.put(Language.EN, new ProductI18nDto("", "desc", null, null, null));

            when(productTypeRepository.findById(1L)).thenReturn(Optional.of(testProductType));
            when(productRepository.existsBySku(anyString())).thenReturn(false);
            when(productRepository.save(any(Product.class))).thenAnswer(inv -> {
                Product p = inv.getArgument(0);
                p.setId(1L);
                return p;
            });

            // When & Then
            assertThatThrownBy(() -> productService.createComposite(
                    1L, "SKU-001", null,
                    null, null, null, translations, null, null, null, null, null, TEST_USER_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("name");
        }

        @Test
        @DisplayName("Should throw exception when category not found")
        void createComposite_ThrowsException_WhenCategoryNotFound() {
            // Given
            Map<Language, ProductI18nDto> translations = Map.of(Language.TR, new ProductI18nDto("Test"));
            List<Long> categoryIds = List.of(999L);

            when(productTypeRepository.findById(1L)).thenReturn(Optional.of(testProductType));
            when(productRepository.existsBySku(anyString())).thenReturn(false);
            when(productRepository.save(any(Product.class))).thenAnswer(inv -> {
                Product p = inv.getArgument(0);
                p.setId(1L);
                return p;
            });
            when(attributeDefinitionRepository.findByProductTypeId(1L)).thenReturn(List.of());
            when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> productService.createComposite(
                    1L, "SKU-001", null,
                    null, null, null, translations, null, categoryIds, null, null, null, TEST_USER_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Category not found");
        }

        @Test
        @DisplayName("Should throw exception when gallery media not found")
        void createComposite_ThrowsException_WhenMediaNotFound() {
            // Given
            Map<Language, ProductI18nDto> translations = Map.of(Language.TR, new ProductI18nDto("Test"));
            List<ResponsiveMediaRequest> gallery = List.of(
                    new ResponsiveMediaRequest(999L, null));

            when(productTypeRepository.findById(1L)).thenReturn(Optional.of(testProductType));
            when(productRepository.existsBySku(anyString())).thenReturn(false);
            when(productRepository.save(any(Product.class))).thenAnswer(inv -> {
                Product p = inv.getArgument(0);
                p.setId(1L);
                return p;
            });
            when(attributeDefinitionRepository.findByProductTypeId(1L)).thenReturn(List.of());
            when(mediaRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> productService.createComposite(
                    1L, "SKU-001", null,
                    null, null, null, translations, null, null, null, gallery, null, TEST_USER_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("media not found");
        }

        @Test
        @DisplayName("Should throw exception when tenant context is not active")
        void createComposite_ThrowsException_WhenTenantNotActive() {
            // Given
            simulateInactiveTenantContext();
            Map<Language, ProductI18nDto> translations = Map.of(Language.TR, new ProductI18nDto("Test"));

            // When & Then
            assertThatThrownBy(() -> productService.createComposite(
                    1L, "SKU-001", null,
                    null, null, null, translations, null, null, null, null, null, TEST_USER_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Tenant context");
        }
    }

    // ==================== updateComposite() Tests ====================

    @Nested
    @DisplayName("updateComposite() Tests")
    class UpdateCompositeTests {

        @Test
        @DisplayName("Should update product with all fields successfully")
        void updateComposite_AllFields_Success() {
            // Given
            Map<Language, ProductI18nDto> translations = Map.of(
                    Language.TR, new ProductI18nDto("Güncel Ürün"));

            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);
            when(productRepository.findByIdComposite(1L)).thenReturn(Optional.of(testProduct));
            when(productI18nRepository.findByProductId(1L)).thenReturn(List.of());
            when(productCategoryLinkRepository.findByProductIdWithCategories(1L)).thenReturn(List.of());
            when(productMediaRepository.findByProductIdWithMedia(1L)).thenReturn(List.of());

            // When
            Product result = productService.updateComposite(1L, BigDecimal.valueOf(299.99),
                    ProductStatus.PUBLISHED, false, null,
                    translations, null, null, null, null, null, TEST_USER_ID);

            // Then
            assertThat(result).isNotNull();
            verify(productRepository)
                    .save(argThat(product -> product.getBasePrice().compareTo(BigDecimal.valueOf(299.99)) == 0 &&
                            product.getStatus() == ProductStatus.PUBLISHED &&
                            product.getIsVisible() == false));
        }

        @Test
        @DisplayName("Should update only partial fields")
        void updateComposite_PartialFields_Success() {
            // Given
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);
            when(productRepository.findByIdComposite(1L)).thenReturn(Optional.of(testProduct));
            when(productI18nRepository.findByProductId(1L)).thenReturn(List.of());
            when(productCategoryLinkRepository.findByProductIdWithCategories(1L)).thenReturn(List.of());
            when(productMediaRepository.findByProductIdWithMedia(1L)).thenReturn(List.of());

            // When - Only update basePrice, leave others null
            Product result = productService.updateComposite(1L, BigDecimal.valueOf(150.00),
                    null, null, null, null, null, null, null, null, null, TEST_USER_ID);

            // Then
            assertThat(result).isNotNull();
            verify(productRepository)
                    .save(argThat(product -> product.getBasePrice().compareTo(BigDecimal.valueOf(150.00)) == 0));
        }

        @Test
        @DisplayName("Should throw exception when product not found")
        void updateComposite_ThrowsException_WhenProductNotFound() {
            // Given
            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> productService.updateComposite(999L, null,
                    null, null, null, null, null, null, null, null, null, TEST_USER_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Product not found");
        }

        @Test
        @DisplayName("Should throw exception when ResponsiveMediaSet not found during update")
        void updateComposite_ThrowsException_WhenResponsiveMediaNotFound() {
            // Given
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            when(responsiveMediaSetRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> productService.updateComposite(1L, null,
                    null, null, 999L, null, null, null, null, null, null, TEST_USER_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Responsive media set not found");
        }

        @Test
        @DisplayName("Should throw exception when tenant context is not active during update")
        void updateComposite_ThrowsException_WhenTenantNotActive() {
            // Given
            simulateInactiveTenantContext();

            // When & Then
            assertThatThrownBy(() -> productService.updateComposite(1L, null,
                    null, null, null, null, null, null, null, null, null, TEST_USER_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Tenant context");
        }
    }

    // ==================== delete() Tests ====================

    @Nested
    @DisplayName("Variant Tests")
    class VariantTests {

        @Test
        @DisplayName("Should save provided variants and sync base price from active minimum")
        void createComposite_WithVariants_SavesVariantsAndSyncsBasePrice() {
            Map<Language, ProductI18nDto> translations = Map.of(Language.TR, new ProductI18nDto("Variant Product"));
            List<ProductVariantDto> variants = List.of(
                    new ProductVariantDto("SKU-RED", BigDecimal.valueOf(200), null, BigDecimal.valueOf(20), 5, true,
                            null, List.of()),
                    new ProductVariantDto("SKU-BLUE", BigDecimal.valueOf(150), null, BigDecimal.valueOf(20), 0, true,
                            null, List.of()));

            when(productTypeRepository.findById(1L)).thenReturn(Optional.of(testProductType));
            when(productRepository.existsBySku("SKU-PARENT")).thenReturn(false);
            when(productRepository.save(any(Product.class))).thenAnswer(inv -> {
                Product p = inv.getArgument(0);
                p.setId(10L);
                return p;
            });
            when(attributeDefinitionRepository.findByProductTypeId(1L)).thenReturn(List.of());
            when(productRepository.findByIdComposite(10L)).thenReturn(Optional.of(testProduct));

            Product result = productService.createComposite(
                    1L, "SKU-PARENT", BigDecimal.valueOf(300),
                    ProductStatus.DRAFT, true, null,
                    translations, null, null, null, null, null, variants, TEST_USER_ID);

            assertThat(result).isNotNull();
            verify(productVariantRepository, org.mockito.Mockito.times(2)).save(any(ProductVariant.class));
            verify(productRepository, org.mockito.Mockito.atLeastOnce())
                    .save(argThat(product -> product.getBasePrice().compareTo(BigDecimal.valueOf(150)) == 0));
        }

        @Test
        @DisplayName("Should create default variant when variants omitted")
        void createComposite_WithNoVariants_CreatesDefaultVariant() {
            Map<Language, ProductI18nDto> translations = Map.of(Language.TR, new ProductI18nDto("Default Variant Product"));

            when(productTypeRepository.findById(1L)).thenReturn(Optional.of(testProductType));
            when(productRepository.existsBySku("SKU-DEFAULT")).thenReturn(false);
            when(productRepository.save(any(Product.class))).thenAnswer(inv -> {
                Product p = inv.getArgument(0);
                p.setId(10L);
                return p;
            });
            when(attributeDefinitionRepository.findByProductTypeId(1L)).thenReturn(List.of());
            when(productRepository.findByIdComposite(10L)).thenReturn(Optional.of(testProduct));

            productService.createComposite(
                    1L, "SKU-DEFAULT", BigDecimal.valueOf(75),
                    ProductStatus.DRAFT, true, null,
                    translations, null, null, null, null, null, null, TEST_USER_ID);

            verify(productVariantRepository).save(argThat(variant ->
                    "SKU-DEFAULT".equals(variant.getSku())
                            && variant.getPrice().compareTo(BigDecimal.valueOf(75)) == 0
                            && variant.getVatRate().compareTo(BigDecimal.valueOf(20)) == 0
                            && variant.getStockQuantity() == 0
                            && Boolean.TRUE.equals(variant.getActive())));
        }

        @Test
        @DisplayName("Should reject duplicate variant SKU in request")
        void createComposite_ThrowsException_WhenVariantSkuDuplicatedInRequest() {
            Map<Language, ProductI18nDto> translations = Map.of(Language.TR, new ProductI18nDto("Variant Product"));
            List<ProductVariantDto> variants = List.of(
                    new ProductVariantDto("SKU-DUP", BigDecimal.TEN, null, BigDecimal.valueOf(20), 0, true, null,
                            List.of()),
                    new ProductVariantDto("sku-dup", BigDecimal.TEN, null, BigDecimal.valueOf(20), 0, true, null,
                            List.of()));

            when(productTypeRepository.findById(1L)).thenReturn(Optional.of(testProductType));
            when(productRepository.existsBySku("SKU-PARENT")).thenReturn(false);
            when(productRepository.save(any(Product.class))).thenAnswer(inv -> {
                Product p = inv.getArgument(0);
                p.setId(10L);
                return p;
            });

            assertThatThrownBy(() -> productService.createComposite(
                    1L, "SKU-PARENT", BigDecimal.TEN,
                    ProductStatus.DRAFT, true, null,
                    translations, null, null, null, null, null, variants, TEST_USER_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Duplicate variant SKU");
        }

        @Test
        @DisplayName("Should reject firstPrice lower than price")
        void createComposite_ThrowsException_WhenFirstPriceLowerThanPrice() {
            Map<Language, ProductI18nDto> translations = Map.of(Language.TR, new ProductI18nDto("Variant Product"));
            List<ProductVariantDto> variants = List.of(
                    new ProductVariantDto("SKU-SALE", BigDecimal.valueOf(100), BigDecimal.valueOf(90),
                            BigDecimal.valueOf(20), 0, true, null, List.of()));

            when(productTypeRepository.findById(1L)).thenReturn(Optional.of(testProductType));
            when(productRepository.existsBySku("SKU-PARENT")).thenReturn(false);
            when(productRepository.save(any(Product.class))).thenAnswer(inv -> {
                Product p = inv.getArgument(0);
                p.setId(10L);
                return p;
            });

            assertThatThrownBy(() -> productService.createComposite(
                    1L, "SKU-PARENT", BigDecimal.TEN,
                    ProductStatus.DRAFT, true, null,
                    translations, null, null, null, null, null, variants, TEST_USER_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("first price cannot be less than price");
        }

        @Test
        @DisplayName("Should reject negative stock quantity")
        void createComposite_ThrowsException_WhenVariantStockIsNegative() {
            Map<Language, ProductI18nDto> translations = Map.of(Language.TR, new ProductI18nDto("Variant Product"));
            List<ProductVariantDto> variants = List.of(
                    new ProductVariantDto("SKU-STOCK", BigDecimal.valueOf(100), null,
                            BigDecimal.valueOf(20), -1, true, null, List.of()));

            when(productTypeRepository.findById(1L)).thenReturn(Optional.of(testProductType));
            when(productRepository.existsBySku("SKU-PARENT")).thenReturn(false);
            when(productRepository.save(any(Product.class))).thenAnswer(inv -> {
                Product p = inv.getArgument(0);
                p.setId(10L);
                return p;
            });

            assertThatThrownBy(() -> productService.createComposite(
                    1L, "SKU-PARENT", BigDecimal.TEN,
                    ProductStatus.DRAFT, true, null,
                    translations, null, null, null, null, null, variants, TEST_USER_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("stock quantity must be zero or greater");
        }

        @Test
        @DisplayName("Should reject publish without active valid variant")
        void updateStatus_ThrowsException_WhenPublishedWithoutActiveVariant() {
            testProduct.getVariants().clear();
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            when(productVariantRepository.findByProductIdOrderByIdAsc(1L)).thenReturn(List.of());

            assertThatThrownBy(() -> productService.updateStatus(1L, ProductStatus.PUBLISHED, TEST_USER_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("requires at least one active valid variant");
        }

        @Test
        @DisplayName("Should reject more than two variant options per product")
        void createComposite_ThrowsException_WhenMoreThanTwoOptionsUsed() {
            Map<Language, ProductI18nDto> translations = Map.of(Language.TR, new ProductI18nDto("Variant Product"));
            ProductVariantOption option1 = option(1L, "color");
            ProductVariantOption option2 = option(2L, "size");
            ProductVariantOption option3 = option(3L, "material");
            List<ProductVariantOptionValue> values = List.of(
                    value(11L, option1, "red"),
                    value(12L, option2, "large"),
                    value(13L, option3, "cotton"));
            List<ProductVariantDto> variants = List.of(
                    new ProductVariantDto("SKU-3OPT", BigDecimal.TEN, null, BigDecimal.valueOf(20), 0, true, null,
                            List.of(11L, 12L, 13L)));

            when(productTypeRepository.findById(1L)).thenReturn(Optional.of(testProductType));
            when(productRepository.existsBySku("SKU-PARENT")).thenReturn(false);
            when(productRepository.save(any(Product.class))).thenAnswer(inv -> {
                Product p = inv.getArgument(0);
                p.setId(10L);
                return p;
            });
            when(productVariantOptionValueRepository.findByIdIn(anyCollection())).thenReturn(values);

            assertThatThrownBy(() -> productService.createComposite(
                    1L, "SKU-PARENT", BigDecimal.TEN,
                    ProductStatus.DRAFT, true, null,
                    translations, null, null, null, null, null, variants, TEST_USER_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("at most 2 variant options");
        }

        private ProductVariantOption option(Long id, String code) {
            ProductVariantOption option = new ProductVariantOption();
            option.setId(id);
            option.setCode(code);
            option.setName(code);
            option.setDisplayType(ProductVariantOptionDisplayType.TEXT);
            option.setSortOrder(0);
            option.setActive(true);
            return option;
        }

        private ProductVariantOptionValue value(Long id, ProductVariantOption option, String code) {
            ProductVariantOptionValue value = new ProductVariantOptionValue();
            value.setId(id);
            value.setOption(option);
            value.setCode(code);
            value.setLabel(code);
            value.setActive(true);
            return value;
        }
    }

    @Nested
    @DisplayName("delete() Tests")
    class DeleteTests {

        @Test
        @DisplayName("Should delete product successfully")
        void delete_Success() {
            // Given
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            doNothing().when(productRepository).delete(testProduct);

            // When
            productService.delete(1L);

            // Then
            verify(productRepository).findById(1L);
            verify(productRepository).delete(testProduct);
        }

        @Test
        @DisplayName("Should throw exception when product not found")
        void delete_ThrowsException_WhenProductNotFound() {
            // Given
            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> productService.delete(999L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Product not found");
        }

        @Test
        @DisplayName("Should throw exception when tenant context is not active")
        void delete_ThrowsException_WhenTenantNotActive() {
            // Given
            simulateInactiveTenantContext();

            // When & Then
            assertThatThrownBy(() -> productService.delete(1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Tenant context");
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
            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            // When
            Optional<Product> result = productService.findById(999L);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("findById should return product when found")
        void findById_ReturnsProduct_WhenFound() {
            // Given
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

            // When
            Optional<Product> result = productService.findById(1L);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("findByIdComposite should eager load all collections")
        void findByIdComposite_EagerLoadsAllCollections() {
            // Given
            when(productRepository.findByIdComposite(1L)).thenReturn(Optional.of(testProduct));
            when(productI18nRepository.findByProductId(1L)).thenReturn(List.of());
            when(productCategoryLinkRepository.findByProductIdWithCategories(1L)).thenReturn(List.of());
            when(productMediaRepository.findByProductIdWithMedia(1L)).thenReturn(List.of());

            // When
            Optional<Product> result = productService.findByIdComposite(1L);

            // Then
            assertThat(result).isPresent();
            verify(productRepository).findByIdComposite(1L);
            verify(productI18nRepository).findByProductId(1L);
            verify(productCategoryLinkRepository).findByProductIdWithCategories(1L);
            verify(productMediaRepository).findByProductIdWithMedia(1L);
        }

        @Test
        @DisplayName("findBySku should return product")
        void findBySku_ReturnsProduct() {
            // Given
            when(productRepository.findBySku("SKU-001")).thenReturn(Optional.of(testProduct));

            // When
            Optional<Product> result = productService.findBySku("SKU-001");

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getSku()).isEqualTo("SKU-001");
        }

        @Test
        @DisplayName("findByUid should return product")
        void findByUid_ReturnsProduct() {
            // Given
            when(productRepository.findByUid("product_1")).thenReturn(Optional.of(testProduct));

            // When
            Optional<Product> result = productService.findByUid("product_1");

            // Then
            assertThat(result).isPresent();
        }

        @Test
        @DisplayName("findAllPaged should return paginated results")
        void findAllPaged_ReturnsPaginatedResults() {
            // Given
            Page<Product> productPage = new PageImpl<>(List.of(testProduct), PageRequest.of(0, 10), 1);
            when(productRepository.findAllPaged(any(Pageable.class))).thenReturn(productPage);

            // When
            Page<Product> result = productService.findAllPaged(PageRequest.of(0, 10));

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("search should filter with multiple criteria")
        void search_WithMultipleFilters() {
            // Given
            Page<Product> productPage = new PageImpl<>(List.of(testProduct));
            when(productRepository.searchWithFilters(eq("test"), eq(ProductStatus.PUBLISHED), eq(1L),
                    any(Pageable.class)))
                    .thenReturn(productPage);

            // When
            Page<Product> result = productService.search("test", ProductStatus.PUBLISHED, 1L, PageRequest.of(0, 10));

            // Then
            assertThat(result).isNotNull();
            verify(productRepository).searchWithFilters("test", ProductStatus.PUBLISHED, 1L, PageRequest.of(0, 10));
        }
    }

    // ==================== updateStatus() and updateVisibility() Tests
    // ====================

    @Nested
    @DisplayName("Status and Visibility Update Tests")
    class StatusVisibilityTests {

        @Test
        @DisplayName("updateStatus should change product status")
        void updateStatus_Success() {
            // Given
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            // When
            Product result = productService.updateStatus(1L, ProductStatus.PUBLISHED, TEST_USER_ID);

            // Then
            assertThat(result).isNotNull();
            verify(productRepository).save(argThat(product -> product.getStatus() == ProductStatus.PUBLISHED));
        }

        @Test
        @DisplayName("updateStatus should throw exception when product not found")
        void updateStatus_ThrowsException_WhenNotFound() {
            // Given
            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> productService.updateStatus(999L, ProductStatus.PUBLISHED, TEST_USER_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Product not found");
        }

        @Test
        @DisplayName("updateVisibility should toggle visibility")
        void updateVisibility_Success() {
            // Given
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            // When
            Product result = productService.updateVisibility(1L, false, TEST_USER_ID);

            // Then
            assertThat(result).isNotNull();
            verify(productRepository).save(argThat(product -> product.getIsVisible() == false));
        }
    }
}
