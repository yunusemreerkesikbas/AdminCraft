package com.backend.application.commerce;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import com.backend.application.commerce.CommercePaymentProviderPort.Credentials;
import com.backend.application.commerce.CommercePaymentProviderPort.RefundPaymentResult;
import com.backend.application.commerce.dto.CommerceOrderResolutionDecisionCommand;
import com.backend.application.commerce.dto.CreateCommerceOrderResolutionRequestCommand;
import com.backend.domain.commerce.CommerceCustomer;
import com.backend.domain.commerce.CommerceOrder;
import com.backend.domain.commerce.CommerceOrderItem;
import com.backend.domain.commerce.CommerceOrderResolutionRefundStatus;
import com.backend.domain.commerce.CommerceOrderResolutionRequest;
import com.backend.domain.commerce.CommerceOrderResolutionRequestStatus;
import com.backend.domain.commerce.CommerceOrderResolutionRequestType;
import com.backend.domain.commerce.CommerceOrderStatus;
import com.backend.domain.commerce.exception.CommerceDomainException;
import com.backend.domain.commerce.repository.CommerceOrderRepository;
import com.backend.domain.commerce.repository.CommerceOrderResolutionRequestRepository;
import com.backend.testutil.BaseServiceTest;

class CommerceOrderResolutionRequestServiceImplTest extends BaseServiceTest {

	@Mock private CommerceOrderRepository orderRepository;
	@Mock private CommerceOrderResolutionRequestRepository requestRepository;
	@Mock private CommerceProductVariantStockPort stockPort;
	@Mock private CommerceModuleAccessGuard commerceModuleAccessGuard;
	@Mock private CommercePaymentProviderPort paymentProvider;
	@Mock private CommercePaymentConfigResolver paymentConfigResolver;
	@Mock private TransactionTemplate transactionTemplate;

	private CommerceOrderResolutionRequestServiceImpl service;

	@BeforeEach
	void setUp() {
		service = new CommerceOrderResolutionRequestServiceImpl(
				orderRepository,
				requestRepository,
				stockPort,
				commerceModuleAccessGuard,
				List.of(paymentProvider),
				paymentConfigResolver,
				transactionTemplate);
		lenient().when(paymentProvider.providerCode()).thenReturn("iyzico");
		lenient().when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
			TransactionCallback<?> callback = invocation.getArgument(0);
			return callback.doInTransaction(null);
		});
	}

	@Test
	void createCustomerRequest_ShouldMovePaidOrderToCancellationRequested() {
		CommerceOrder order = order(CommerceOrderStatus.PAID);
		when(orderRepository.findByCustomerIdAndUidForUpdate(10L, "order-uid")).thenReturn(Optional.of(order));
		when(requestRepository.existsPendingByOrderId(1L)).thenReturn(false);
		when(requestRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		var response = service.createCustomerRequest(
				principal(),
				"order-uid",
				new CreateCommerceOrderResolutionRequestCommand(
						CommerceOrderResolutionRequestType.CANCELLATION,
						"Changed mind",
						"Please cancel the order."));

		assertThat(order.getStatus()).isEqualTo(CommerceOrderStatus.CANCELLATION_REQUESTED);
		assertThat(order.getStatusHistory().getFirst().getFromStatus()).isEqualTo(CommerceOrderStatus.PAID);
		assertThat(response.type()).isEqualTo("CANCELLATION");
		assertThat(response.status()).isEqualTo("PENDING");
		verify(commerceModuleAccessGuard).assertEnabledForCurrentTenant();
	}

	@Test
	void createCustomerRequest_ShouldRejectReturnBeforeDelivered() {
		CommerceOrder order = order(CommerceOrderStatus.PAID);
		when(orderRepository.findByCustomerIdAndUidForUpdate(10L, "order-uid")).thenReturn(Optional.of(order));

		assertThatThrownBy(() -> service.createCustomerRequest(
				principal(),
				"order-uid",
				new CreateCommerceOrderResolutionRequestCommand(
						CommerceOrderResolutionRequestType.RETURN,
						"Does not fit",
						"Need to return.")))
				.isInstanceOf(CommerceDomainException.class)
				.hasMessage("commerce.order.request.not.eligible");
	}

	@Test
	void createCustomerRequest_ShouldRejectDuplicatePendingRequest() {
		CommerceOrder order = order(CommerceOrderStatus.PREPARING);
		when(orderRepository.findByCustomerIdAndUidForUpdate(10L, "order-uid")).thenReturn(Optional.of(order));
		when(requestRepository.existsPendingByOrderId(1L)).thenReturn(true);

		assertThatThrownBy(() -> service.createCustomerRequest(
				principal(),
				"order-uid",
				new CreateCommerceOrderResolutionRequestCommand(
						CommerceOrderResolutionRequestType.CANCELLATION,
						"Changed mind",
						"Please cancel.")))
				.isInstanceOf(CommerceDomainException.class)
				.hasMessage("commerce.order.request.pending.exists");
	}

	@Test
	void decide_ShouldApproveCancellationRefundAndRestoreStock() {
		CommerceOrder order = order(CommerceOrderStatus.CANCELLATION_REQUESTED);
		CommerceOrderResolutionRequest request = request(order, CommerceOrderResolutionRequestType.CANCELLATION);
		when(requestRepository.findByUidForUpdate("request-uid")).thenReturn(Optional.of(request));
		when(paymentConfigResolver.credentialsForProvider("iyzico")).thenReturn(new Credentials("api", "secret", "https://sandbox-api.iyzipay.com"));
		when(paymentProvider.refundPayment(any())).thenReturn(new RefundPaymentResult(true, "refund-123", null, null));
		when(stockPort.restore(any())).thenReturn(new CommerceProductVariantStockPort.StockAdjustmentResult(true, null));
		when(requestRepository.save(request)).thenReturn(request);

		var response = service.decide(
				"request-uid",
				new CommerceOrderResolutionDecisionCommand(true, "Approved"));

		assertThat(order.getStatus()).isEqualTo(CommerceOrderStatus.CANCELLED);
		assertThat(request.getStatus()).isEqualTo(CommerceOrderResolutionRequestStatus.APPROVED);
		assertThat(request.getRefundStatus()).isEqualTo(CommerceOrderResolutionRefundStatus.SUCCEEDED);
		assertThat(request.isStockRestored()).isTrue();
		assertThat(response.refundReference()).isEqualTo("refund-123");
		verify(stockPort).restore(any());
	}

	@Test
	void decide_ShouldPersistProcessingBeforeCallingProvider() {
		CommerceOrder order = order(CommerceOrderStatus.CANCELLATION_REQUESTED);
		CommerceOrderResolutionRequest request = request(order, CommerceOrderResolutionRequestType.CANCELLATION);
		when(requestRepository.findByUidForUpdate("request-uid")).thenReturn(Optional.of(request));
		when(paymentConfigResolver.credentialsForProvider("iyzico")).thenReturn(new Credentials("api", "secret", "https://sandbox-api.iyzipay.com"));
		when(requestRepository.save(request)).thenReturn(request);
		when(paymentProvider.refundPayment(any())).thenAnswer(invocation -> {
			assertThat(request.getRefundStatus()).isEqualTo(CommerceOrderResolutionRefundStatus.PROCESSING);
			verify(requestRepository, times(1)).save(request);
			return new RefundPaymentResult(true, "refund-123", null, null);
		});
		when(stockPort.restore(any())).thenReturn(new CommerceProductVariantStockPort.StockAdjustmentResult(true, null));

		service.decide("request-uid", new CommerceOrderResolutionDecisionCommand(true, "Approved"));

		assertThat(request.getStatus()).isEqualTo(CommerceOrderResolutionRequestStatus.APPROVED);
		assertThat(request.getRefundStatus()).isEqualTo(CommerceOrderResolutionRefundStatus.SUCCEEDED);
	}

	@Test
	void decide_ShouldLeaveRequestPendingWhenRefundFails() {
		CommerceOrder order = order(CommerceOrderStatus.RETURN_REQUESTED);
		CommerceOrderResolutionRequest request = request(order, CommerceOrderResolutionRequestType.RETURN);
		when(requestRepository.findByUidForUpdate("request-uid")).thenReturn(Optional.of(request));
		when(paymentConfigResolver.credentialsForProvider("iyzico")).thenReturn(new Credentials("api", "secret", "https://sandbox-api.iyzipay.com"));
		when(paymentProvider.refundPayment(any())).thenReturn(new RefundPaymentResult(false, null, "REFUND_FAILED", "commerce.payment.refund.failed"));
		when(requestRepository.save(request)).thenReturn(request);

		service.decide("request-uid", new CommerceOrderResolutionDecisionCommand(true, "Retry later"));

		assertThat(order.getStatus()).isEqualTo(CommerceOrderStatus.RETURN_REQUESTED);
		assertThat(request.getStatus()).isEqualTo(CommerceOrderResolutionRequestStatus.PENDING);
		assertThat(request.getRefundStatus()).isEqualTo(CommerceOrderResolutionRefundStatus.FAILED);
		assertThat(request.getRefundFailureCode()).isEqualTo("REFUND_FAILED");
		verify(stockPort, never()).restore(any());
	}

	@Test
	void decide_ShouldLeaveRequestPendingWhenRefundConfigFails() {
		CommerceOrder order = order(CommerceOrderStatus.RETURN_REQUESTED);
		CommerceOrderResolutionRequest request = request(order, CommerceOrderResolutionRequestType.RETURN);
		when(requestRepository.findByUidForUpdate("request-uid")).thenReturn(Optional.of(request));
		when(paymentConfigResolver.credentialsForProvider("iyzico"))
				.thenThrow(new IllegalStateException("commerce.payment.config.required"));
		when(requestRepository.save(request)).thenReturn(request);

		service.decide("request-uid", new CommerceOrderResolutionDecisionCommand(true, "Retry later"));

		assertThat(order.getStatus()).isEqualTo(CommerceOrderStatus.RETURN_REQUESTED);
		assertThat(request.getStatus()).isEqualTo(CommerceOrderResolutionRequestStatus.PENDING);
		assertThat(request.getRefundStatus()).isEqualTo(CommerceOrderResolutionRefundStatus.FAILED);
		assertThat(request.getRefundFailureCode()).isEqualTo("PROVIDER_REFUND_FAILED");
		verify(paymentProvider, never()).refundPayment(any());
		verify(stockPort, never()).restore(any());
	}

	@Test
	void decide_ShouldRejectAndRestorePreviousOrderStatus() {
		CommerceOrder order = order(CommerceOrderStatus.RETURN_REQUESTED);
		CommerceOrderResolutionRequest request = request(order, CommerceOrderResolutionRequestType.RETURN);
		request.setPreviousOrderStatus(CommerceOrderStatus.DELIVERED);
		when(requestRepository.findByUidForUpdate("request-uid")).thenReturn(Optional.of(request));
		when(requestRepository.save(request)).thenReturn(request);

		service.decide("request-uid", new CommerceOrderResolutionDecisionCommand(false, "Rejected"));

		assertThat(order.getStatus()).isEqualTo(CommerceOrderStatus.DELIVERED);
		assertThat(request.getStatus()).isEqualTo(CommerceOrderResolutionRequestStatus.REJECTED);
		verify(paymentProvider, never()).refundPayment(any());
	}

	private CommerceCustomerPrincipal principal() {
		return new CommerceCustomerPrincipal(10L, "customer-uid", "jane@example.com", 1L);
	}

	private CommerceOrderResolutionRequest request(CommerceOrder order, CommerceOrderResolutionRequestType type) {
		CommerceOrderResolutionRequest request = new CommerceOrderResolutionRequest();
		request.setId(50L);
		request.setUid("request-uid");
		request.setOrder(order);
		request.setCustomer(order.getCustomer());
		request.setType(type);
		request.setStatus(CommerceOrderResolutionRequestStatus.PENDING);
		request.setReason("Reason");
		request.setDescription("Description");
		request.setPreviousOrderStatus(type == CommerceOrderResolutionRequestType.CANCELLATION
				? CommerceOrderStatus.PAID
				: CommerceOrderStatus.DELIVERED);
		request.setRequestedOrderStatus(type == CommerceOrderResolutionRequestType.CANCELLATION
				? CommerceOrderStatus.CANCELLATION_REQUESTED
				: CommerceOrderStatus.RETURN_REQUESTED);
		request.setRefundStatus(CommerceOrderResolutionRefundStatus.NOT_ATTEMPTED);
		return request;
	}

	private CommerceOrder order(CommerceOrderStatus status) {
		CommerceCustomer customer = new CommerceCustomer();
		customer.setId(10L);
		customer.setUid("customer-uid");
		customer.setEmail("jane@example.com");
		customer.setFirstName("Jane");
		customer.setLastName("Doe");

		CommerceOrder order = new CommerceOrder();
		order.setId(1L);
		order.setUid("order-uid");
		order.setOrderNumber("ORD-20260615-000001");
		order.setCustomer(customer);
		order.setStatus(status);
		order.setCreatedAt(LocalDateTime.of(2026, 6, 15, 12, 0));
		order.setCurrencyIso("TRY");
		order.setTotal(BigDecimal.valueOf(200).setScale(2));
		order.setProvider("iyzico");
		order.setProviderTransactionId("payment-123");
		order.setStockDeducted(true);
		CommerceOrderItem item = new CommerceOrderItem();
		item.setVariantUid("variant-uid");
		item.setQuantity(2);
		order.addItem(item);
		return order;
	}
}
