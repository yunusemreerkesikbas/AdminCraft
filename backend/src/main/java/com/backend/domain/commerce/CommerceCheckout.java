package com.backend.domain.commerce;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.backend.domain.entity.BaseEntity;

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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "commerce_checkouts", uniqueConstraints = {
		@UniqueConstraint(columnNames = { "uuid" }, name = "uk_commerce_checkout_uuid"),
		@UniqueConstraint(columnNames = { "uid" }, name = "uk_commerce_checkout_uid")
}, indexes = {
		@Index(columnList = "customer_id, status, expires_at", name = "idx_commerce_checkout_customer_status_expires"),
		@Index(columnList = "cart_id", name = "idx_commerce_checkout_cart")
})
@Data
@EqualsAndHashCode(callSuper = true, exclude = { "customer", "cart", "items" })
@NoArgsConstructor
@AllArgsConstructor
public class CommerceCheckout extends BaseEntity {

	@ToString.Exclude
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_id", nullable = false)
	private CommerceCustomer customer;

	@ToString.Exclude
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "cart_id", nullable = false)
	private CommerceCart cart;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private CommerceCheckoutStatus status = CommerceCheckoutStatus.DRAFT;

	@Column(name = "currency_iso", nullable = false, length = 3)
	private String currencyIso;

	@Column(nullable = false, precision = 15, scale = 2)
	private BigDecimal subtotal = BigDecimal.ZERO;

	@Column(name = "vat_total", nullable = false, precision = 15, scale = 2)
	private BigDecimal vatTotal = BigDecimal.ZERO;

	@Column(name = "shipping_total", nullable = false, precision = 15, scale = 2)
	private BigDecimal shippingTotal = BigDecimal.ZERO;

	@Column(nullable = false, precision = 15, scale = 2)
	private BigDecimal total = BigDecimal.ZERO;

	@Column(name = "shipping_method_code", nullable = false, length = 50)
	private String shippingMethodCode;

	@Column(name = "shipping_method_name", nullable = false, length = 100)
	private String shippingMethodName;

	@Column(name = "delivery_address_uid", nullable = false, length = 50)
	private String deliveryAddressUid;

	@Column(name = "billing_address_uid", nullable = false, length = 50)
	private String billingAddressUid;

	@Column(name = "delivery_address_snapshot", nullable = false, columnDefinition = "JSON")
	private String deliveryAddressSnapshot;

	@Column(name = "billing_address_snapshot", nullable = false, columnDefinition = "JSON")
	private String billingAddressSnapshot;

	@Column(name = "expires_at", nullable = false)
	private LocalDateTime expiresAt;

	@ToString.Exclude
	@OneToMany(mappedBy = "checkout", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@OrderBy("id ASC")
	private List<CommerceCheckoutItem> items = new ArrayList<>();

	public void addItem(CommerceCheckoutItem item) {
		items.add(item);
		item.setCheckout(this);
	}
}
