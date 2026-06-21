package com.backend.application.commerce;

import java.time.LocalDateTime;
import java.util.Locale;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import com.backend.application.commerce.dto.CommerceNotificationOutboxResponse;
import com.backend.domain.commerce.CommerceNotificationEventType;
import com.backend.domain.commerce.CommerceNotificationOutbox;
import com.backend.domain.commerce.CommerceNotificationStatus;
import com.backend.domain.commerce.exception.CommerceDomainException;
import com.backend.domain.commerce.repository.CommerceNotificationOutboxRepository;
import com.backend.domain.exception.EntityNotFoundException;
import com.backend.shared.common.LogSanitizer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Validated
@RequiredArgsConstructor
@Slf4j
class CommerceNotificationOutboxAdminServiceImpl implements CommerceNotificationOutboxAdminService {

	private static final String OUTBOX_NOT_FOUND = "commerce.notification.outbox.not.found";
	private static final String RETRY_STATUS_INVALID = "commerce.notification.outbox.retry.status.invalid";
	private static final String RETRY_LIMIT_EXCEEDED = "commerce.notification.outbox.retry.limit.exceeded";

	private final CommerceNotificationOutboxRepository outboxRepository;
	private final CommerceNotificationDispatchService dispatchService;
	private final CommerceNotificationProperties properties;
	private final CommerceModuleAccessGuard commerceModuleAccessGuard;

	@Override
	@Transactional(readOnly = true)
	public Page<CommerceNotificationOutboxResponse> listOutbox(
			Pageable pageable,
			String search,
			CommerceNotificationStatus status,
			CommerceNotificationEventType eventType,
			String aggregateUid) {
		commerceModuleAccessGuard.assertEnabledForCurrentTenant();
		return outboxRepository.findAdminOutbox(
						normalize(search),
						status,
						eventType,
						normalizeExact(aggregateUid),
						pageable)
				.map(this::toResponse);
	}

	@Override
	@Transactional(readOnly = true)
	public CommerceNotificationOutboxResponse getOutbox(String outboxUid) {
		commerceModuleAccessGuard.assertEnabledForCurrentTenant();
		return toResponse(findByUid(outboxUid));
	}

	@Override
	public CommerceNotificationOutboxResponse retry(String outboxUid) {
		commerceModuleAccessGuard.assertEnabledForCurrentTenant();
		CommerceNotificationOutbox outbox = findByUid(outboxUid);
		assertRetryAllowed(outbox);
		return toResponse(dispatchService.dispatch(outbox.getId()));
	}

	@Override
	public int retryDueNotificationsForCurrentTenant() {
		commerceModuleAccessGuard.assertEnabledForCurrentTenant();
		Page<CommerceNotificationOutbox> dueRetries = outboxRepository.findDueRetries(
				properties.getMaxRetryAttempts(),
				LocalDateTime.now(),
				PageRequest.of(0, properties.getRetryBatchSize()));
		dueRetries.forEach(outbox -> {
			try {
				dispatchService.dispatch(outbox.getId());
			} catch (RuntimeException ex) {
				log.warn(
						"Commerce notification retry failed outboxUid={} outboxId={} reason={}",
						outbox.getUid(),
						outbox.getId(),
						LogSanitizer.sanitizeForLog(ex.getMessage()));
			}
		});
		return dueRetries.getNumberOfElements();
	}

	private CommerceNotificationOutbox findByUid(String outboxUid) {
		return outboxRepository.findByUid(outboxUid)
				.orElseThrow(() -> new EntityNotFoundException(OUTBOX_NOT_FOUND));
	}

	private void assertRetryAllowed(CommerceNotificationOutbox outbox) {
		if (outbox.getStatus() != CommerceNotificationStatus.FAILED) {
			throw new CommerceDomainException(RETRY_STATUS_INVALID);
		}
		if (outbox.getAttemptCount() > properties.getMaxRetryAttempts()) {
			throw new CommerceDomainException(RETRY_LIMIT_EXCEEDED);
		}
	}

	private CommerceNotificationOutboxResponse toResponse(CommerceNotificationOutbox outbox) {
		return CommerceNotificationOutboxResponse.from(outbox, properties.getMaxRetryAttempts());
	}

	private String normalize(String search) {
		return StringUtils.hasText(search) ? search.trim().toLowerCase(Locale.ROOT) : null;
	}

	private String normalizeExact(String value) {
		return StringUtils.hasText(value) ? value.trim() : null;
	}
}
