package com.backend.application.dto.response;

import java.util.Comparator;
import java.util.List;

import com.backend.domain.entity.ProductVariantOption;
import com.backend.domain.enums.ProductVariantOptionDisplayType;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProductVariantOptionResponse(
        Long id,
        String uid,
        String code,
        String name,
        ProductVariantOptionDisplayType displayType,
        Integer sortOrder,
        Boolean active,
        List<ProductVariantOptionValueResponse> values) {
    public static ProductVariantOptionResponse from(ProductVariantOption entity) {
        List<ProductVariantOptionValueResponse> values = entity.getValues() == null
                ? List.of()
                : entity.getValues().stream()
                        .map(ProductVariantOptionValueResponse::from)
                        .sorted(Comparator.comparing(ProductVariantOptionValueResponse::sortOrder)
                                .thenComparing(ProductVariantOptionValueResponse::id))
                        .toList();
        return new ProductVariantOptionResponse(
                entity.getId(),
                entity.getUid(),
                entity.getCode(),
                entity.getName(),
                entity.getDisplayType(),
                entity.getSortOrder(),
                entity.getActive(),
                values);
    }
}
