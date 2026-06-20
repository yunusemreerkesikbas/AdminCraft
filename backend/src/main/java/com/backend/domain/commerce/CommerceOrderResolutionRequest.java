package com.backend.domain.commerce;

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
@Table(name = "commerce_order_resolution_requests", uniqueConstraints = {
		@UniqueConstraint(columnNames = { "uuid" }, name = "uk_commerce_order_resolution_request_uuid"),
		@UniqueConstraint(columnNames = { "uid" }, name = "uk_commerce_order_resolution_request_uid")
}, indexes = {
		@Index(columnList = "order_id, status", name = "idx_commerce_order_resolution_request_order_status"),
		@Index(columnList = "customer_id, created_at", name = "idx_commerce_order_resolution_request_customer_created"),
		@Index(columnList = "status, created_at", name = "idx_commerce_order_resolution_request_status_created"),
		@Index(columnList = "request_type, status, created_at", name = "idx_commerce_order_resolution_request_type_status_created"),
		@Index(columnList = "refund_status, created_at", name = "idx_commerce_order_resolution_request_refund_status")
})
@Data
@EqualsAndHashCode(callSuper = true, exclude = { "order", "customer" })
@NoArgsConstructor
@AllArgsConstructor
public class CommerceOrderResolutionRequest extends BaseEntity {

	@ToString.Exclude
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id", nullable = false)
	private CommerceOrder order;

	@ToString.Exclude
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_id", nullable = false)
	private CommerceCustomer customer;

	@Enumerated(EnumType.STRING)
	@Column(name = "request_type", nullable = false, length = 30)
	private CommerceOrderResolutionRequestType type;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private CommerceOrderResolutionRequestStatus status = CommerceOrderResolutionRequestStatus.PENDING;

	@Column(nullable = false, length = 100)
	private String reason;

	@Column(nullable = false, length = 1000)
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(name = "previous_order_status", nullable = false, length = 30)
	private CommerceOrderStatus previousOrderStatus;

	@Enumerated(EnumType.STRING)
	@Column(name = "requested_order_status", nullable = false, length = 30)
	private CommerceOrderStatus requestedOrderStatus;

	@Column(name = "decision_note", length = 1000)
	private String decisionNote;

	@Column(name = "decided_by_user_id")
	private Long decidedByUserId;

	@Column(name = "decided_by_email", length = 191)
	private String decidedByEmail;

	@Column(name = "decided_at")
	private LocalDateTime decidedAt;

	@Enumerated(EnumType.STRING)
	@Column(name = "refund_status", nullable = false, length = 30)
	private CommerceOrderResolutionRefundStatus refundStatus = CommerceOrderResolutionRefundStatus.NOT_ATTEMPTED;

	@Column(name = "refund_provider", length = 40)
	private String refundProvider;

	@Column(name = "refund_reference", length = 191)
	private String refundReference;

	@Column(name = "refund_failure_code", length = 100)
	private String refundFailureCode;

	@Column(name = "refund_failure_message_key", length = 191)
	private String refundFailureMessageKey;

	@Column(name = "refund_attempted_at")
	private LocalDateTime refundAttemptedAt;

	@Column(name = "refunded_at")
	private LocalDateTime refundedAt;

	@Column(name = "stock_restored", nullable = false)
	private boolean stockRestored = false;
}
