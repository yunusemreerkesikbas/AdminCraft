package com.backend.domain.commerce;

import java.math.BigDecimal;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "commerce_orders", uniqueConstraints = {
		@UniqueConstraint(columnNames = { "uuid" }, name = "uk_commerce_order_uuid"),
		@UniqueConstraint(columnNames = { "uid" }, name = "uk_commerce_order_uid"),
		@UniqueConstraint(columnNames = { "order_number" }, name = "uk_commerce_order_number"),
		@UniqueConstraint(columnNames = { "checkout_id" }, name = "uk_commerce_order_checkout"),
		@UniqueConstraint(columnNames = { "payment_attempt_id" }, name = "uk_commerce_order_payment_attempt")
}, indexes = {
		@Index(columnList = "customer_id, status, created_at", name = "idx_commerce_order_customer_status_created"),
		@Index(columnList = "requires_attention", name = "idx_commerce_order_requires_attention")
})
@Data
@EqualsAndHashCode(callSuper = true, exclude = { "customer", "checkout", "paymentAttempt", "items" })
@NoArgsConstructor
@AllArgsConstructor
public class CommerceOrder extends BaseEntity {

	@Column(name = "order_number", nullable = false, length = 40)
	private String orderNumber;

	@ToString.Exclude
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_id", nullable = false)
	private CommerceCustomer customer;

	@ToString.Exclude
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "checkout_id", nullable = false)
	private CommerceCheckout checkout;

	@ToString.Exclude
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "payment_attempt_id", nullable = false)
	private CommercePaymentAttempt paymentAttempt;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private CommerceOrderStatus status = CommerceOrderStatus.PAID;

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

	@Column(nullable = false, length = 40)
	private String provider;

	@Column(name = "provider_transaction_id", length = 191)
	private String providerTransactionId;

	@Enumerated(EnumType.STRING)
	@Column(name = "legal_snapshot_status", nullable = false, length = 30)
	private CommerceOrderLegalSnapshotStatus legalSnapshotStatus = CommerceOrderLegalSnapshotStatus.NOT_CAPTURED;

	@Column(name = "legal_snapshot_json", columnDefinition = "JSON")
	private String legalSnapshotJson;

	@Column(name = "stock_deducted", nullable = false)
	private boolean stockDeducted = false;

	@Column(name = "requires_attention", nullable = false)
	private boolean requiresAttention = false;

	@Column(name = "attention_reason_key", length = 191)
	private String attentionReasonKey;

	@ToString.Exclude
	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@OrderBy("id ASC")
	private List<CommerceOrderItem> items = new ArrayList<>();

	public void addItem(CommerceOrderItem item) {
		items.add(item);
		item.setOrder(this);
	}
}
