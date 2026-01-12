package com.backend.presentation.dto.request;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.backend.domain.enums.Language;
import com.backend.domain.enums.ProductStatus;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductCompositeRequest(
        @NotNull(message = "validation.product.productTypeId.required") Long productTypeId,

        @NotBlank(message = "validation.product.sku.required") @Size(max = 100, message = "validation.product.sku.size") String sku,

        @DecimalMin(value = "0.0", inclusive = true, message = "validation.product.basePrice.min") BigDecimal basePrice,

        @Size(max = 3, message = "validation.product.currency.size") String currency,

        ProductStatus status,

        Boolean isVisible,

        Long responsiveMediaId,

        @NotEmpty(message = "validation.product.translations.required") @Valid Map<Language, ProductI18nRequest> translations,

        Map<String, Object> attributes,

        List<Long> categoryIds,

        Long primaryCategoryId,

        List<Long> galleryMediaIds) {
    public ProductCompositeRequest {
        if (currency == null || currency.isBlank())
            currency = "TRY";
        if (status == null)
            status = ProductStatus.DRAFT;
        if (isVisible == null)
            isVisible = true;
    }
}
