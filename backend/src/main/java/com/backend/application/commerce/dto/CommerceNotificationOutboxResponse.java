package com.backend.application.commerce.dto;

import java.time.LocalDateTime;

import com.backend.domain.commerce.CommerceNotificationOutbox;
import com.backend.domain.commerce.CommerceNotificationStatus;

public record CommerceNotificationOutboxResponse(
		Long id,
		String outboxUid,
		String eventType,
		String channel,
		String aggregateType,
		String aggregateUid,
		String recipientEmail,
		String language,
		String subject,
		String content,
		String status,
		int attemptCount,
		int maxRetryAttempts,
		boolean retryAllowed,
		String providerMessageId,
		String errorMessage,
		LocalDateTime lastAttemptedAt,
		LocalDateTime nextRetryAt,
		LocalDateTime sentAt,
		LocalDateTime createdAt,
		LocalDateTime updatedAt) {

	public static CommerceNotificationOutboxResponse from(
			CommerceNotificationOutbox outbox,
			int maxRetryAttempts) {
		return new CommerceNotificationOutboxResponse(
				outbox.getId(),
				outbox.getUid(),
				outbox.getEventType().name(),
				outbox.getChannel().name(),
				outbox.getAggregateType(),
				outbox.getAggregateUid(),
				outbox.getRecipientEmail(),
				outbox.getLanguage(),
				outbox.getSubject(),
				outbox.getContent(),
				outbox.getStatus().name(),
				outbox.getAttemptCount(),
				maxRetryAttempts,
				outbox.getStatus() == CommerceNotificationStatus.FAILED && outbox.getAttemptCount() <= maxRetryAttempts,
				outbox.getProviderMessageId(),
				outbox.getErrorMessage(),
				outbox.getLastAttemptedAt(),
				outbox.getNextRetryAt(),
				outbox.getSentAt(),
				outbox.getCreatedAt(),
				outbox.getUpdatedAt());
	}
}
