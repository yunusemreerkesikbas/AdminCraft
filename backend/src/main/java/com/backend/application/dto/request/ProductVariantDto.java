package com.backend.application.dto.request;

import static com.backend.shared.constants.ValidationConstants.PRICE_MIN;
import static com.backend.shared.constants.ValidationConstants.SKU_MAX_LENGTH;

import java.math.BigDecimal;
import java.util.List;

import com.backend.shared.validation.Sku;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductVariantDto(
		@NotBlank @Sku(maxLength = SKU_MAX_LENGTH) String sku,
		@NotNull @DecimalMin(value = PRICE_MIN, inclusive = true) BigDecimal price,
		@DecimalMin(value = PRICE_MIN, inclusive = true) BigDecimal firstPrice,
		@NotNull @DecimalMin(value = PRICE_MIN, inclusive = true) BigDecimal vatRate,
		@NotNull @Min(0) Integer stockQuantity,
        Boolean active,
        Long responsiveMediaId,
		@Size(max = 2) List<@NotNull Long> optionValueIds) {
    public ProductVariantDto {
        sku = sku != null ? sku.trim() : null;
    }
}
