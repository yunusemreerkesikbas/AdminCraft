package com.backend.application.commerce;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.backend.application.dto.email.EmailResult;
import com.backend.domain.commerce.CommerceNotificationOutbox;
import com.backend.domain.commerce.CommerceNotificationStatus;
import com.backend.domain.commerce.repository.CommerceNotificationOutboxRepository;
import com.backend.domain.port.MailSenderPort;
import com.backend.shared.common.LogSanitizer;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CommerceNotificationDispatchService {

	private final CommerceNotificationOutboxRepository outboxRepository;
	private final MailSenderPort mailSender;
	private final CommerceNotificationProperties properties;
	private final PlatformTransactionManager tenantTransactionManager;

	public CommerceNotificationDispatchService(
			CommerceNotificationOutboxRepository outboxRepository,
			MailSenderPort mailSender,
			CommerceNotificationProperties properties,
			@Qualifier("tenantTransactionManager") PlatformTransactionManager tenantTransactionManager) {
		this.outboxRepository = outboxRepository;
		this.mailSender = mailSender;
		this.properties = properties;
		this.tenantTransactionManager = tenantTransactionManager;
	}

	public void dispatchAfterCommit(Long outboxId) {
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					try {
						dispatch(outboxId);
					} catch (RuntimeException ex) {
						log.warn(
								"Commerce notification dispatch failed outboxId={} reason={}",
								outboxId,
								LogSanitizer.sanitizeForLog(ex.getMessage()));
					}
				}
			});
			return;
		}
		dispatch(outboxId);
	}

	public CommerceNotificationOutbox dispatch(Long outboxId) {
		return newRequiresNewTransactionTemplate().execute(status -> {
			CommerceNotificationOutbox outbox = outboxRepository.findByIdForUpdate(outboxId).orElse(null);
			if (outbox == null) {
				return null;
			}
			if (!isDispatchable(outbox)) {
				return outbox;
			}
			EmailResult result;
			try {
				result = mailSender.send(outbox.getRecipientEmail(), outbox.getSubject(), outbox.getContent());
			} catch (RuntimeException ex) {
				result = EmailResult.failure(LogSanitizer.sanitizeForLog(ex.getMessage()));
			}
			return applyResult(outbox, result);
		});
	}

	private boolean isDispatchable(CommerceNotificationOutbox outbox) {
		if (outbox.getStatus() == CommerceNotificationStatus.SENT) {
			return false;
		}
		return outbox.getStatus() != CommerceNotificationStatus.FAILED
				|| outbox.getAttemptCount() <= properties.getMaxRetryAttempts();
	}

	private CommerceNotificationOutbox applyResult(CommerceNotificationOutbox outbox, EmailResult result) {
		LocalDateTime now = LocalDateTime.now();
		outbox.setAttemptCount(outbox.getAttemptCount() + 1);
		outbox.setLastAttemptedAt(now);
		if (result.isSuccess()) {
			outbox.setStatus(CommerceNotificationStatus.SENT);
			outbox.setProviderMessageId(result.getMessageId());
			outbox.setErrorMessage(null);
			outbox.setNextRetryAt(null);
			outbox.setSentAt(now);
		} else {
			outbox.setStatus(CommerceNotificationStatus.FAILED);
			outbox.setProviderMessageId(null);
			outbox.setErrorMessage(LogSanitizer.sanitizeForLog(result.getErrorMessage()));
			outbox.setNextRetryAt(outbox.getAttemptCount() <= properties.getMaxRetryAttempts()
					? now.plus(properties.getRetryDelay())
					: null);
		}
		return outboxRepository.save(outbox);
	}

	private TransactionTemplate newRequiresNewTransactionTemplate() {
		TransactionTemplate transactionTemplate = new TransactionTemplate(tenantTransactionManager);
		transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		return transactionTemplate;
	}
}
