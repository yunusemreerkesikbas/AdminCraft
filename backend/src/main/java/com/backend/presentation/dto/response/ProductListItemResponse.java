package com.backend.presentation.dto.response;

import com.backend.domain.entity.Product;
import com.backend.domain.entity.ProductCategoryLink;
import com.backend.domain.entity.ProductI18n;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.ProductStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductListItemResponse(
        Long id,
        String uuid,
        String uid,
        String sku,
        String name,
        ProductStatus status,
        Boolean isVisible,
        BigDecimal basePrice,
        String currency,
        String primaryCategoryName,
        String thumbnailUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ProductListItemResponse from(Product entity, Language language) {
        if (entity == null) {
            throw new IllegalArgumentException("Product entity cannot be null");
        }
        String name = entity.getI18nContent() != null
                ? entity.getI18nContent().stream()
                    .filter(i -> i.getLanguage() == language)
                    .findFirst()
                    .map(ProductI18n::getName)
                    .orElse(entity.getSku())
                : entity.getSku();

        String primaryCategoryName = entity.getCategoryLinks() != null
                ? entity.getCategoryLinks().stream()
                    .filter(l -> l.getIsPrimary() != null && l.getIsPrimary())
                    .findFirst()
                    .map(ProductCategoryLink::getCategory)
                    .map(c -> c.getI18nContent() != null
                            ? c.getI18nContent().stream()
                                .filter(i -> i.getLanguage() == language)
                                .findFirst()
                                .map(ci -> ci.getName())
                                .orElse(c.getCode())
                            : c.getCode())
                    .orElse(null)
                : null;

        String thumbnailUrl = entity.getResponsiveMediaSet() != null
                && entity.getResponsiveMediaSet().getDesktopMedia() != null
                ? "/api/media/files/" + entity.getResponsiveMediaSet().getDesktopMedia().getFileName()
                : null;

        return new ProductListItemResponse(
                entity.getId(),
                entity.getUuid(),
                entity.getUid(),
                entity.getSku(),
                name,
                entity.getStatus(),
                entity.getIsVisible(),
                entity.getBasePrice(),
                entity.getCurrency(),
                primaryCategoryName,
                thumbnailUrl,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static ProductListItemResponse from(Product entity) {
        return from(entity, Language.TR);
    }
}
