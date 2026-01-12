package com.backend.presentation.dto.response;

import com.backend.domain.entity.Product;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.ProductStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record ProductCompositeResponse(
        Long id,
        String uuid,
        String uid,
        Long productTypeId,
        String productTypeName,
        String sku,
        BigDecimal basePrice,
        String currency,
        ProductStatus status,
        Boolean isVisible,
        ResponsiveMediaResponse responsiveMedia,
        Map<Language, ProductI18nResponse> translations,
        List<ProductAttributeValueResponse> attributes,
        List<ProductCategoryResponse> categories,
        List<ProductMediaResponse> gallery,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ProductCompositeResponse from(Product entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Product entity cannot be null");
        }

        Map<Language, ProductI18nResponse> translations = entity.getI18nContent() != null
                ? entity.getI18nContent().stream()
                    .collect(Collectors.toMap(
                            i18n -> i18n.getLanguage(),
                            ProductI18nResponse::from
                    ))
                : Map.of();

        List<ProductAttributeValueResponse> attributes = entity.getAttributes() != null
                ? entity.getAttributes().stream()
                    .filter(a -> a.getAttributeDefinition() != null)
                    .map(ProductAttributeValueResponse::from)
                    .toList()
                : List.of();

        List<ProductCategoryResponse> categories = entity.getCategoryLinks() != null
                ? entity.getCategoryLinks().stream()
                    .filter(l -> l.getCategory() != null)
                    .map(ProductCategoryResponse::from)
                    .toList()
                : List.of();

        List<ProductMediaResponse> gallery = entity.getGallery() != null
                ? entity.getGallery().stream()
                    .map(ProductMediaResponse::from)
                    .toList()
                : List.of();

        ResponsiveMediaResponse responsiveMedia = entity.getResponsiveMediaSet() != null
                ? ResponsiveMediaResponse.from(entity.getResponsiveMediaSet())
                : null;

        return new ProductCompositeResponse(
                entity.getId(),
                entity.getUuid(),
                entity.getUid(),
                entity.getProductType() != null ? entity.getProductType().getId() : null,
                entity.getProductType() != null ? entity.getProductType().getName() : null,
                entity.getSku(),
                entity.getBasePrice(),
                entity.getCurrency(),
                entity.getStatus(),
                entity.getIsVisible(),
                responsiveMedia,
                translations,
                attributes,
                categories,
                gallery,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
