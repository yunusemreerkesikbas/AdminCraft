package com.backend.domain.entity;

import java.util.ArrayList;
import java.util.List;

import com.backend.domain.enums.ProductVariantOptionDisplayType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
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

@Entity
@Table(name = "product_variant_options", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "uid" }, name = "uk_product_variant_option_uid"),
        @UniqueConstraint(columnNames = { "code" }, name = "uk_product_variant_option_code")
}, indexes = {
        @Index(columnList = "active", name = "idx_product_variant_option_active"),
        @Index(columnList = "sort_order", name = "idx_product_variant_option_sort")
})
@Data
@EqualsAndHashCode(callSuper = true, exclude = { "values" })
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantOption extends BaseEntity {

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String code;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "display_type", nullable = false, length = 20)
    private ProductVariantOptionDisplayType displayType = ProductVariantOptionDisplayType.TEXT;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(nullable = false)
    private Boolean active = true;

    @ToString.Exclude
    @OneToMany(mappedBy = "option", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("sortOrder ASC, id ASC")
    private List<ProductVariantOptionValue> values = new ArrayList<>();

    public void addValue(ProductVariantOptionValue value) {
        values.add(value);
        value.setOption(this);
    }
}
