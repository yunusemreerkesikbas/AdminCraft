package com.backend.presentation.dto.request;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.backend.domain.enums.Language;
import com.backend.domain.enums.ProductStatus;
import com.backend.presentation.validation.Sku;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import static com.backend.shared.constants.ValidationConstants.CURRENCY_MAX_LENGTH;
import static com.backend.shared.constants.ValidationConstants.PRICE_MIN;
import static com.backend.shared.constants.ValidationConstants.SKU_MAX_LENGTH;

public record ProductCompositeRequest(
        @NotNull(message = "validation.product.productTypeId.required")
        Long productTypeId,

        @Sku(maxLength = SKU_MAX_LENGTH)
        String sku,

        @DecimalMin(value = PRICE_MIN, inclusive = true, message = "validation.product.basePrice.min")
        BigDecimal basePrice,

        @Size(max = CURRENCY_MAX_LENGTH, message = "validation.product.currency.size")
        String currency,

        ProductStatus status,

        Boolean isVisible,

        Long responsiveMediaId,

        @NotEmpty(message = "validation.product.translations.required")
        @Valid
        Map<Language, ProductI18nRequest> translations,

        Map<String, Object> attributes,

        List<Long> categoryIds,

        Long primaryCategoryId,

        List<Long> galleryMediaIds
) {
    public ProductCompositeRequest {
        sku = sku != null ? sku.trim() : null;
        currency = currency != null && !currency.isBlank() ? currency.trim() : "TRY";
        if (status == null)
            status = ProductStatus.DRAFT;
        if (isVisible == null)
            isVisible = true;
    }
}
