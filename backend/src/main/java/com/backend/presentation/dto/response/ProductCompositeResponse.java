package com.backend.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.backend.domain.entity.Product;
import com.backend.domain.enums.Currency;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.ProductStatus;
import com.backend.shared.common.PriceResponse;
import com.backend.shared.util.ResponseValueFilter;

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
                List<ProductVariantResponse> variants,
                LocalDateTime createdAt,
                LocalDateTime updatedAt) {
        public static ProductCompositeResponse from(Product entity, Currency currency) {
                if (entity == null) {
                        throw new IllegalArgumentException("Product entity cannot be null");
                }

                Map<Language, ProductI18nResponse> translations = ResponseValueFilter.filterEmptyMap(
                                entity.getI18nContent() != null
                                                ? entity.getI18nContent().stream()
                                                                .collect(Collectors.toMap(
                                                                                i18n -> i18n.getLanguage(),
                                                                                ProductI18nResponse::from))
                                                : null);

                Map<String, Object> attributes = ResponseValueFilter.filterEmptyMap(
                                entity.getAttributes() != null
                                                ? entity.getAttributes().stream()
                                                                .filter(a -> a.getAttributeDefinition() != null)
                                                                .collect(Collectors.toMap(
                                                                                a -> a.getAttributeDefinition().getCode(),
                                                                                a -> a.getValue(),
                                                                                (existing, replacement) -> replacement))
                                                : null);

                Map<String, Object> customFields = ResponseValueFilter.filterEmptyMap(
                                entity.getFieldValues() != null
                                                ? entity.getFieldValues().stream()
                                                                .filter(fv -> fv.getFieldDefinition() != null)
                                                                .collect(Collectors.toMap(
                                                                                fv -> fv.getFieldDefinition().getCode(),
                                                                                fv -> fv.getValue(),
                                                                                (existing, replacement) -> replacement))
                                                : null);

                List<ProductCategoryResponse> categories = ResponseValueFilter.filterEmptyList(
                                entity.getCategoryLinks() != null
                                                ? entity.getCategoryLinks().stream()
                                                                .filter(l -> l.getCategory() != null)
                                                                .map(ProductCategoryResponse::from)
                                                                .toList()
                                                : null);

                List<ProductMediaResponse> galleryImages = ResponseValueFilter.filterEmptyList(
                                entity.getGallery() != null
                                                ? entity.getGallery().stream()
                                                                .map(ProductMediaResponse::from)
                                                                .toList()
                                                : null);

                List<ProductVariantResponse> variants = ResponseValueFilter.filterEmptyList(
                                entity.getVariants() != null
                                                ? entity.getVariants().stream()
                                                                .map(variant -> ProductVariantResponse.from(variant,
                                                                                currency))
                                                                .toList()
                                                : null);

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
                                variants,
                                entity.getCreatedAt(),
                                entity.getUpdatedAt());
        }
}
