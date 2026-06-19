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
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.backend.application.commerce.CommerceOrderResolutionRequestService;
import com.backend.application.commerce.dto.CommerceOrderResolutionDecisionCommand;
import com.backend.application.commerce.dto.CommerceOrderResolutionRequestResponse;
import com.backend.application.commerce.dto.CreateCommerceOrderResolutionRequestCommand;
import com.backend.application.commerce.dto.CustomerOrderResolutionRequestResponse;
import com.backend.domain.commerce.CommerceOrderResolutionRequestStatus;
import com.backend.domain.commerce.CommerceOrderResolutionRequestType;

@ExtendWith(MockitoExtension.class)
class CommerceOrderResolutionRequestControllerTest {

	@Mock private CommerceOrderResolutionRequestService requestService;
	@Mock private MessageSource messageSource;
	private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

	@Test
	void createCustomerRequest_ShouldPassCommercePrincipalAndCommand() {
		CommerceOrderController controller = new CommerceOrderController(null, requestService, messageSource);
		when(requestService.createCustomerRequest(any(), anyString(), any())).thenReturn(customerResponse());
		when(messageSource.getMessage(anyString(), any(), anyString(), any(Locale.class)))
				.thenAnswer(invocation -> "Request created");

		var result = controller.createRequest(
				authentication(),
				"order-uid",
				new CreateCommerceOrderResolutionRequest(
						CommerceOrderResolutionRequestType.CANCELLATION,
						"Changed mind",
						"Please cancel this order."));

		assertThat(result.getBody()).isNotNull();
		assertThat(result.getBody().getMessage()).isEqualTo("Request created");
		ArgumentCaptor<CreateCommerceOrderResolutionRequestCommand> commandCaptor =
				ArgumentCaptor.forClass(CreateCommerceOrderResolutionRequestCommand.class);
		verify(requestService).createCustomerRequest(any(), anyString(), commandCaptor.capture());
		assertThat(commandCaptor.getValue().type()).isEqualTo(CommerceOrderResolutionRequestType.CANCELLATION);
	}

	@Test
	void createCustomerRequest_ShouldNotExposeAdminFields() throws Exception {
		CommerceOrderController controller = new CommerceOrderController(null, requestService, messageSource);
		when(requestService.createCustomerRequest(any(), anyString(), any())).thenReturn(customerResponse());
		when(messageSource.getMessage(anyString(), any(), anyString(), any(Locale.class)))
				.thenAnswer(invocation -> "Request created");

		var result = controller.createRequest(
				authentication(),
				"order-uid",
				new CreateCommerceOrderResolutionRequest(
						CommerceOrderResolutionRequestType.CANCELLATION,
						"Changed mind",
						"Please cancel this order."));

		@SuppressWarnings("unchecked")
		Map<String, Object> json = objectMapper.convertValue(result.getBody().getData(), Map.class);
		assertThat(json).containsKeys("requestUid", "type", "status", "refundStatus");
		assertThat(json).doesNotContainKeys(
				"id",
				"customerEmail",
				"previousOrderStatus",
				"refundProvider",
				"refundReference",
				"refundFailureCode",
				"stockRestored");
	}

	@Test
	void listCustomerRequests_ShouldRequireCommercePrincipal() {
		CommerceOrderController controller = new CommerceOrderController(null, requestService, messageSource);
		TestingAuthenticationToken authentication = new TestingAuthenticationToken("admin", null, "ROLE_TENANT_ADMIN");

		assertThatThrownBy(() -> controller.listRequests(authentication, "order-uid"))
				.isInstanceOf(AccessDeniedException.class)
				.hasMessage("commerce.customer.auth.required");
	}

	@Test
	void listAdminRequests_ShouldReturnPageableResponseWithFilters() {
		CommerceAdminOrderController controller = new CommerceAdminOrderController(null, requestService, messageSource);
		when(requestService.listAdminRequests(any(), anyString(), any(), any()))
				.thenReturn(new PageImpl<>(List.of(response())));
		when(messageSource.getMessage(anyString(), any(), anyString(), any(Locale.class)))
				.thenAnswer(invocation -> "Requests retrieved");

		var result = controller.listOrderRequests(
				0,
				20,
				"createdAt,desc",
				"jane",
				CommerceOrderResolutionRequestType.RETURN,
				CommerceOrderResolutionRequestStatus.PENDING);

		assertThat(result.getBody()).isNotNull();
		assertThat(result.getBody().getData().content()).hasSize(1);
		ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
		verify(requestService).listAdminRequests(
				pageableCaptor.capture(),
				anyString(),
				any(),
				any());
		assertThat(pageableCaptor.getValue().getSort().getOrderFor("createdAt")).isNotNull();
	}

	@Test
	void getAdminRequest_ShouldExposeDecisionAuditFields() throws Exception {
		CommerceAdminOrderController controller = new CommerceAdminOrderController(null, requestService, messageSource);
		when(requestService.getAdminRequest("request-uid")).thenReturn(response());
		when(messageSource.getMessage(anyString(), any(), anyString(), any(Locale.class)))
				.thenAnswer(invocation -> "Request retrieved");

		var result = controller.getOrderRequest("request-uid");

		@SuppressWarnings("unchecked")
		Map<String, Object> json = objectMapper.convertValue(result.getBody().getData(), Map.class);
		assertThat(json).containsKeys("decidedByUserId", "decidedByEmail");
	}

	@Test
	void decideOrderRequest_ShouldMapApproveFlag() {
		CommerceAdminOrderController controller = new CommerceAdminOrderController(null, requestService, messageSource);
		when(requestService.decide(anyString(), any())).thenReturn(response());
		when(messageSource.getMessage(anyString(), any(), anyString(), any(Locale.class)))
				.thenAnswer(invocation -> "Request decided");

		controller.decideOrderRequest(
				"request-uid",
				new CommerceOrderResolutionDecisionRequest(
						CommerceOrderResolutionDecisionRequest.Decision.APPROVE,
						"Approved"));

		ArgumentCaptor<CommerceOrderResolutionDecisionCommand> commandCaptor =
				ArgumentCaptor.forClass(CommerceOrderResolutionDecisionCommand.class);
		verify(requestService).decide(anyString(), commandCaptor.capture());
		assertThat(commandCaptor.getValue().approve()).isTrue();
	}

	private TestingAuthenticationToken authentication() {
		CommerceCustomerPrincipal principal = new CommerceCustomerPrincipal(10L, "customer-uid", "customer@example.com", 1L);
		return new TestingAuthenticationToken(principal, null, "ROLE_COMMERCE_CUSTOMER");
	}

	private CommerceOrderResolutionRequestResponse response() {
		return new CommerceOrderResolutionRequestResponse(
				1L,
				"request-uid",
				"order-uid",
				"ORD-20260615-000001",
				"jane@example.com",
				"CANCELLATION",
				"PENDING",
				"Changed mind",
				"Please cancel this order.",
				"PAID",
				"CANCELLATION_REQUESTED",
				null,
				42L,
				"admin@example.com",
				"NOT_ATTEMPTED",
				null,
				null,
				null,
				null,
				false,
				BigDecimal.valueOf(200).setScale(2),
				"TRY",
				LocalDateTime.of(2026, 6, 19, 12, 0),
				null,
				null,
				null);
	}

	private CustomerOrderResolutionRequestResponse customerResponse() {
		return new CustomerOrderResolutionRequestResponse(
				"request-uid",
				"order-uid",
				"ORD-20260615-000001",
				"CANCELLATION",
				"PENDING",
				"Changed mind",
				"Please cancel this order.",
				"CANCELLATION_REQUESTED",
				"NOT_ATTEMPTED",
				LocalDateTime.of(2026, 6, 19, 12, 0),
				null,
				null);
	}
}
