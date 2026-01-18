package com.backend.application.dto.delivery;

import java.math.BigDecimal;
import java.util.List;

import com.backend.domain.entity.Category;
import com.backend.domain.entity.CategoryI18n;
import com.backend.domain.entity.Media;
import com.backend.domain.entity.Product;
import com.backend.domain.entity.ProductAttribute;
import com.backend.domain.entity.ProductCategoryLink;
import com.backend.domain.entity.ProductI18n;
import com.backend.domain.entity.ResponsiveMediaSet;
import com.backend.domain.enums.Language;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductDeliveryResponse {
    private String uid;
    private String sku;
    private String name;
    private String shortDescription;
    private String description;
    private BigDecimal basePrice;
    private String currency;
    private String seoTitle;
    private String seoDescription;
    private String productTypeName;
    private ResponsiveMediaDelivery mainImage;
    private List<AttributeDelivery> attributes;
    private List<CategoryDelivery> categories;
    private List<ResponsiveMediaDelivery> gallery;

    public static ProductDeliveryResponse from(Product product, Language language) {
        if (product == null)
            return null;

        String name = null, shortDesc = null, description = null, seoTitle = null, seoDesc = null;
        if (product.getI18nContent() != null) {
            ProductI18n i18n = product.getI18nContent().stream()
                    .filter(i -> i.getLanguage() == language)
                    .findFirst()
                    .orElse(null);
            if (i18n != null) {
                name = i18n.getName();
                shortDesc = i18n.getShortDescription();
                description = i18n.getDescription();
                seoTitle = i18n.getSeoTitle();
                seoDesc = i18n.getSeoDescription();
            }
        }

        List<AttributeDelivery> attrs = product.getAttributes() != null
                ? product.getAttributes().stream()
                        .filter(a -> a.getAttributeDefinition() != null)
                        .map(AttributeDelivery::from)
                        .toList()
                : List.of();

        List<CategoryDelivery> cats = product.getCategoryLinks() != null
                ? product.getCategoryLinks().stream()
                        .filter(l -> l.getCategory() != null)
                        .map(l -> CategoryDelivery.from(l, language))
                        .toList()
                : List.of();

        List<ResponsiveMediaDelivery> gallery = product.getGallery() != null
                ? product.getGallery().stream()
                        .filter(pm -> pm.getResponsiveMediaSet() != null)
                        .map(pm -> ResponsiveMediaDelivery.from(pm.getResponsiveMediaSet()))
                        .toList()
                : List.of();

        ResponsiveMediaDelivery mainImage = null;
        if (product.getResponsiveMediaSet() != null) {
            mainImage = ResponsiveMediaDelivery.from(product.getResponsiveMediaSet());
        }

        return ProductDeliveryResponse.builder()
                .uid(product.getUid())
                .sku(product.getSku())
                .name(name != null ? name : product.getSku())
                .shortDescription(shortDesc)
                .description(description)
                .basePrice(product.getBasePrice())
                .currency(product.getCurrency())
                .seoTitle(seoTitle)
                .seoDescription(seoDesc)
                .productTypeName(product.getProductType() != null ? product.getProductType().getName() : null)
                .mainImage(mainImage)
                .attributes(attrs)
                .categories(cats)
                .gallery(gallery)
                .build();
    }

    @Data
    @Builder
    public static class AttributeDelivery {
        private String code;
        private String name;
        private String fieldType;
        private Object value;

        public static AttributeDelivery from(ProductAttribute attr) {
            return AttributeDelivery.builder()
                    .code(attr.getAttributeDefinition().getCode())
                    .name(attr.getAttributeDefinition().getName())
                    .fieldType(attr.getAttributeDefinition().getFieldType().name())
                    .value(attr.getValue())
                    .build();
        }
    }

    @Data
    @Builder
    public static class CategoryDelivery {
        private String uid;
        private String code;
        private String name;
        private Boolean isPrimary;

        public static CategoryDelivery from(ProductCategoryLink link, Language language) {
            Category cat = link.getCategory();
            String name = cat.getI18nContent() != null
                    ? cat.getI18nContent().stream()
                            .filter(i -> i.getLanguage() == language)
                            .findFirst()
                            .map(CategoryI18n::getName)
                            .orElse(cat.getCode())
                    : cat.getCode();

            return CategoryDelivery.builder()
                    .uid(cat.getUid())
                    .code(cat.getCode())
                    .name(name)
                    .isPrimary(link.getIsPrimary())
                    .build();
        }
    }

    @Data
    @Builder
    public static class MediaDelivery {
        private String uid;
        private String url;
        private String mimeType;
        private Integer width;
        private Integer height;

        public static MediaDelivery from(Media m) {
            if (m == null)
                return null;
            return MediaDelivery.builder()
                    .uid(m.getUid())
                    .url("/api/media/files/" + m.getFileName())
                    .mimeType(m.getMimeType())
                    .width(m.getWidth())
                    .height(m.getHeight())
                    .build();
        }
    }

    @Data
    @Builder
    public static class ResponsiveMediaDelivery {
        private String uid;
        private MediaDelivery desktop;
        private MediaDelivery mobile;

        public static ResponsiveMediaDelivery from(ResponsiveMediaSet set) {
            if (set == null)
                return null;
            return ResponsiveMediaDelivery.builder()
                    .uid(set.getUid())
                    .desktop(set.getDesktopMedia() != null ? MediaDelivery.from(set.getDesktopMedia()) : null)
                    .mobile(set.getMobileMedia() != null ? MediaDelivery.from(set.getMobileMedia()) : null)
                    .build();
        }
    }
}
