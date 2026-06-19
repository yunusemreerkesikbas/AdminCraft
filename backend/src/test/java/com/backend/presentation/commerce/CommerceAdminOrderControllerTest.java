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
import com.backend.application.commerce.dto.ChangeCommerceOrderStatusCommand;
import com.backend.application.commerce.dto.CheckoutTotalsResponse;
import com.backend.application.commerce.dto.CommerceAdminDashboardResponse;
import com.backend.application.commerce.dto.CommerceAdminMetricResponse;
import com.backend.application.commerce.dto.CommerceAdminOrderSummaryResponse;
import com.backend.domain.commerce.CommerceOrderStatus;

@ExtendWith(MockitoExtension.class)
class CommerceAdminOrderControllerTest {

	@Mock private CommerceAdminOrderService adminOrderService;
	@Mock private MessageSource messageSource;

	@Test
	void dashboard_ShouldReturnAdminDashboard() {
		CommerceAdminOrderController controller = new CommerceAdminOrderController(adminOrderService, messageSource);
		when(adminOrderService.dashboard()).thenReturn(new CommerceAdminDashboardResponse(
				new CommerceAdminMetricResponse(1, BigDecimal.TEN, "TRY"),
				new CommerceAdminMetricResponse(3, BigDecimal.valueOf(30), "TRY"),
				0,
				1,
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
}
