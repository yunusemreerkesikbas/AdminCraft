package com.backend.presentation.commerce;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.backend.application.commerce.CommerceAdminOrderService;
import com.backend.application.commerce.CommerceNotificationOutboxAdminService;
import com.backend.application.commerce.dto.ChangeCommerceOrderStatusCommand;
import com.backend.application.commerce.dto.CheckoutTotalsResponse;
import com.backend.application.commerce.dto.CommerceAdminDashboardResponse;
import com.backend.application.commerce.dto.CommerceAdminMetricResponse;
import com.backend.application.commerce.dto.CommerceAdminOrderSummaryResponse;
import com.backend.application.commerce.dto.CommerceNotificationOutboxResponse;
import com.backend.domain.commerce.CommerceNotificationStatus;
import com.backend.domain.commerce.CommerceOrderStatus;

@ExtendWith(MockitoExtension.class)
class CommerceAdminOrderControllerTest {

	@Mock private CommerceAdminOrderService adminOrderService;
	@Mock private CommerceNotificationOutboxAdminService notificationOutboxAdminService;
	@Mock private MessageSource messageSource;

	@Test
	void dashboard_ShouldReturnAdminDashboard() {
		CommerceAdminOrderController controller = new CommerceAdminOrderController(adminOrderService, messageSource);
		when(adminOrderService.dashboard()).thenReturn(new CommerceAdminDashboardResponse(
				new CommerceAdminMetricResponse(1, BigDecimal.TEN, "TRY"),
				new CommerceAdminMetricResponse(3, BigDecimal.valueOf(30), "TRY"),
				0,
				1,
				2,
				"TRY"));
		when(messageSource.getMessage(anyString(), any(), anyString(), any(Locale.class)))
				.thenAnswer(invocation -> "Dashboard retrieved");

		var result = controller.dashboard();

		assertThat(result.getBody()).isNotNull();
		assertThat(result.getBody().getMessage()).isEqualTo("Dashboard retrieved");
		assertThat(result.getBody().getData().today().orderCount()).isEqualTo(1);
	}

	@Test
	void listOrders_ShouldReturnPageableResponseAndSortConfig() {
		CommerceAdminOrderController controller = new CommerceAdminOrderController(adminOrderService, messageSource);
		when(adminOrderService.listOrders(any(), any(), any(), any())).thenReturn(new PageImpl<>(List.of(orderSummary())));
		when(messageSource.getMessage(anyString(), any(), anyString(), any(Locale.class)))
				.thenAnswer(invocation -> "Orders retrieved");

		var result = controller.listOrders(0, 20, "total,desc", "jane", null, null);

		assertThat(result.getBody()).isNotNull();
		assertThat(result.getBody().getData().content()).hasSize(1);
		assertThat(result.getBody().getData().sortConfig().currentSort().field()).isEqualTo("total");
		ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
		verify(adminOrderService).listOrders(pageableCaptor.capture(), any(), any(), any());
		assertThat(pageableCaptor.getValue().getSort().getOrderFor("total")).isNotNull();
	}

	@Test
	void listOrders_ShouldRejectInvalidSortField() {
		CommerceAdminOrderController controller = new CommerceAdminOrderController(adminOrderService, messageSource);

		assertThatThrownBy(() -> controller.listOrders(0, 20, "providerTransactionId,asc", null, null, null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Invalid sort field");
	}

	@Test
	void listPaymentAttempts_ShouldRejectInvalidSortField() {
		CommerceAdminOrderController controller = new CommerceAdminOrderController(adminOrderService, messageSource);

		assertThatThrownBy(() -> controller.listPaymentAttempts(0, 20, "providerReference,asc", null, null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Invalid sort field");
	}

	@Test
	void listNotificationOutbox_ShouldReturnPageableResponseAndSortConfig() {
		CommerceAdminOrderController controller = new CommerceAdminOrderController(
				adminOrderService,
				null,
				notificationOutboxAdminService,
				messageSource);
		when(notificationOutboxAdminService.listOutbox(any(), any(), any(), any(), any()))
				.thenReturn(new PageImpl<>(List.of(notificationOutbox())));
		when(messageSource.getMessage(anyString(), any(), anyString(), any(Locale.class)))
				.thenAnswer(invocation -> "Outbox retrieved");

		var result = controller.listNotificationOutbox(
				0,
				20,
				"eventType,asc",
				"jane",
				CommerceNotificationStatus.FAILED,
				null,
				null);

		assertThat(result.getBody()).isNotNull();
		assertThat(result.getBody().getMessage()).isEqualTo("Outbox retrieved");
		assertThat(result.getBody().getData().content()).hasSize(1);
		assertThat(result.getBody().getData().sortConfig().currentSort().field()).isEqualTo("eventType");
		verify(notificationOutboxAdminService).listOutbox(any(), any(), any(), any(), any());
	}

	@Test
	void listNotificationOutbox_ShouldRejectInvalidSortField() {
		CommerceAdminOrderController controller = new CommerceAdminOrderController(
				adminOrderService,
				null,
				notificationOutboxAdminService,
				messageSource);

		assertThatThrownBy(() -> controller.listNotificationOutbox(0, 20, "recipientEmail,asc", null, null, null, null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Invalid sort field");
	}

	@Test
	void retryNotificationOutbox_ShouldPassUidToService() {
		CommerceAdminOrderController controller = new CommerceAdminOrderController(
				adminOrderService,
				null,
				notificationOutboxAdminService,
				messageSource);
		when(notificationOutboxAdminService.retry("outbox-uid")).thenReturn(notificationOutbox());
		when(messageSource.getMessage(anyString(), any(), anyString(), any(Locale.class)))
				.thenAnswer(invocation -> "Retry completed");

		var result = controller.retryNotificationOutbox("outbox-uid");

		assertThat(result.getBody()).isNotNull();
		assertThat(result.getBody().getMessage()).isEqualTo("Retry completed");
		verify(notificationOutboxAdminService).retry("outbox-uid");
	}

	@Test
	void getNotificationOutbox_ShouldReturnOutbox() {
		CommerceAdminOrderController controller = new CommerceAdminOrderController(
				adminOrderService,
				null,
				notificationOutboxAdminService,
				messageSource);
		CommerceNotificationOutboxResponse outbox = notificationOutbox();
		when(notificationOutboxAdminService.getOutbox("outbox-uid")).thenReturn(outbox);
		when(messageSource.getMessage(anyString(), any(), anyString(), any(Locale.class)))
				.thenAnswer(invocation -> "Outbox detail retrieved");

		var result = controller.getNotificationOutbox("outbox-uid");

		assertThat(result.getBody()).isNotNull();
		assertThat(result.getBody().getMessage()).isEqualTo("Outbox detail retrieved");
		assertThat(result.getBody().getData()).isEqualTo(outbox);
		verify(notificationOutboxAdminService).getOutbox("outbox-uid");
	}

	@Test
	void changeOrderStatus_ShouldPassCommandToServiceAndReturnMessage() {
		CommerceAdminOrderController controller = new CommerceAdminOrderController(adminOrderService, messageSource);
		when(messageSource.getMessage(anyString(), any(), anyString(), any(Locale.class)))
				.thenAnswer(invocation -> "Status updated");

		var result = controller.changeOrderStatus(
				"order-uid",
				new ChangeCommerceOrderStatusRequest(
						CommerceOrderStatus.SHIPPED,
						"Carrier",
						"TRK-1",
						"https://tracking.example/TRK-1",
						"Packed"));

		assertThat(result.getBody()).isNotNull();
		assertThat(result.getBody().getMessage()).isEqualTo("Status updated");
		ArgumentCaptor<ChangeCommerceOrderStatusCommand> commandCaptor =
				ArgumentCaptor.forClass(ChangeCommerceOrderStatusCommand.class);
		verify(adminOrderService).changeStatus(org.mockito.ArgumentMatchers.eq("order-uid"), commandCaptor.capture());
		assertThat(commandCaptor.getValue().status()).isEqualTo(CommerceOrderStatus.SHIPPED);
		assertThat(commandCaptor.getValue().carrierName()).isEqualTo("Carrier");
		assertThat(commandCaptor.getValue().trackingNumber()).isEqualTo("TRK-1");
		assertThat(commandCaptor.getValue().trackingUrl()).isEqualTo("https://tracking.example/TRK-1");
		assertThat(commandCaptor.getValue().internalNote()).isEqualTo("Packed");
	}

	private CommerceAdminOrderSummaryResponse orderSummary() {
		return new CommerceAdminOrderSummaryResponse(
				1L,
				"order-uid",
				"ORD-20260615-000001",
				"customer-uid",
				"Jane Doe",
				"jane@example.com",
				"PAID",
				LocalDateTime.of(2026, 6, 15, 12, 0),
				"TRY",
				new CheckoutTotalsResponse("TRY", BigDecimal.TEN, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.TEN),
				1,
				"iyzico",
				false,
				null);
	}

	private CommerceNotificationOutboxResponse notificationOutbox() {
		return new CommerceNotificationOutboxResponse(
				1L,
				"outbox-uid",
				"ORDER_PAID",
				"EMAIL",
				"ORDER",
				"order-uid",
				"jane@example.com",
				"EN",
				"Subject",
				"Content",
				"FAILED",
				1,
				3,
				true,
				null,
				"provider down",
				LocalDateTime.of(2026, 6, 15, 12, 5),
				LocalDateTime.of(2026, 6, 15, 12, 20),
				null,
				LocalDateTime.of(2026, 6, 15, 12, 0),
				LocalDateTime.of(2026, 6, 15, 12, 5));
	}
}
