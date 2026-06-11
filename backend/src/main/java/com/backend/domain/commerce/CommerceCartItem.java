package com.backend.domain.commerce;

import java.math.BigDecimal;

import static com.backend.domain.commerce.CommerceCartLimits.MAX_QUANTITY;
import static com.backend.domain.commerce.CommerceCartLimits.MIN_QUANTITY;

import com.backend.domain.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "commerce_cart_items", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "uid" }, name = "uk_commerce_cart_item_uid"),
        @UniqueConstraint(columnNames = { "cart_id", "variant_uid" }, name = "uk_commerce_cart_item_cart_variant")
}, indexes = {
        @Index(columnList = "cart_id", name = "idx_commerce_cart_item_cart"),
        @Index(columnList = "variant_uid", name = "idx_commerce_cart_item_variant_uid")
})
@Data
@EqualsAndHashCode(callSuper = true, exclude = { "cart" })
@NoArgsConstructor
@AllArgsConstructor
public class CommerceCartItem extends BaseEntity {

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private CommerceCart cart;

    @NotBlank
    @Column(name = "product_uid", nullable = false, length = 50)
    private String productUid;

    @NotBlank
    @Column(name = "product_sku", nullable = false, length = 100)
    private String productSku;

    @NotBlank
    @Column(name = "variant_uid", nullable = false, length = 50)
    private String variantUid;

    @NotBlank
    @Column(name = "variant_sku", nullable = false, length = 100)
    private String variantSku;

    @NotNull
    @Min(MIN_QUANTITY)
    @Max(MAX_QUANTITY)
    @Column(nullable = false)
    private Integer quantity;

    @NotNull
    @DecimalMin("0.0")
    @Column(name = "unit_gross_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal unitGrossPrice = BigDecimal.ZERO;

    @NotNull
    @DecimalMin("0.0")
    @Column(name = "vat_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal vatRate;
}
