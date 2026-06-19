package com.backend.domain.commerce;

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
@Table(name = "commerce_order_status_history", uniqueConstraints = {
		@UniqueConstraint(columnNames = { "uuid" }, name = "uk_commerce_order_status_history_uuid"),
		@UniqueConstraint(columnNames = { "uid" }, name = "uk_commerce_order_status_history_uid")
}, indexes = {
		@Index(columnList = "order_id, created_at", name = "idx_commerce_order_status_history_order_created"),
		@Index(columnList = "to_status, created_at", name = "idx_commerce_order_status_history_to_status_created")
})
@Data
@EqualsAndHashCode(callSuper = true, exclude = { "order" })
@NoArgsConstructor
@AllArgsConstructor
public class CommerceOrderStatusHistory extends BaseEntity {

	@ToString.Exclude
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id", nullable = false)
	private CommerceOrder order;

	@Enumerated(EnumType.STRING)
	@Column(name = "from_status", nullable = false, length = 30)
	private CommerceOrderStatus fromStatus;

	@Enumerated(EnumType.STRING)
	@Column(name = "to_status", nullable = false, length = 30)
	private CommerceOrderStatus toStatus;

	@Column(name = "shipping_carrier_name", length = 100)
	private String shippingCarrierName;

	@Column(name = "shipping_tracking_number", length = 100)
	private String shippingTrackingNumber;

	@Column(name = "shipping_tracking_url", length = 500)
	private String shippingTrackingUrl;

	@Column(name = "internal_note", length = 1000)
	private String internalNote;

	@Column(name = "changed_by_user_id")
	private Long changedByUserId;

	@Column(name = "changed_by_email", length = 191)
	private String changedByEmail;
}
