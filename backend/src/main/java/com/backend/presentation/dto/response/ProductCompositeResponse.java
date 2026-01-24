package com.backend.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.backend.domain.entity.Product;
import com.backend.domain.enums.Currency;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.ProductStatus;
import com.backend.presentation.dto.PriceResponse;

public record ProductCompositeResponse(
                Long id,
                String uuid,
                String uid,
                Long productTypeId,
                String productTypeName,
                String sku,
                PriceResponse price,
                ProductStatus status,
                Boolean isVisible,
                ResponsiveMediaResponse images,
                Map<Language, ProductI18nResponse> translations,
                Map<String, Object> attributes,
                List<ProductCategoryResponse> categories,
                List<ProductMediaResponse> galleryImages,
                Map<String, Object> customFields,
                LocalDateTime createdAt,
                LocalDateTime updatedAt) {
        public static ProductCompositeResponse from(Product entity, Currency currency) {
                if (entity == null) {
                        throw new IllegalArgumentException("Product entity cannot be null");
                }

                Map<Language, ProductI18nResponse> translations = entity.getI18nContent() != null
                                ? entity.getI18nContent().stream()
                                                .collect(Collectors.toMap(
                                                                i18n -> i18n.getLanguage(),
                                                                ProductI18nResponse::from))
                                : Map.of();

                Map<String, Object> attributes = entity.getAttributes() != null
                                ? entity.getAttributes().stream()
                                                .filter(a -> a.getAttributeDefinition() != null)
                                                .collect(Collectors.toMap(
                                                                a -> a.getAttributeDefinition().getCode(),
                                                                a -> a.getValue(),
                                                                (existing, replacement) -> replacement))
                                : Map.of();

                // Add custom fields mapping
                Map<String, Object> customFields = entity.getFieldValues() != null
                                ? entity.getFieldValues().stream()
                                                .filter(fv -> fv.getFieldDefinition() != null)
                                                .collect(Collectors.toMap(
                                                                fv -> fv.getFieldDefinition().getCode(),
                                                                fv -> fv.getValue(),
                                                                (existing, replacement) -> replacement))
                                : Map.of();

                List<ProductCategoryResponse> categories = entity.getCategoryLinks() != null
                                ? entity.getCategoryLinks().stream()
                                                .filter(l -> l.getCategory() != null)
                                                .map(ProductCategoryResponse::from)
                                                .toList()
                                : List.of();

                List<ProductMediaResponse> galleryImages = entity.getGallery() != null
                                ? entity.getGallery().stream()
                                                .map(ProductMediaResponse::from)
                                                .toList()
                                : List.of();

                ResponsiveMediaResponse images = entity.getResponsiveMediaSet() != null
                                ? ResponsiveMediaResponse.from(entity.getResponsiveMediaSet())
                                : null;

                PriceResponse price = PriceResponse.from(entity.getBasePrice(), currency);

                return new ProductCompositeResponse(
                                entity.getId(),
                                entity.getUuid(),
                                entity.getUid(),
                                entity.getProductType() != null ? entity.getProductType().getId() : null,
                                entity.getProductType() != null ? entity.getProductType().getName() : null,
                                entity.getSku(),
                                price,
                                entity.getStatus(),
                                entity.getIsVisible(),
                                images,
                                translations,
                                attributes,
                                categories,
                                galleryImages,
                                customFields,
                                entity.getCreatedAt(),
                                entity.getUpdatedAt());
        }
}
