package com.backend.domain.commerce;

import java.time.LocalDateTime;

import com.backend.domain.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "commerce_notification_outbox", uniqueConstraints = {
		@UniqueConstraint(columnNames = { "uuid" }, name = "uk_commerce_notification_outbox_uuid"),
		@UniqueConstraint(columnNames = { "uid" }, name = "uk_commerce_notification_outbox_uid")
}, indexes = {
		@Index(columnList = "status, created_at", name = "idx_commerce_notification_outbox_status_created"),
		@Index(columnList = "event_type, aggregate_uid", name = "idx_commerce_notification_outbox_event_aggregate")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class CommerceNotificationOutbox extends BaseEntity {

	@Enumerated(EnumType.STRING)
	@Column(name = "event_type", nullable = false, length = 60)
	private CommerceNotificationEventType eventType;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private CommerceNotificationChannel channel = CommerceNotificationChannel.EMAIL;

	@Column(name = "aggregate_type", nullable = false, length = 40)
	private String aggregateType;

	@Column(name = "aggregate_uid", nullable = false, length = 50)
	private String aggregateUid;

	@Column(name = "recipient_email", nullable = false, length = 255)
	private String recipientEmail;

	@Column(nullable = false, length = 10)
	private String language;

	@Column(nullable = false, length = 255)
	private String subject;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private CommerceNotificationStatus status = CommerceNotificationStatus.PENDING;

	@Column(name = "attempt_count", nullable = false)
	private int attemptCount;

	@Column(name = "last_attempted_at")
	private LocalDateTime lastAttemptedAt;

	@Column(name = "next_retry_at")
	private LocalDateTime nextRetryAt;

	@Column(name = "provider_message_id", length = 255)
	private String providerMessageId;

	@Column(name = "error_message", columnDefinition = "TEXT")
	private String errorMessage;

	@Column(name = "sent_at")
	private LocalDateTime sentAt;
}
