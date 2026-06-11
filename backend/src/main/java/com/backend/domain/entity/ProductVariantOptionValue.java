package com.backend.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "product_variant_option_values", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "uid" }, name = "uk_product_variant_option_value_uid"),
        @UniqueConstraint(columnNames = { "option_id", "code" }, name = "uk_product_variant_option_value_code")
}, indexes = {
        @Index(columnList = "option_id", name = "idx_product_variant_option_value_option"),
        @Index(columnList = "active", name = "idx_product_variant_option_value_active"),
        @Index(columnList = "sort_order", name = "idx_product_variant_option_value_sort")
})
@Data
@EqualsAndHashCode(callSuper = true, exclude = { "option" })
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantOptionValue extends BaseEntity {

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id", nullable = false)
    private ProductVariantOption option;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String code;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String label;

    @Size(max = 50)
    @Column(name = "swatch_value", length = 50)
    private String swatchValue;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(nullable = false)
    private Boolean active = true;
}
