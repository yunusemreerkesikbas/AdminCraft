package com.backend.presentation.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.backend.domain.enums.Language;
import com.backend.domain.enums.ProductFieldType;
import com.backend.presentation.dto.request.AttributeDefinitionCreateRequest;
import com.backend.presentation.dto.request.CategoryI18nRequest;
import com.backend.presentation.dto.request.ProductCompositeRequest;
import com.backend.presentation.dto.request.ProductI18nRequest;
import com.backend.presentation.dto.request.ProductTypeCreateRequest;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

/**
 * Validation tests for Product Catalog DTOs.
 * Tests Bean Validation annotations on request DTOs.
 */
class ProductCatalogDtoValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // ==================== ProductCompositeRequest Tests ====================

    @Nested
    @DisplayName("ProductCompositeRequest Validation")
    class ProductCompositeRequestTests {

        @Test
        @DisplayName("Should pass validation with valid input")
        void productCompositeRequest_ValidInput() {
            // Given
            Map<Language, ProductI18nRequest> translations = new HashMap<>();
            translations.put(Language.TR, new ProductI18nRequest("Test Ürün", null, null, null, null));

            ProductCompositeRequest request = new ProductCompositeRequest(
                    1L, // productTypeId
                    "SKU-001", // sku
                    BigDecimal.valueOf(99.99), // basePrice
                    null, // status
                    true, // isVisible
                    null, // responsiveMediaId
                    translations, // translations
                    null, // attributes
                    null, // categoryIds
                    null, // primaryCategoryId
                    null, // galleryMediaIds
                    null // customFields
            );

            // When
            Set<ConstraintViolation<ProductCompositeRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should fail when productTypeId is null")
        void productCompositeRequest_ProductTypeIdRequired() {
            // Given
            Map<Language, ProductI18nRequest> translations = new HashMap<>();
            translations.put(Language.TR, new ProductI18nRequest("Test", null, null, null, null));

            ProductCompositeRequest request = new ProductCompositeRequest(
                    null, // productTypeId - MISSING
                    "SKU-001",
                    BigDecimal.valueOf(99.99),
                    null, true, null, translations, null, null, null, null, null);

            // When
            Set<ConstraintViolation<ProductCompositeRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("productTypeId"));
        }

    }

    // ==================== ProductI18nRequest Tests ====================

    @Nested
    @DisplayName("ProductI18nRequest Validation")
    class ProductI18nRequestTests {

        @Test
        @DisplayName("Should pass with valid name")
        void productI18nRequest_ValidInput() {
            // Given
            ProductI18nRequest request = new ProductI18nRequest(
                    "Valid Product Name",
                    "Short description",
                    "<p>Full description</p>",
                    "SEO Title",
                    "SEO Description");

            // When
            Set<ConstraintViolation<ProductI18nRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should fail when name exceeds max length")
        void productI18nRequest_NameMaxLength() {
            // Given
            String longName = "A".repeat(201); // > 200 chars
            ProductI18nRequest request = new ProductI18nRequest(
                    longName, null, null, null, null);

            // When
            Set<ConstraintViolation<ProductI18nRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("name"));
        }

        @Test
        @DisplayName("Should fail when shortDescription exceeds max length")
        void productI18nRequest_ShortDescriptionMaxLength() {
            // Given
            String longDesc = "A".repeat(501); // > 500 chars
            ProductI18nRequest request = new ProductI18nRequest(
                    "Valid Name", longDesc, null, null, null);

            // When
            Set<ConstraintViolation<ProductI18nRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("shortDescription"));
        }
    }

    // ==================== CategoryI18nRequest Tests ====================

    @Nested
    @DisplayName("CategoryI18nRequest Validation")
    class CategoryI18nRequestTests {

        @Test
        @DisplayName("Should pass with valid name")
        void categoryI18nRequest_ValidInput() {
            // Given
            CategoryI18nRequest request = new CategoryI18nRequest(
                    "Valid Category Name",
                    "Category description");

            // When
            Set<ConstraintViolation<CategoryI18nRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should fail when name is blank")
        void categoryI18nRequest_NameRequired() {
            // Given
            CategoryI18nRequest request = new CategoryI18nRequest("", "Description");

            // When
            Set<ConstraintViolation<CategoryI18nRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("name"));
        }

        @Test
        @DisplayName("Should fail when name exceeds max length")
        void categoryI18nRequest_NameMaxLength() {
            // Given
            String longName = "A".repeat(201); // > 200 chars
            CategoryI18nRequest request = new CategoryI18nRequest(longName, null);

            // When
            Set<ConstraintViolation<CategoryI18nRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("name"));
        }
    }

    // ==================== ProductTypeCreateRequest Tests ====================

    @Nested
    @DisplayName("ProductTypeCreateRequest Validation")
    class ProductTypeCreateRequestTests {

        @Test
        @DisplayName("Should pass with valid input")
        void productTypeCreateRequest_ValidInput() {
            // Given
            ProductTypeCreateRequest request = new ProductTypeCreateRequest(
                    "general_product",
                    "General Product",
                    "electronics");

            // When
            Set<ConstraintViolation<ProductTypeCreateRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should fail when name is blank")
        void productTypeCreateRequest_NameRequired() {
            // Given
            ProductTypeCreateRequest request = new ProductTypeCreateRequest(
                    "code", "", "category");

            // When
            Set<ConstraintViolation<ProductTypeCreateRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("name"));
        }

        @Test
        @DisplayName("Should fail when name exceeds max length")
        void productTypeCreateRequest_NameMaxLength() {
            // Given
            String longName = "A".repeat(101); // > 100 chars
            ProductTypeCreateRequest request = new ProductTypeCreateRequest(
                    "code", longName, "category");

            // When
            Set<ConstraintViolation<ProductTypeCreateRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isNotEmpty();
        }
    }

    // ==================== AttributeDefinitionCreateRequest Tests
    // ====================

    @Nested
    @DisplayName("AttributeDefinitionCreateRequest Validation")
    class AttributeDefinitionCreateRequestTests {

        @Test
        @DisplayName("Should pass with valid input")
        void attributeDefinitionCreateRequest_ValidInput() {
            // Given
            AttributeDefinitionCreateRequest request = new AttributeDefinitionCreateRequest(
                    "Color",
                    ProductFieldType.TEXT);

            // When
            Set<ConstraintViolation<AttributeDefinitionCreateRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should fail when name is blank")
        void attributeDefinitionCreateRequest_NameRequired() {
            // Given
            AttributeDefinitionCreateRequest request = new AttributeDefinitionCreateRequest(
                    "", ProductFieldType.TEXT);

            // When
            Set<ConstraintViolation<AttributeDefinitionCreateRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("name"));
        }

        @Test
        @DisplayName("Should fail when fieldType is null")
        void attributeDefinitionCreateRequest_FieldTypeRequired() {
            // Given
            AttributeDefinitionCreateRequest request = new AttributeDefinitionCreateRequest(
                    "Name", null);

            // When
            Set<ConstraintViolation<AttributeDefinitionCreateRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("fieldType"));
        }
    }
}
