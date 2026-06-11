package com.backend.application.dto.delivery;

import java.util.List;
import java.util.Comparator;

import com.backend.domain.entity.Category;
import com.backend.domain.entity.CategoryI18n;
import com.backend.domain.entity.Media;
import com.backend.domain.entity.Product;
import com.backend.domain.entity.ProductAttribute;
import com.backend.domain.entity.ProductCategoryLink;
import com.backend.domain.entity.ProductI18n;
import com.backend.domain.entity.ProductVariant;
import com.backend.domain.entity.ProductVariantOptionValue;
import com.backend.domain.entity.ResponsiveMediaSet;
import com.backend.domain.enums.Currency;
import com.backend.domain.enums.Language;
import com.backend.presentation.dto.PriceResponse;

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
    private PriceResponse price;
    private String seoTitle;
    private String seoDescription;
    private String productTypeName;
    private ResponsiveMediaDelivery mainImage;
    private List<AttributeDelivery> attributes;
    private List<CategoryDelivery> categories;
    private List<ResponsiveMediaDelivery> gallery;
    private List<VariantDelivery> variants;

    public static ProductDeliveryResponse from(Product product, Language language, Currency currency) {
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

        PriceResponse price = PriceResponse.from(product.getBasePrice(), currency);
        List<VariantDelivery> variants = product.getVariants() != null
                ? product.getVariants().stream()
                        .filter(v -> v.getActive() != null && v.getActive())
                        .map(v -> VariantDelivery.from(v, currency))
                        .toList()
                : List.of();

        return ProductDeliveryResponse.builder()
                .uid(product.getUid())
                .sku(product.getSku())
                .name(name != null ? name : product.getSku())
                .shortDescription(shortDesc)
                .description(description)
                .price(price)
                .seoTitle(seoTitle)
                .seoDescription(seoDesc)
                .productTypeName(product.getProductType() != null ? product.getProductType().getName() : null)
                .mainImage(mainImage)
                .attributes(attrs)
                .categories(cats)
                .gallery(gallery)
                .variants(variants)
                .build();
    }

    @Data
    @Builder
    public static class VariantDelivery {
        private String uid;
        private String sku;
        private PriceResponse price;
        private PriceResponse firstPrice;
        private String vatRate;
        private Integer stockQuantity;
        private List<VariantOptionValueDelivery> optionValues;

        public static VariantDelivery from(ProductVariant variant, Currency currency) {
            List<VariantOptionValueDelivery> values = variant.getOptionValues() != null
                    ? variant.getOptionValues().stream()
                            .filter(value -> value.getOption() != null)
							.sorted(Comparator
									.<ProductVariantOptionValue, Integer>comparing(
											value -> value.getOption().getSortOrder(),
											Comparator.nullsLast(Comparator.naturalOrder()))
									.thenComparing(value -> value.getOption().getId(), Comparator.nullsLast(Comparator.naturalOrder()))
									.thenComparing(ProductVariantOptionValue::getId, Comparator.nullsLast(Comparator.naturalOrder())))
                            .map(VariantOptionValueDelivery::from)
                            .toList()
                    : List.of();
            return VariantDelivery.builder()
                    .uid(variant.getUid())
                    .sku(variant.getSku())
                    .price(PriceResponse.from(variant.getPrice(), currency))
                    .firstPrice(variant.getFirstPrice() == null ? null : PriceResponse.from(variant.getFirstPrice(), currency))
                    .vatRate(variant.getVatRate() == null ? null : variant.getVatRate().toPlainString())
                    .stockQuantity(variant.getStockQuantity())
                    .optionValues(values)
                    .build();
        }
    }

    @Data
    @Builder
    public static class VariantOptionValueDelivery {
        private String optionCode;
        private String optionName;
        private String displayType;
        private String valueCode;
        private String valueLabel;
        private String swatchValue;

        public static VariantOptionValueDelivery from(ProductVariantOptionValue value) {
            return VariantOptionValueDelivery.builder()
                    .optionCode(value.getOption().getCode())
                    .optionName(value.getOption().getName())
                    .displayType(value.getOption().getDisplayType().name())
                    .valueCode(value.getCode())
                    .valueLabel(value.getLabel())
                    .swatchValue(value.getSwatchValue())
                    .build();
        }
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
