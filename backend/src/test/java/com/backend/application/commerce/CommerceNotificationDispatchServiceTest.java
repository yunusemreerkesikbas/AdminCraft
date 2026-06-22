package com.backend.application.commerce;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;

import com.backend.application.dto.email.EmailResult;
import com.backend.domain.commerce.CommerceNotificationChannel;
import com.backend.domain.commerce.CommerceNotificationEventType;
import com.backend.domain.commerce.CommerceNotificationOutbox;
import com.backend.domain.commerce.CommerceNotificationStatus;
import com.backend.domain.commerce.repository.CommerceNotificationOutboxRepository;
import com.backend.domain.port.MailSenderPort;
import com.backend.domain.port.SmsSenderPort;
import com.backend.domain.sms.SmsResult;
import com.backend.testutil.BaseServiceTest;

class CommerceNotificationDispatchServiceTest extends BaseServiceTest {

	@Mock private CommerceNotificationOutboxRepository outboxRepository;
	@Mock private MailSenderPort mailSender;
	@Mock private SmsSenderPort smsSender;
	@Mock private PlatformTransactionManager transactionManager;

	private final AtomicReference<CommerceNotificationOutbox> savedOutbox = new AtomicReference<>();
	private CommerceNotificationDispatchService service;

	@BeforeEach
	void setUp() {
		CommerceNotificationProperties properties = new CommerceNotificationProperties();
		service = new CommerceNotificationDispatchService(outboxRepository, mailSender, smsSender, properties, transactionManager);
		lenient().when(transactionManager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
		lenient().doNothing().when(transactionManager).commit(any());
		lenient().doNothing().when(transactionManager).rollback(any());
		lenient().when(outboxRepository.findByIdForUpdate(99L)).thenAnswer(invocation -> Optional.of(savedOutbox.get()));
		lenient().when(outboxRepository.save(any())).thenAnswer(invocation -> {
			CommerceNotificationOutbox outbox = invocation.getArgument(0);
			savedOutbox.set(outbox);
			return outbox;
		});
	}

	@Test
	void dispatch_ShouldUseSmsSender_WhenChannelIsSms() {
		CommerceNotificationOutbox outbox = outbox(CommerceNotificationStatus.PENDING, 0);
		outbox.setChannel(CommerceNotificationChannel.SMS);
		outbox.setRecipientEmail(null);
		outbox.setRecipientPhone("905551112233");
		savedOutbox.set(outbox);
		when(smsSender.send("905551112233", "Content")).thenReturn(SmsResult.success("sms-1"));

		CommerceNotificationOutbox result = service.dispatch(99L);

		assertThat(result.getStatus()).isEqualTo(CommerceNotificationStatus.SENT);
		assertThat(result.getProviderMessageId()).isEqualTo("sms-1");
		verify(mailSender, never()).send(any(), any(), any());
	}

	@Test
	void dispatch_ShouldMarkSmsFailedAndScheduleRetry_WhenSmsProviderFails() {
		CommerceNotificationOutbox outbox = outbox(CommerceNotificationStatus.PENDING, 0);
		outbox.setChannel(CommerceNotificationChannel.SMS);
		outbox.setRecipientEmail(null);
		outbox.setRecipientPhone("905551112233");
		savedOutbox.set(outbox);
		when(smsSender.send("905551112233", "Content")).thenReturn(SmsResult.failure("sms provider down"));

		CommerceNotificationOutbox result = service.dispatch(99L);

		assertThat(result.getStatus()).isEqualTo(CommerceNotificationStatus.FAILED);
		assertThat(result.getAttemptCount()).isEqualTo(1);
		assertThat(result.getNextRetryAt()).isNotNull();
		assertThat(result.getErrorMessage()).isEqualTo("sms provider down");
		verify(smsSender, times(1)).send("905551112233", "Content");
		verify(mailSender, never()).send(any(), any(), any());
	}

	@Test
	void dispatch_ShouldMarkFailedAndScheduleRetry_WhenProviderFails() {
		savedOutbox.set(outbox(CommerceNotificationStatus.PENDING, 0));
		when(mailSender.send("jane@example.com", "Subject", "Content")).thenReturn(EmailResult.failure("provider down"));

		CommerceNotificationOutbox result = service.dispatch(99L);

		assertThat(result.getStatus()).isEqualTo(CommerceNotificationStatus.FAILED);
		assertThat(result.getAttemptCount()).isEqualTo(1);
		assertThat(result.getLastAttemptedAt()).isNotNull();
		assertThat(result.getNextRetryAt()).isNotNull();
		assertThat(result.getErrorMessage()).isEqualTo("provider down");
	}

	@Test
	void dispatch_ShouldMarkSentAndClearRetryFields_WhenRetrySucceeds() {
		CommerceNotificationOutbox outbox = outbox(CommerceNotificationStatus.FAILED, 1);
		outbox.setNextRetryAt(java.time.LocalDateTime.now().minusMinutes(1));
		savedOutbox.set(outbox);
		when(mailSender.send("jane@example.com", "Subject", "Content")).thenReturn(EmailResult.success("message-1"));

		CommerceNotificationOutbox result = service.dispatch(99L);

		assertThat(result.getStatus()).isEqualTo(CommerceNotificationStatus.SENT);
		assertThat(result.getAttemptCount()).isEqualTo(2);
		assertThat(result.getProviderMessageId()).isEqualTo("message-1");
		assertThat(result.getNextRetryAt()).isNull();
		assertThat(result.getSentAt()).isNotNull();
	}

	@Test
	void dispatch_ShouldStopScheduling_WhenRetryLimitReachedAfterFailure() {
		savedOutbox.set(outbox(CommerceNotificationStatus.FAILED, 3));
		when(mailSender.send("jane@example.com", "Subject", "Content")).thenReturn(EmailResult.failure("still down"));

		CommerceNotificationOutbox result = service.dispatch(99L);

		assertThat(result.getStatus()).isEqualTo(CommerceNotificationStatus.FAILED);
		assertThat(result.getAttemptCount()).isEqualTo(4);
		assertThat(result.getNextRetryAt()).isNull();
	}

	@Test
	void dispatch_ShouldSkipProvider_WhenAlreadySent() {
		savedOutbox.set(outbox(CommerceNotificationStatus.SENT, 1));

		CommerceNotificationOutbox result = service.dispatch(99L);

		assertThat(result.getStatus()).isEqualTo(CommerceNotificationStatus.SENT);
		assertThat(result.getAttemptCount()).isEqualTo(1);
		verify(mailSender, never()).send(any(), any(), any());
	}

	@Test
	void dispatch_ShouldSkipProvider_WhenRetryLimitAlreadyExceeded() {
		savedOutbox.set(outbox(CommerceNotificationStatus.FAILED, 4));

		CommerceNotificationOutbox result = service.dispatch(99L);

		assertThat(result.getStatus()).isEqualTo(CommerceNotificationStatus.FAILED);
		assertThat(result.getAttemptCount()).isEqualTo(4);
		verify(mailSender, never()).send(any(), any(), any());
	}

	private CommerceNotificationOutbox outbox(CommerceNotificationStatus status, int attemptCount) {
		CommerceNotificationOutbox outbox = new CommerceNotificationOutbox();
		outbox.setId(99L);
		outbox.setUid("outbox-uid");
		outbox.setEventType(CommerceNotificationEventType.ORDER_PAID);
		outbox.setChannel(CommerceNotificationChannel.EMAIL);
		outbox.setAggregateType("ORDER");
		outbox.setAggregateUid("order-uid");
		outbox.setRecipientEmail("jane@example.com");
		outbox.setLanguage("EN");
		outbox.setSubject("Subject");
		outbox.setContent("Content");
		outbox.setStatus(status);
		outbox.setAttemptCount(attemptCount);
		return outbox;
	}
}
