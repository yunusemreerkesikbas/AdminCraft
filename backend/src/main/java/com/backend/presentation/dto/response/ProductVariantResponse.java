package com.backend.presentation.dto.response;

import java.util.Comparator;
import java.util.List;

import com.backend.domain.entity.ProductVariant;
import com.backend.domain.entity.ProductVariantOptionValue;
import com.backend.domain.enums.Currency;
import com.backend.shared.common.PriceResponse;
import com.backend.shared.util.ResponseValueFilter;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProductVariantResponse(
        Long id,
        String uid,
        String sku,
        PriceResponse price,
        PriceResponse firstPrice,
        String vatRate,
        Integer stockQuantity,
        Boolean active,
        ResponsiveMediaResponse images,
        List<ProductVariantOptionValueSelectionResponse> optionValues) {
    public static ProductVariantResponse from(ProductVariant entity, Currency currency) {
        List<ProductVariantOptionValueSelectionResponse> values = ResponseValueFilter.filterEmptyList(
                entity.getOptionValues() == null
                        ? null
                        : entity.getOptionValues().stream()
                                .filter(value -> value.getOption() != null)
								.sorted(Comparator
										.<ProductVariantOptionValue, Integer>comparing(
												value -> value.getOption().getSortOrder(),
												Comparator.nullsLast(Comparator.naturalOrder()))
										.thenComparing(value -> value.getOption().getId(), Comparator.nullsLast(Comparator.naturalOrder()))
										.thenComparing(value -> value.getId(), Comparator.nullsLast(Comparator.naturalOrder())))
                                .map(ProductVariantOptionValueSelectionResponse::from)
                                .toList());
        ResponsiveMediaResponse images = entity.getResponsiveMediaSet() == null
                ? null
                : ResponsiveMediaResponse.from(entity.getResponsiveMediaSet());
        return new ProductVariantResponse(
                entity.getId(),
                entity.getUid(),
                entity.getSku(),
                PriceResponse.from(entity.getPrice(), currency),
                entity.getFirstPrice() == null ? null : PriceResponse.from(entity.getFirstPrice(), currency),
                entity.getVatRate() == null ? null : entity.getVatRate().toPlainString(),
                entity.getStockQuantity(),
                entity.getActive(),
                images,
                values);
    }
}
