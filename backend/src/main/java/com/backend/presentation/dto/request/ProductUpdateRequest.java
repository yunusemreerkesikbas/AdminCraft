package com.backend.presentation.dto.request;

import static com.backend.shared.constants.ValidationConstants.GALLERY_MAX_SIZE;
import static com.backend.shared.constants.ValidationConstants.PRICE_MIN;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.backend.domain.enums.Language;
import com.backend.domain.enums.ProductStatus;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductUpdateRequest(
    @DecimalMin(value = PRICE_MIN, inclusive = true, message = "validation.product.basePrice.min") BigDecimal basePrice,

    ProductStatus status,

    Boolean isVisible,

    Long responsiveMediaId,

    @NotEmpty(message = "validation.product.translations.required") @Valid Map<Language, ProductI18nRequest> translations,

    Map<String, Object> attributes,

    List<Long> categoryIds,

    Long primaryCategoryId,

    @Valid @Size(max = GALLERY_MAX_SIZE, message = "validation.product.gallery.maxSize") List<ResponsiveMediaRequest> gallery,

    Map<String, Object> customFields,

    @Valid List<@NotNull ProductVariantRequest> variants) {

    public ProductUpdateRequest(BigDecimal basePrice,
            ProductStatus status,
            Boolean isVisible,
            Long responsiveMediaId,
            Map<Language, ProductI18nRequest> translations,
            Map<String, Object> attributes,
            List<Long> categoryIds,
            Long primaryCategoryId,
            List<ResponsiveMediaRequest> gallery,
            Map<String, Object> customFields) {
        this(basePrice, status, isVisible, responsiveMediaId, translations, attributes,
                categoryIds, primaryCategoryId, gallery, customFields, null);
    }
}
