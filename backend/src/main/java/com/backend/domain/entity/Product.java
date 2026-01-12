package com.backend.domain.entity;

import com.backend.domain.enums.ProductStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"uid"}, name = "uk_product_uid"),
        @UniqueConstraint(columnNames = {"sku"}, name = "uk_product_sku")
}, indexes = {
        @Index(columnList = "product_type_id", name = "idx_product_type"),
        @Index(columnList = "status", name = "idx_product_status"),
        @Index(columnList = "is_visible", name = "idx_product_visible"),
        @Index(columnList = "responsive_id", name = "idx_product_responsive")
})
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"productType", "responsiveMediaSet", "i18nContent", "attributes", "categoryLinks", "gallery"})
@NoArgsConstructor
@AllArgsConstructor
public class Product extends BaseEntity {

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_type_id", nullable = false)
    private ProductType productType;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String sku;

    @Column(name = "base_price", precision = 15, scale = 2)
    private BigDecimal basePrice;

    @Size(max = 3)
    @Column(length = 3)
    private String currency = "TRY";

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductStatus status = ProductStatus.DRAFT;

    @Column(name = "is_visible")
    private Boolean isVisible = true;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsive_id")
    private ResponsiveMediaSet responsiveMediaSet;

    @ToString.Exclude
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProductI18n> i18nContent = new ArrayList<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProductAttribute> attributes = new ArrayList<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProductCategoryLink> categoryLinks = new ArrayList<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("sortOrder ASC, id ASC")
    private List<ProductMedia> gallery = new ArrayList<>();

    public void addI18n(ProductI18n i18n) {
        i18nContent.add(i18n);
        i18n.setProduct(this);
    }

    public void addAttribute(ProductAttribute attribute) {
        attributes.add(attribute);
        attribute.setProduct(this);
    }

    public void addCategoryLink(ProductCategoryLink link) {
        categoryLinks.add(link);
        link.setProduct(this);
    }

    public void addGalleryItem(ProductMedia media) {
        gallery.add(media);
        media.setProduct(this);
    }
}
