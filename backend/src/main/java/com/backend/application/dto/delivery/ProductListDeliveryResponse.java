package com.backend.application.dto.delivery;

import com.backend.domain.entity.Product;
import com.backend.domain.entity.ProductI18n;
import com.backend.domain.enums.Currency;
import com.backend.domain.enums.Language;
import com.backend.shared.common.PriceResponse;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProductListDeliveryResponse {
    private String uid;
    private String sku;
    private String name;
    private String shortDescription;
    private BigDecimal basePrice;
    private PriceResponse price;
    private String thumbnailUrl;
    private String productTypeName;

    public static ProductListDeliveryResponse from(Product product, Language language) {
        return from(product, language, Currency.getDefault());
    }

    public static ProductListDeliveryResponse from(Product product, Language language, Currency currency) {
        if (product == null) return null;

        String name = null, shortDesc = null;
        if (product.getI18nContent() != null) {
            ProductI18n i18n = product.getI18nContent().stream()
                    .filter(i -> i.getLanguage() == language)
                    .findFirst()
                    .orElse(null);
            if (i18n != null) {
                name = i18n.getName();
                shortDesc = i18n.getShortDescription();
            }
        }

        String thumbnailUrl = null;
        if (product.getResponsiveMediaSet() != null 
                && product.getResponsiveMediaSet().getDesktopMedia() != null) {
            thumbnailUrl = "/api/media/files/" + product.getResponsiveMediaSet().getDesktopMedia().getFileName();
        }

        return ProductListDeliveryResponse.builder()
                .uid(product.getUid())
                .sku(product.getSku())
                .name(name != null ? name : product.getSku())
                .shortDescription(shortDesc)
                .basePrice(product.getBasePrice())
                .price(PriceResponse.from(product.getBasePrice(), currency))
                .thumbnailUrl(thumbnailUrl)
                .productTypeName(product.getProductType() != null ? product.getProductType().getName() : null)
                .build();
    }
}
