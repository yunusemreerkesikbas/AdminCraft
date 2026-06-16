package com.backend.application.commerce;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.backend.application.commerce.dto.CommerceAdminDashboardResponse;
import com.backend.application.commerce.dto.CommerceAdminOrderDetailResponse;
import com.backend.application.commerce.dto.CommerceAdminOrderSummaryResponse;
import com.backend.application.commerce.dto.CommerceAdminPaymentAttemptResponse;
import com.backend.domain.commerce.CommerceCheckout;
import com.backend.domain.commerce.CommerceCustomer;
import com.backend.domain.commerce.CommerceOrder;
import com.backend.domain.commerce.CommerceOrderItem;
import com.backend.domain.commerce.CommerceOrderLegalSnapshotStatus;
import com.backend.domain.commerce.CommerceOrderStatus;
import com.backend.domain.commerce.CommercePaymentAttempt;
import com.backend.domain.commerce.CommercePaymentAttemptStatus;
import com.backend.domain.commerce.repository.CommerceOrderRepository;
import com.backend.domain.commerce.repository.CommercePaymentAttemptRepository;
import com.backend.domain.port.TenantContextPort;
import com.backend.domain.exception.EntityNotFoundException;
import com.backend.testutil.BaseServiceTest;
import com.fasterxml.jackson.databind.ObjectMapper;

class CommerceAdminOrderServiceImplTest extends BaseServiceTest {

	@Mock private CommerceOrderRepository orderRepository;
	@Mock private CommercePaymentAttemptRepository paymentAttemptRepository;
	@Mock private CommerceModuleAccessGuard commerceModuleAccessGuard;
	@Mock private TenantContextPort tenantContext;

	private CommerceAdminOrderServiceImpl service;

	@BeforeEach
	void setUp() {
		service = new CommerceAdminOrderServiceImpl(
				orderRepository,
				paymentAttemptRepository,
				commerceModuleAccessGuard,
				tenantContext,
				new ObjectMapper());
	}

	@Test
	void dashboard_ShouldReturnOperationSummaryAndCallModuleGuard() {
		when(orderRepository.findMostRecentCurrencyIso()).thenReturn(Optional.of("TRY"));
		when(orderRepository.countByCreatedAtBetween(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
				.thenReturn(2L, 5L);
		when(orderRepository.sumTotalByCreatedAtBetween(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
				.thenReturn(BigDecimal.valueOf(250), BigDecimal.valueOf(900));
		when(orderRepository.countByRequiresAttentionTrue()).thenReturn(1L);
		when(paymentAttemptRepository.countByStatusAndCreatedAtGreaterThanEqual(
				org.mockito.ArgumentMatchers.eq(CommercePaymentAttemptStatus.FAILED),
				org.mockito.ArgumentMatchers.any())).thenReturn(3L);

		CommerceAdminDashboardResponse response = service.dashboard();

		assertThat(response.today().orderCount()).isEqualTo(2);
		assertThat(response.today().revenue()).isEqualByComparingTo("250");
		assertThat(response.lastSevenDays().orderCount()).isEqualTo(5);
		assertThat(response.attentionOrderCount()).isEqualTo(1);
		assertThat(response.failedPaymentAttemptCount()).isEqualTo(3);
		assertThat(response.currencyIso()).isEqualTo("TRY");
		verify(commerceModuleAccessGuard).assertEnabledForCurrentTenant();
	}

	@Test
	void listOrders_ShouldMapAdminFieldsAndItemCounts() {
		CommerceOrder order = order(1L, "order-uid");
		when(orderRepository.findAdminOrders("jane", CommerceOrderStatus.PAID, true, PageRequest.of(0, 20)))
				.thenReturn(new PageImpl<>(List.of(order)));
		when(orderRepository.countItemsByOrderIds(List.of(1L))).thenReturn(Map.of(1L, 2));

		CommerceAdminOrderSummaryResponse response = service
				.listOrders(PageRequest.of(0, 20), " Jane ", CommerceOrderStatus.PAID, true)
				.getContent()
				.getFirst();

		assertThat(response.id()).isEqualTo(1L);
		assertThat(response.customerName()).isEqualTo("Jane Doe");
		assertThat(response.customerEmail()).isEqualTo("jane@example.com");
		assertThat(response.itemCount()).isEqualTo(2);
		assertThat(response.provider()).isEqualTo("iyzico");
		assertThat(response.requiresAttention()).isTrue();
		verify(commerceModuleAccessGuard).assertEnabledForCurrentTenant();
	}

	@Test
	void getOrder_ShouldReturnAdminDetailWithPaymentProviderFields() {
		CommerceOrder order = order(1L, "order-uid");
		order.addItem(orderItem());
		when(orderRepository.findAdminByUid("order-uid")).thenReturn(Optional.of(order));

		CommerceAdminOrderDetailResponse response = service.getOrder("order-uid");

		assertThat(response.summary().orderUid()).isEqualTo("order-uid");
		assertThat(response.customerPhone()).isEqualTo("+905350000000");
		assertThat(response.providerTransactionId()).isEqualTo("payment-123");
		assertThat(response.paymentAttempt().providerReference()).isEqualTo("token-123");
		assertThat(response.items()).hasSize(1);
		assertThat(response.deliveryAddress().city()).isEqualTo("Istanbul");
		assertThat(response.legalSnapshotStatus()).isEqualTo("NOT_CAPTURED");
		assertThat(response.stockDeducted()).isTrue();
	}

	@Test
	void getOrder_ShouldThrowNotFound_WhenOrderMissing() {
		when(orderRepository.findAdminByUid("missing")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.getOrder("missing"))
				.isInstanceOf(EntityNotFoundException.class)
				.hasMessage("commerce.order.not.found");
	}

	@Test
	void listPaymentAttempts_ShouldMapAdminPaymentRows() {
		CommercePaymentAttempt attempt = paymentAttempt();
		when(paymentAttemptRepository.findAdminPaymentAttempts(
				"jane",
				CommercePaymentAttemptStatus.FAILED,
				PageRequest.of(0, 20)))
				.thenReturn(new PageImpl<>(List.of(attempt)));

		CommerceAdminPaymentAttemptResponse response = service
				.listPaymentAttempts(PageRequest.of(0, 20), " Jane ", CommercePaymentAttemptStatus.FAILED)
				.getContent()
				.getFirst();

		assertThat(response.attemptUid()).isEqualTo("attempt-uid");
		assertThat(response.checkoutUid()).isEqualTo("checkout-uid");
		assertThat(response.customerEmail()).isEqualTo("jane@example.com");
		assertThat(response.providerReference()).isEqualTo("token-123");
		assertThat(response.failureCode()).isEqualTo("card_error");
		verify(commerceModuleAccessGuard).assertEnabledForCurrentTenant();
	}

	private CommerceOrder order(Long id, String uid) {
		CommerceOrder order = new CommerceOrder();
		order.setId(id);
		order.setUid(uid);
		order.setOrderNumber("ORD-20260615-000001");
		order.setCreatedAt(LocalDateTime.of(2026, 6, 15, 12, 0));
		order.setCustomer(customer());
		order.setStatus(CommerceOrderStatus.PAID);
		order.setCurrencyIso("TRY");
		order.setSubtotal(BigDecimal.valueOf(166.67));
		order.setVatTotal(BigDecimal.valueOf(33.33));
		order.setShippingTotal(BigDecimal.ZERO.setScale(2));
		order.setTotal(BigDecimal.valueOf(200).setScale(2));
		order.setShippingMethodCode("STANDARD");
		order.setShippingMethodName("commerce.shipping.method.standard");
		order.setDeliveryAddressUid("delivery-address-uid");
		order.setBillingAddressUid("billing-address-uid");
		order.setDeliveryAddressSnapshot(addressSnapshot());
		order.setBillingAddressSnapshot(addressSnapshot());
		order.setProvider("iyzico");
		order.setProviderTransactionId("payment-123");
		order.setLegalSnapshotStatus(CommerceOrderLegalSnapshotStatus.NOT_CAPTURED);
		order.setStockDeducted(true);
		order.setRequiresAttention(true);
		order.setAttentionReasonKey("commerce.order.attention.stock_not_deducted");
		order.setPaymentAttempt(paymentAttempt());
		return order;
	}

	private CommercePaymentAttempt paymentAttempt() {
		CommercePaymentAttempt attempt = new CommercePaymentAttempt();
		attempt.setId(20L);
		attempt.setUid("attempt-uid");
		attempt.setCustomer(customer());
		attempt.setCheckout(checkout());
		attempt.setProvider("iyzico");
		attempt.setStatus(CommercePaymentAttemptStatus.FAILED);
		attempt.setCurrencyIso("TRY");
		attempt.setSubtotal(BigDecimal.valueOf(166.67));
		attempt.setVatTotal(BigDecimal.valueOf(33.33));
		attempt.setShippingTotal(BigDecimal.ZERO.setScale(2));
		attempt.setTotal(BigDecimal.valueOf(200).setScale(2));
		attempt.setCreatedAt(LocalDateTime.of(2026, 6, 15, 12, 0));
		attempt.setExpiresAt(LocalDateTime.of(2026, 6, 15, 12, 30));
		attempt.setProviderReference("token-123");
		attempt.setProviderTransactionId("payment-123");
		attempt.setFailureCode("card_error");
		attempt.setFailureMessageKey("commerce.payment.provider.failed");
		return attempt;
	}

	private CommerceCustomer customer() {
		CommerceCustomer customer = new CommerceCustomer();
		customer.setId(10L);
		customer.setUid("customer-uid");
		customer.setFirstName("Jane");
		customer.setLastName("Doe");
		customer.setEmail("jane@example.com");
		customer.setPhone("+905350000000");
		return customer;
	}

	private CommerceCheckout checkout() {
		CommerceCheckout checkout = new CommerceCheckout();
		checkout.setId(30L);
		checkout.setUid("checkout-uid");
		return checkout;
	}

	private CommerceOrderItem orderItem() {
		CommerceOrderItem item = new CommerceOrderItem();
		item.setUid("order-item-uid");
		item.setProductUid("product-uid");
		item.setProductSku("PROD-1");
		item.setVariantUid("variant-uid");
		item.setVariantSku("VAR-1");
		item.setQuantity(2);
		item.setUnitGrossPrice(BigDecimal.valueOf(100).setScale(2));
		item.setVatRate(BigDecimal.valueOf(20).setScale(2));
		item.setLineTotal(BigDecimal.valueOf(200).setScale(2));
		item.setLineVatTotal(BigDecimal.valueOf(33.33));
		return item;
	}

	private String addressSnapshot() {
		return """
				{
				  "uid": "address-uid",
				  "label": "Home",
				  "firstName": "Jane",
				  "lastName": "Doe",
				  "phone": "+905350000000",
				  "countryIso": "TR",
				  "city": "Istanbul",
				  "district": "Kadikoy",
				  "addressLine1": "Test Street 1",
				  "addressLine2": null,
				  "postalCode": "34710",
				  "invoiceType": "INDIVIDUAL",
				  "companyName": null,
				  "taxNumber": null,
				  "taxOffice": null,
				  "invoiceIdentityNumber": null
				}
				""";
	}
}
