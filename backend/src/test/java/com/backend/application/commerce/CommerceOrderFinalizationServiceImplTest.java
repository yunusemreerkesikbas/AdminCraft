package com.backend.application.commerce;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;

import com.backend.application.commerce.CommerceProductVariantStockPort.StockDeductionResult;
import com.backend.application.service.config.ConfigPropertyService;
import com.backend.domain.commerce.CommerceCart;
import com.backend.domain.commerce.CommerceCartItem;
import com.backend.domain.commerce.CommerceCartStatus;
import com.backend.domain.commerce.CommerceCheckout;
import com.backend.domain.commerce.CommerceCheckoutItem;
import com.backend.domain.commerce.CommerceCheckoutStatus;
import com.backend.domain.commerce.CommerceCustomer;
import com.backend.domain.commerce.CommerceOrder;
import com.backend.domain.commerce.CommerceOrderLegalSnapshotStatus;
import com.backend.domain.commerce.CommerceOrderStatus;
import com.backend.domain.commerce.CommercePaymentAttempt;
import com.backend.domain.commerce.CommercePaymentAttemptStatus;
import com.backend.domain.commerce.repository.CommerceCheckoutRepository;
import com.backend.domain.commerce.repository.CommerceOrderNumberCounterRepository;
import com.backend.domain.commerce.repository.CommerceOrderRepository;
import com.backend.domain.port.TenantContextPort;
import com.backend.testutil.BaseServiceTest;

class CommerceOrderFinalizationServiceImplTest extends BaseServiceTest {

	@Mock private CommerceOrderRepository orderRepository;
	@Mock private CommerceCheckoutRepository checkoutRepository;
	@Mock private CommerceOrderNumberCounterRepository orderNumberCounterRepository;
	@Mock private CommerceProductVariantStockPort stockPort;
	@Mock private ConfigPropertyService configPropertyService;
	@Mock private TenantContextPort tenantContext;

	private CommerceOrderFinalizationServiceImpl service;

	@BeforeEach
	void setUp() {
		service = new CommerceOrderFinalizationServiceImpl(
				orderRepository,
				checkoutRepository,
				orderNumberCounterRepository,
				stockPort,
				configPropertyService,
				tenantContext);
		lenient().when(tenantContext.getTenantId()).thenReturn("1");
		lenient().when(tenantContext.getTenantDbName()).thenReturn("tenant_1");
		lenient().when(configPropertyService.findRaw(1L, "tenant_1", "commerce.order.number_prefix"))
				.thenReturn(Optional.empty());
	}

	@Test
	void finalizeSuccessfulPayment_ShouldCreatePaidOrderFromCheckoutSnapshot() {
		CommercePaymentAttempt attempt = attempt();
		when(checkoutRepository.findByIdForUpdate(30L)).thenReturn(Optional.of(attempt.getCheckout()));
		when(orderRepository.findByPaymentAttemptId(40L)).thenReturn(Optional.empty());
		when(orderRepository.findByCheckoutId(30L)).thenReturn(Optional.empty());
		when(orderNumberCounterRepository.nextSequence(any(String.class), any(LocalDate.class))).thenReturn(1);
		when(stockPort.deductIfAvailable(Map.of("variant-uid", 2))).thenReturn(new StockDeductionResult(true, null));
		when(orderRepository.save(any(CommerceOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

		CommerceOrder order = service.finalizeSuccessfulPayment(attempt);

		assertThat(order.getOrderNumber()).startsWith("ORD-").endsWith("-000001");
		assertThat(order.getStatus()).isEqualTo(CommerceOrderStatus.PAID);
		assertThat(order.getCustomer()).isSameAs(attempt.getCustomer());
		assertThat(order.getCheckout()).isSameAs(attempt.getCheckout());
		assertThat(order.getPaymentAttempt()).isSameAs(attempt);
		assertThat(order.getProviderTransactionId()).isEqualTo("payment-123");
		assertThat(order.getLegalSnapshotStatus()).isEqualTo(CommerceOrderLegalSnapshotStatus.NOT_CAPTURED);
		assertThat(order.isStockDeducted()).isTrue();
		assertThat(order.isRequiresAttention()).isFalse();
		assertThat(order.getItems()).hasSize(1);
		assertThat(order.getItems().getFirst().getVariantUid()).isEqualTo("variant-uid");
		assertThat(order.getItems().getFirst().getLineTotal()).isEqualByComparingTo("200.00");
		assertThat(attempt.getCheckout().getCart().getStatus()).isEqualTo(CommerceCartStatus.CLEARED);
		assertThat(attempt.getCheckout().getStatus()).isEqualTo(CommerceCheckoutStatus.COMPLETED);
		verify(stockPort).deductIfAvailable(Map.of("variant-uid", 2));
	}

	@Test
	void finalizeSuccessfulPayment_ShouldLockCheckoutBeforeStockDeduction() {
		CommercePaymentAttempt attempt = attempt();
		when(checkoutRepository.findByIdForUpdate(30L)).thenReturn(Optional.of(attempt.getCheckout()));
		when(orderRepository.findByPaymentAttemptId(40L)).thenReturn(Optional.empty());
		when(orderRepository.findByCheckoutId(30L)).thenReturn(Optional.empty());
		when(orderNumberCounterRepository.nextSequence(any(String.class), any(LocalDate.class))).thenReturn(1);
		when(stockPort.deductIfAvailable(Map.of("variant-uid", 2))).thenReturn(new StockDeductionResult(true, null));
		when(orderRepository.save(any(CommerceOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

		service.finalizeSuccessfulPayment(attempt);

		InOrder inOrder = inOrder(checkoutRepository, orderRepository, stockPort);
		inOrder.verify(checkoutRepository).findByIdForUpdate(30L);
		inOrder.verify(orderRepository).findByPaymentAttemptId(40L);
		inOrder.verify(orderRepository).findByCheckoutId(30L);
		inOrder.verify(stockPort).deductIfAvailable(Map.of("variant-uid", 2));
	}

	@Test
	void finalizeSuccessfulPayment_ShouldUseConfiguredPrefixAndDailyCounter() {
		CommercePaymentAttempt attempt = attempt();
		when(checkoutRepository.findByIdForUpdate(30L)).thenReturn(Optional.of(attempt.getCheckout()));
		when(configPropertyService.findRaw(1L, "tenant_1", "commerce.order.number_prefix"))
				.thenReturn(Optional.of("web"));
		when(orderRepository.findByPaymentAttemptId(40L)).thenReturn(Optional.empty());
		when(orderRepository.findByCheckoutId(30L)).thenReturn(Optional.empty());
		when(orderNumberCounterRepository.nextSequence(any(String.class), any(LocalDate.class))).thenReturn(42);
		when(stockPort.deductIfAvailable(any())).thenReturn(new StockDeductionResult(true, null));
		when(orderRepository.save(any(CommerceOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

		CommerceOrder order = service.finalizeSuccessfulPayment(attempt);

		assertThat(order.getOrderNumber()).startsWith("WEB-").endsWith("-000042");
		ArgumentCaptor<String> prefixCaptor = ArgumentCaptor.forClass(String.class);
		verify(orderNumberCounterRepository).nextSequence(prefixCaptor.capture(), any(LocalDate.class));
		assertThat(prefixCaptor.getValue()).isEqualTo("WEB");
	}

	@Test
	void finalizeSuccessfulPayment_ShouldFallbackToDefaultPrefix_WhenConfiguredPrefixIsInvalid() {
		CommercePaymentAttempt attempt = attempt();
		when(checkoutRepository.findByIdForUpdate(30L)).thenReturn(Optional.of(attempt.getCheckout()));
		when(configPropertyService.findRaw(1L, "tenant_1", "commerce.order.number_prefix"))
				.thenReturn(Optional.of("this-prefix-is-way-too-long"));
		when(orderRepository.findByPaymentAttemptId(40L)).thenReturn(Optional.empty());
		when(orderRepository.findByCheckoutId(30L)).thenReturn(Optional.empty());
		when(orderNumberCounterRepository.nextSequence(any(String.class), any(LocalDate.class))).thenReturn(7);
		when(stockPort.deductIfAvailable(any())).thenReturn(new StockDeductionResult(true, null));
		when(orderRepository.save(any(CommerceOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

		CommerceOrder order = service.finalizeSuccessfulPayment(attempt);

		assertThat(order.getOrderNumber()).startsWith("ORD-").endsWith("-000007");
		ArgumentCaptor<String> prefixCaptor = ArgumentCaptor.forClass(String.class);
		verify(orderNumberCounterRepository).nextSequence(prefixCaptor.capture(), any(LocalDate.class));
		assertThat(prefixCaptor.getValue()).isEqualTo("ORD");
	}

	@Test
	void finalizeSuccessfulPayment_ShouldReturnExistingOrder_WhenAttemptAlreadyFinalized() {
		CommercePaymentAttempt attempt = attempt();
		CommerceOrder existing = new CommerceOrder();
		existing.setOrderNumber("ORD-20260615-000001");
		when(checkoutRepository.findByIdForUpdate(30L)).thenReturn(Optional.of(attempt.getCheckout()));
		when(orderRepository.findByPaymentAttemptId(40L)).thenReturn(Optional.of(existing));

		CommerceOrder order = service.finalizeSuccessfulPayment(attempt);

		assertThat(order).isSameAs(existing);
		verify(stockPort, never()).deductIfAvailable(any());
		verify(orderRepository, never()).save(any());
	}

	@Test
	void finalizeSuccessfulPayment_ShouldCreatePaidAttentionOrder_WhenStockCannotBeDeducted() {
		CommercePaymentAttempt attempt = attempt();
		when(checkoutRepository.findByIdForUpdate(30L)).thenReturn(Optional.of(attempt.getCheckout()));
		when(orderRepository.findByPaymentAttemptId(40L)).thenReturn(Optional.empty());
		when(orderRepository.findByCheckoutId(30L)).thenReturn(Optional.empty());
		when(orderNumberCounterRepository.nextSequence(any(String.class), any(LocalDate.class))).thenReturn(1);
		when(stockPort.deductIfAvailable(Map.of("variant-uid", 2)))
				.thenReturn(new StockDeductionResult(false, "commerce.order.attention.stock_not_deducted"));
		when(orderRepository.save(any(CommerceOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

		CommerceOrder order = service.finalizeSuccessfulPayment(attempt);

		assertThat(order.getStatus()).isEqualTo(CommerceOrderStatus.PAID);
		assertThat(order.isStockDeducted()).isFalse();
		assertThat(order.isRequiresAttention()).isTrue();
		assertThat(order.getAttentionReasonKey()).isEqualTo("commerce.order.attention.stock_not_deducted");
		assertThat(attempt.getCheckout().getCart().getStatus()).isEqualTo(CommerceCartStatus.CLEARED);
		assertThat(attempt.getCheckout().getStatus()).isEqualTo(CommerceCheckoutStatus.COMPLETED);
	}

	private CommercePaymentAttempt attempt() {
		CommercePaymentAttempt attempt = new CommercePaymentAttempt();
		attempt.setId(40L);
		attempt.setUid("attempt-uid");
		attempt.setCustomer(customer());
		attempt.setCheckout(checkout(attempt.getCustomer()));
		attempt.setProvider("iyzico");
		attempt.setStatus(CommercePaymentAttemptStatus.SUCCEEDED);
		attempt.setCurrencyIso("TRY");
		attempt.setSubtotal(BigDecimal.valueOf(200).setScale(2));
		attempt.setVatTotal(BigDecimal.valueOf(33.33));
		attempt.setShippingTotal(BigDecimal.ZERO.setScale(2));
		attempt.setTotal(BigDecimal.valueOf(200).setScale(2));
		attempt.setProviderTransactionId("payment-123");
		attempt.setExpiresAt(LocalDateTime.now().plusMinutes(30));
		return attempt;
	}

	private CommerceCheckout checkout(CommerceCustomer customer) {
		CommerceCheckout checkout = new CommerceCheckout();
		checkout.setId(30L);
		checkout.setUid("checkout-uid");
		checkout.setCustomer(customer);
		checkout.setCart(activeCart(customer));
		checkout.setStatus(CommerceCheckoutStatus.READY);
		checkout.setCurrencyIso("TRY");
		checkout.setSubtotal(BigDecimal.valueOf(200).setScale(2));
		checkout.setVatTotal(BigDecimal.valueOf(33.33));
		checkout.setShippingTotal(BigDecimal.ZERO.setScale(2));
		checkout.setTotal(BigDecimal.valueOf(200).setScale(2));
		checkout.setShippingMethodCode("standard");
		checkout.setShippingMethodName("Standard Shipping");
		checkout.setDeliveryAddressUid("delivery-address-uid");
		checkout.setBillingAddressUid("billing-address-uid");
		checkout.setDeliveryAddressSnapshot(addressSnapshot());
		checkout.setBillingAddressSnapshot(addressSnapshot());
		checkout.setExpiresAt(LocalDateTime.now().plusHours(1));
		checkout.addItem(checkoutItem());
		return checkout;
	}

	private CommerceCheckoutItem checkoutItem() {
		CommerceCheckoutItem item = new CommerceCheckoutItem();
		item.setUid("checkout-item-uid");
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

	private CommerceCart activeCart(CommerceCustomer customer) {
		CommerceCart cart = new CommerceCart();
		cart.setId(20L);
		cart.setUid("cart-uid");
		cart.setCustomer(customer);
		cart.setStatus(CommerceCartStatus.ACTIVE);
		cart.setExpiresAt(LocalDateTime.now().plusDays(1));
		cart.addItem(cartItem());
		return cart;
	}

	private CommerceCartItem cartItem() {
		CommerceCartItem item = new CommerceCartItem();
		item.setProductUid("product-uid");
		item.setProductSku("PROD-1");
		item.setVariantUid("variant-uid");
		item.setVariantSku("VAR-1");
		item.setQuantity(2);
		item.setUnitGrossPrice(BigDecimal.valueOf(100));
		item.setVatRate(BigDecimal.valueOf(20));
		return item;
	}

	private CommerceCustomer customer() {
		CommerceCustomer customer = new CommerceCustomer();
		customer.setId(10L);
		customer.setUid("customer-uid");
		customer.setEmail("user@example.com");
		customer.setFirstName("John");
		customer.setLastName("Doe");
		customer.setPhone("+905350000000");
		return customer;
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
