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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.TestingAuthenticationToken;

import com.backend.application.commerce.CommerceCustomerPrincipal;
import com.backend.application.commerce.CommerceOrderReadService;
import com.backend.application.commerce.dto.CheckoutTotalsResponse;
import com.backend.application.commerce.dto.CommerceOrderFulfillmentResponse;
import com.backend.application.commerce.dto.CommerceOrderDetailResponse;
import com.backend.application.commerce.dto.CommerceOrderSummaryResponse;

@ExtendWith(MockitoExtension.class)
class CommerceOrderControllerTest {

	@Mock private CommerceOrderReadService orderReadService;
	@Mock private MessageSource messageSource;

	@Test
	void list_ShouldReturnPageableResponseAndSortConfig() {
		CommerceOrderController controller = new CommerceOrderController(orderReadService, messageSource);
		when(orderReadService.list(any(), any())).thenReturn(new PageImpl<>(List.of(summary())));
		when(messageSource.getMessage(anyString(), any(), anyString(), any(Locale.class)))
				.thenAnswer(invocation -> "Orders retrieved");

		var result = controller.list(authentication(), 0, 20, "total,desc");

		assertThat(result.getBody()).isNotNull();
		assertThat(result.getBody().getMessage()).isEqualTo("Orders retrieved");
		assertThat(result.getBody().getData().content()).hasSize(1);
		assertThat(result.getBody().getData().sortConfig().currentSort().field()).isEqualTo("total");
		assertThat(result.getBody().getData().sortConfig().currentSort().direction()).isEqualTo("desc");
		ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
		verify(orderReadService).list(any(), pageableCaptor.capture());
		assertThat(pageableCaptor.getValue().getSort().getOrderFor("total")).isNotNull();
	}

	@Test
	void get_ShouldReturnLocalizedDetailResponse() {
		CommerceOrderController controller = new CommerceOrderController(orderReadService, messageSource);
		when(orderReadService.get(any(), anyString())).thenReturn(detail());
		when(messageSource.getMessage(anyString(), any(), anyString(), any(Locale.class)))
				.thenAnswer(invocation -> "Order retrieved");

		var result = controller.get(authentication(), "order-uid");

		assertThat(result.getBody()).isNotNull();
		assertThat(result.getBody().getMessage()).isEqualTo("Order retrieved");
		assertThat(result.getBody().getData().orderUid()).isEqualTo("order-uid");
		verify(orderReadService).get(any(), anyString());
	}

	@Test
	void list_ShouldRejectInvalidSortField() {
		CommerceOrderController controller = new CommerceOrderController(orderReadService, messageSource);

		assertThatThrownBy(() -> controller.list(authentication(), 0, 20, "providerTransactionId,asc"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Invalid sort field");
	}

	@Test
	void list_ShouldRequireCommerceCustomerPrincipal() {
		CommerceOrderController controller = new CommerceOrderController(orderReadService, messageSource);
		TestingAuthenticationToken authentication = new TestingAuthenticationToken("admin", null, "ROLE_TENANT_ADMIN");

		assertThatThrownBy(() -> controller.list(authentication, 0, 20, null))
				.isInstanceOf(AccessDeniedException.class)
				.hasMessage("commerce.customer.auth.required");
	}

	private TestingAuthenticationToken authentication() {
		CommerceCustomerPrincipal principal = new CommerceCustomerPrincipal(10L, "customer-uid", "customer@example.com", 1L);
		return new TestingAuthenticationToken(principal, null, "ROLE_COMMERCE_CUSTOMER");
	}

	private CommerceOrderSummaryResponse summary() {
		return new CommerceOrderSummaryResponse(
				"order-uid",
				"ORD-20260615-000001",
				"PAID",
				LocalDateTime.of(2026, 6, 15, 12, 0),
				"TRY",
				new CheckoutTotalsResponse("TRY", BigDecimal.TEN, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.TEN),
				1);
	}

	private CommerceOrderDetailResponse detail() {
		return new CommerceOrderDetailResponse(
				"order-uid",
				"ORD-20260615-000001",
				"PAID",
				LocalDateTime.of(2026, 6, 15, 12, 0),
				"TRY",
				new CheckoutTotalsResponse("TRY", BigDecimal.TEN, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.TEN),
				0,
				List.of(),
				null,
				new CommerceOrderFulfillmentResponse(null, null, null, null, null, null),
				null,
				null,
				"NOT_CAPTURED");
	}
}
