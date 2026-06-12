package com.backend.domain.commerce;

import java.math.BigDecimal;

import com.backend.domain.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "commerce_checkout_items", uniqueConstraints = {
		@UniqueConstraint(columnNames = { "uuid" }, name = "uk_commerce_checkout_item_uuid"),
		@UniqueConstraint(columnNames = { "uid" }, name = "uk_commerce_checkout_item_uid"),
		@UniqueConstraint(columnNames = { "checkout_id", "variant_uid" }, name = "uk_commerce_checkout_item_checkout_variant")
}, indexes = {
		@Index(columnList = "checkout_id", name = "idx_commerce_checkout_item_checkout"),
		@Index(columnList = "variant_uid", name = "idx_commerce_checkout_item_variant_uid")
})
@Data
@EqualsAndHashCode(callSuper = true, exclude = { "checkout" })
@NoArgsConstructor
@AllArgsConstructor
public class CommerceCheckoutItem extends BaseEntity {

	@ToString.Exclude
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "checkout_id", nullable = false)
	private CommerceCheckout checkout;

	@Column(name = "product_uid", nullable = false, length = 50)
	private String productUid;

	@Column(name = "product_sku", nullable = false, length = 100)
	private String productSku;

	@Column(name = "variant_uid", nullable = false, length = 50)
	private String variantUid;

	@Column(name = "variant_sku", nullable = false, length = 100)
	private String variantSku;

	@Column(nullable = false)
	private Integer quantity;

	@Column(name = "unit_gross_price", nullable = false, precision = 15, scale = 2)
	private BigDecimal unitGrossPrice = BigDecimal.ZERO;

	@Column(name = "vat_rate", nullable = false, precision = 5, scale = 2)
	private BigDecimal vatRate;

	@Column(name = "line_total", nullable = false, precision = 15, scale = 2)
	private BigDecimal lineTotal = BigDecimal.ZERO;

	@Column(name = "line_vat_total", nullable = false, precision = 15, scale = 2)
	private BigDecimal lineVatTotal = BigDecimal.ZERO;
}
