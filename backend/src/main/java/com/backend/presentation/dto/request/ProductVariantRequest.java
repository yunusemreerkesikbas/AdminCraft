package com.backend.presentation.dto.request;

import static com.backend.shared.constants.ValidationConstants.PRICE_MIN;
import static com.backend.shared.constants.ValidationConstants.SKU_MAX_LENGTH;

import java.math.BigDecimal;
import java.util.List;

import com.backend.shared.validation.Sku;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record ProductVariantRequest(
        @Sku(maxLength = SKU_MAX_LENGTH) String sku,
        @DecimalMin(value = PRICE_MIN, inclusive = true, message = "validation.product.variant.price.min") BigDecimal price,
        @DecimalMin(value = PRICE_MIN, inclusive = true, message = "validation.product.variant.firstPrice.min") BigDecimal firstPrice,
        @DecimalMin(value = PRICE_MIN, inclusive = true, message = "validation.product.variant.vatRate.min") BigDecimal vatRate,
        @Min(value = 0, message = "validation.product.variant.stock.min") Integer stockQuantity,
        Boolean active,
        Long responsiveMediaId,
        @Size(max = 2, message = "validation.product.variant.options.max") List<Long> optionValueIds) {
    public ProductVariantRequest {
        sku = sku != null ? sku.trim() : null;
    }
}
