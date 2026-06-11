package com.backend.presentation.dto.request;

import static com.backend.shared.constants.ValidationConstants.GALLERY_MAX_SIZE;
import static com.backend.shared.constants.ValidationConstants.PRICE_MIN;
import static com.backend.shared.constants.ValidationConstants.SKU_MAX_LENGTH;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.backend.domain.enums.Language;
import com.backend.domain.enums.ProductStatus;
import com.backend.shared.validation.Sku;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductCompositeRequest(
        @NotNull(message = "validation.product.productTypeId.required") Long productTypeId,

        @Sku(maxLength = SKU_MAX_LENGTH) String sku,

        @DecimalMin(value = PRICE_MIN, inclusive = true, message = "validation.product.basePrice.min") BigDecimal basePrice,

        ProductStatus status,

        Boolean isVisible,

        Long responsiveMediaId,

        @Valid Map<Language, ProductI18nRequest> translations,

        Map<String, Object> attributes,

        List<Long> categoryIds,

        Long primaryCategoryId,

        @Valid @Size(max = GALLERY_MAX_SIZE, message = "validation.product.gallery.maxSize") List<ResponsiveMediaRequest> gallery,

        Map<String, Object> customFields,

        @Valid List<ProductVariantRequest> variants) {
    public ProductCompositeRequest {
        sku = sku != null ? sku.trim() : null;
        if (status == null)
            status = ProductStatus.DRAFT;
        if (isVisible == null)
            isVisible = true;
    }

    public ProductCompositeRequest(Long productTypeId, String sku, BigDecimal basePrice,
            ProductStatus status, Boolean isVisible, Long responsiveMediaId,
            Map<Language, ProductI18nRequest> translations,
            Map<String, Object> attributes,
            List<Long> categoryIds,
            Long primaryCategoryId,
            List<ResponsiveMediaRequest> gallery,
            Map<String, Object> customFields) {
        this(productTypeId, sku, basePrice, status, isVisible, responsiveMediaId, translations, attributes,
                categoryIds, primaryCategoryId, gallery, customFields, null);
    }
}
