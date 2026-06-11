package com.backend.domain.entity;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "product_variants", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "uid" }, name = "uk_product_variant_uid"),
        @UniqueConstraint(columnNames = { "sku" }, name = "uk_product_variant_sku")
}, indexes = {
        @Index(columnList = "product_id", name = "idx_product_variant_product"),
        @Index(columnList = "active", name = "idx_product_variant_active"),
        @Index(columnList = "responsive_id", name = "idx_product_variant_responsive")
})
@Data
@EqualsAndHashCode(callSuper = true, exclude = { "product", "responsiveMediaSet", "optionValues" })
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariant extends BaseEntity {

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String sku;

    @NotNull
    @DecimalMin("0.0")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;

    @DecimalMin("0.0")
    @Column(name = "first_price", precision = 15, scale = 2)
    private BigDecimal firstPrice;

    @NotNull
    @DecimalMin("0.0")
    @Column(name = "vat_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal vatRate = BigDecimal.valueOf(20);

    @NotNull
    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity = 0;

    @Column(nullable = false)
    private Boolean active = true;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsive_id")
    private ResponsiveMediaSet responsiveMediaSet;

    @ToString.Exclude
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "product_variant_value_links",
            joinColumns = @JoinColumn(name = "variant_id"),
            inverseJoinColumns = @JoinColumn(name = "option_value_id"))
    private Set<ProductVariantOptionValue> optionValues = new LinkedHashSet<>();
}
