package com.backend.domain.commerce;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.backend.domain.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "commerce_payment_attempts", uniqueConstraints = {
		@UniqueConstraint(columnNames = { "uuid" }, name = "uk_commerce_payment_attempt_uuid"),
		@UniqueConstraint(columnNames = { "uid" }, name = "uk_commerce_payment_attempt_uid")
}, indexes = {
		@Index(columnList = "customer_id, status, expires_at", name = "idx_commerce_payment_attempt_customer_status_expires"),
		@Index(columnList = "checkout_id, status", name = "idx_commerce_payment_attempt_checkout_status"),
		@Index(columnList = "provider, provider_reference", name = "idx_commerce_payment_attempt_provider_reference"),
		@Index(columnList = "provider, provider_transaction_id", name = "idx_commerce_payment_attempt_provider_transaction")
})
@Data
@EqualsAndHashCode(callSuper = true, exclude = { "customer", "checkout" })
@NoArgsConstructor
@AllArgsConstructor
public class CommercePaymentAttempt extends BaseEntity {

	@ToString.Exclude
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_id", nullable = false)
	private CommerceCustomer customer;

	@ToString.Exclude
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "checkout_id", nullable = false)
	private CommerceCheckout checkout;

	@Column(nullable = false, length = 40)
	private String provider;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private CommercePaymentAttemptStatus status = CommercePaymentAttemptStatus.PENDING;

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

	@Column(name = "expires_at", nullable = false)
	private LocalDateTime expiresAt;

	@Column(name = "provider_reference", length = 191)
	private String providerReference;

	@Column(name = "provider_transaction_id", length = 191)
	private String providerTransactionId;

	@Column(name = "failure_code", length = 100)
	private String failureCode;

	@Column(name = "failure_message_key", length = 191)
	private String failureMessageKey;
}
