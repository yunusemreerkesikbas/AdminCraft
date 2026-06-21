package com.backend.application.commerce;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.backend.domain.commerce.CommerceNotificationChannel;
import com.backend.domain.commerce.CommerceNotificationEventType;
import com.backend.domain.commerce.CommerceNotificationOutbox;
import com.backend.domain.commerce.CommerceNotificationStatus;
import com.backend.domain.commerce.exception.CommerceDomainException;
import com.backend.domain.commerce.repository.CommerceNotificationOutboxRepository;
import com.backend.domain.exception.EntityNotFoundException;
import com.backend.testutil.BaseServiceTest;

class CommerceNotificationOutboxAdminServiceImplTest extends BaseServiceTest {

	@Mock private CommerceNotificationOutboxRepository outboxRepository;
	@Mock private CommerceNotificationDispatchService dispatchService;
	@Mock private CommerceModuleAccessGuard commerceModuleAccessGuard;

	private CommerceNotificationOutboxAdminServiceImpl service;

	@BeforeEach
	void setUp() {
		service = new CommerceNotificationOutboxAdminServiceImpl(
				outboxRepository,
				dispatchService,
				new CommerceNotificationProperties(),
				commerceModuleAccessGuard);
	}

	@Test
	void listOutbox_ShouldNormalizeFiltersAndMapRetryAllowed() {
		CommerceNotificationOutbox outbox = outbox(CommerceNotificationStatus.FAILED, 2);
		when(outboxRepository.findAdminOutbox(
				"jane",
				CommerceNotificationStatus.FAILED,
				CommerceNotificationEventType.ORDER_PAID,
				"order-uid",
				PageRequest.of(0, 20)))
				.thenReturn(new PageImpl<>(List.of(outbox)));

		var response = service.listOutbox(
						PageRequest.of(0, 20),
						" Jane ",
						CommerceNotificationStatus.FAILED,
						CommerceNotificationEventType.ORDER_PAID,
						" order-uid ")
				.getContent()
				.getFirst();

		assertThat(response.outboxUid()).isEqualTo("outbox-uid");
		assertThat(response.retryAllowed()).isTrue();
		verify(commerceModuleAccessGuard).assertEnabledForCurrentTenant();
	}

	@Test
	void getOutbox_ShouldThrowNotFound_WhenMissing() {
		when(outboxRepository.findByUid("missing")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.getOutbox("missing"))
				.isInstanceOf(EntityNotFoundException.class)
				.hasMessage("commerce.notification.outbox.not.found");
	}

	@Test
	void retry_ShouldDispatchFailedOutbox() {
		CommerceNotificationOutbox outbox = outbox(CommerceNotificationStatus.FAILED, 1);
		CommerceNotificationOutbox sent = outbox(CommerceNotificationStatus.SENT, 2);
		when(outboxRepository.findByUid("outbox-uid")).thenReturn(Optional.of(outbox));
		when(dispatchService.dispatch(99L)).thenReturn(sent);

		var response = service.retry("outbox-uid");

		assertThat(response.status()).isEqualTo("SENT");
		verify(dispatchService).dispatch(99L);
	}

	@Test
	void retry_ShouldRejectNonFailedOutbox() {
		when(outboxRepository.findByUid("outbox-uid")).thenReturn(Optional.of(outbox(CommerceNotificationStatus.SENT, 1)));

		assertThatThrownBy(() -> service.retry("outbox-uid"))
				.isInstanceOf(CommerceDomainException.class)
				.hasMessage("commerce.notification.outbox.retry.status.invalid");
	}

	@Test
	void retry_ShouldRejectOutboxAfterRetryLimit() {
		when(outboxRepository.findByUid("outbox-uid")).thenReturn(Optional.of(outbox(CommerceNotificationStatus.FAILED, 4)));

		assertThatThrownBy(() -> service.retry("outbox-uid"))
				.isInstanceOf(CommerceDomainException.class)
				.hasMessage("commerce.notification.outbox.retry.limit.exceeded");
	}

	@Test
	void retryDueNotificationsForCurrentTenant_ShouldDispatchDueRows() {
		CommerceNotificationOutbox outbox = outbox(CommerceNotificationStatus.FAILED, 1);
		when(outboxRepository.findDueRetries(anyInt(), any(), any())).thenReturn(new PageImpl<>(List.of(outbox)));
		when(dispatchService.dispatch(99L)).thenReturn(outbox);

		int retried = service.retryDueNotificationsForCurrentTenant();

		assertThat(retried).isEqualTo(1);
		verify(dispatchService).dispatch(99L);
	}

	@Test
	void retryDueNotificationsForCurrentTenant_ShouldContinue_WhenOneDispatchFails() {
		CommerceNotificationOutbox first = outbox(CommerceNotificationStatus.FAILED, 1);
		CommerceNotificationOutbox second = outbox(CommerceNotificationStatus.FAILED, 1);
		second.setId(100L);
		second.setUid("outbox-uid-2");
		when(outboxRepository.findDueRetries(anyInt(), any(), any())).thenReturn(new PageImpl<>(List.of(first, second)));
		doThrow(new RuntimeException("provider down")).when(dispatchService).dispatch(99L);
		when(dispatchService.dispatch(100L)).thenReturn(second);

		int retried = service.retryDueNotificationsForCurrentTenant();

		assertThat(retried).isEqualTo(2);
		verify(dispatchService).dispatch(99L);
		verify(dispatchService).dispatch(100L);
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
