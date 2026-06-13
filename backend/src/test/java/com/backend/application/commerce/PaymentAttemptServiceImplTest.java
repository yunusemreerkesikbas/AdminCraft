package com.backend.application.commerce;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.backend.application.commerce.CommerceProductVariantLookupPort.CommerceVariantSnapshot;
import com.backend.application.commerce.dto.CreatePaymentAttemptCommand;
import com.backend.application.service.config.ConfigPropertyService;
import com.backend.domain.commerce.CommerceCart;
import com.backend.domain.commerce.CommerceCartItem;
import com.backend.domain.commerce.CommerceCartStatus;
import com.backend.domain.commerce.CommerceCheckout;
import com.backend.domain.commerce.CommerceCheckoutItem;
import com.backend.domain.commerce.CommerceCheckoutStatus;
import com.backend.domain.commerce.CommerceCustomer;
import com.backend.domain.commerce.CommercePaymentAttempt;
import com.backend.domain.commerce.CommercePaymentAttemptStatus;
import com.backend.domain.commerce.exception.CommerceDomainException;
import com.backend.domain.commerce.repository.CommerceCheckoutRepository;
import com.backend.domain.commerce.repository.CommercePaymentAttemptRepository;
import com.backend.domain.port.TenantContextPort;
import com.backend.testutil.BaseServiceTest;

class PaymentAttemptServiceImplTest extends BaseServiceTest {

	@Mock private CommerceModuleAccessGuard commerceModuleAccessGuard;
	@Mock private CommerceCheckoutRepository checkoutRepository;
	@Mock private CommercePaymentAttemptRepository paymentAttemptRepository;
	@Mock private CommerceProductVariantLookupPort productVariantLookupPort;
	@Mock private ConfigPropertyService configPropertyService;
	@Mock private TenantContextPort tenantContext;

	private PaymentAttemptServiceImpl service;

	@BeforeEach
	void setUp() {
		service = new PaymentAttemptServiceImpl(
				commerceModuleAccessGuard,
				checkoutRepository,
				paymentAttemptRepository,
				productVariantLookupPort,
				configPropertyService,
				tenantContext);
		lenient().when(tenantContext.getTenantId()).thenReturn("1");
		lenient().when(tenantContext.getTenantDbName()).thenReturn("tenant_1");
	}

	@Test
	void create_ShouldCreatePendingAttemptFromReadyCheckout() {
		stubPaymentEnabled();
		CommerceCheckout checkout = checkout();
		when(checkoutRepository.findByCustomerIdAndUid(10L, "checkout-uid")).thenReturn(Optional.of(checkout));
		when(productVariantLookupPort.findByVariantUids(anyCollection()))
				.thenReturn(Map.of("variant-uid", variant(BigDecimal.valueOf(100), 5)));
		when(paymentAttemptRepository.save(any(CommercePaymentAttempt.class))).thenAnswer(invocation -> {
			CommercePaymentAttempt attempt = invocation.getArgument(0);
			attempt.setUid("attempt-uid");
			return attempt;
		});

		var response = service.create(principal(), new CreatePaymentAttemptCommand("checkout-uid"));

		assertThat(response.attemptUid()).isEqualTo("attempt-uid");
		assertThat(response.checkoutUid()).isEqualTo("checkout-uid");
		assertThat(response.status()).isEqualTo("PENDING");
		assertThat(response.provider()).isEqualTo("iyzico");
		assertThat(response.totals().total()).isEqualByComparingTo("200.00");
		assertThat(response.expiresAt()).isAfter(LocalDateTime.now().plusMinutes(29));
		verify(paymentAttemptRepository).expirePendingAttemptsForCheckout(
				10L,
				30L,
				CommercePaymentAttemptStatus.PENDING);
	}

	@Test
	void create_ShouldReject_WhenPaymentDisabled() {
		when(configPropertyService.getBoolean(1L, "tenant_1", "commerce.payment.enabled", false)).thenReturn(false);

		assertThatThrownBy(() -> service.create(principal(), new CreatePaymentAttemptCommand("checkout-uid")))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("commerce.payment.disabled");
		verify(paymentAttemptRepository, never()).save(any());
	}

	@Test
	void create_ShouldReject_WhenCheckoutPriceChanged() {
		stubPaymentEnabled();
		when(checkoutRepository.findByCustomerIdAndUid(10L, "checkout-uid")).thenReturn(Optional.of(checkout()));
		when(productVariantLookupPort.findByVariantUids(anyCollection()))
				.thenReturn(Map.of("variant-uid", variant(BigDecimal.valueOf(120), 5)));

		assertThatThrownBy(() -> service.create(principal(), new CreatePaymentAttemptCommand("checkout-uid")))
				.isInstanceOf(CommerceDomainException.class)
				.hasMessageContaining("commerce.payment.checkout.price.changed");
		verify(paymentAttemptRepository, never()).save(any());
	}

	@Test
	void get_ShouldExpirePendingAttempt_WhenAttemptTtlExpired() {
		CommercePaymentAttempt attempt = attempt();
		attempt.setExpiresAt(LocalDateTime.now().minusMinutes(1));
		when(paymentAttemptRepository.findByCustomerIdAndUid(10L, "attempt-uid")).thenReturn(Optional.of(attempt));
		when(paymentAttemptRepository.save(any(CommercePaymentAttempt.class))).thenAnswer(invocation -> invocation.getArgument(0));

		var response = service.get(principal(), "attempt-uid");

		assertThat(response.status()).isEqualTo("EXPIRED");
		verify(paymentAttemptRepository).save(attempt);
	}

	@Test
	void get_ShouldExpirePendingAttempt_WhenCheckoutPriceChanged() {
		CommercePaymentAttempt attempt = attempt();
		when(paymentAttemptRepository.findByCustomerIdAndUid(10L, "attempt-uid")).thenReturn(Optional.of(attempt));
		when(productVariantLookupPort.findByVariantUids(anyCollection()))
				.thenReturn(Map.of("variant-uid", variant(BigDecimal.valueOf(120), 5)));
		when(paymentAttemptRepository.save(any(CommercePaymentAttempt.class))).thenAnswer(invocation -> invocation.getArgument(0));

		var response = service.get(principal(), "attempt-uid");

		assertThat(response.status()).isEqualTo("EXPIRED");
		verify(paymentAttemptRepository).save(attempt);
	}

	@Test
	void get_ShouldExpirePendingAttempt_WhenCheckoutStockChanged() {
		CommercePaymentAttempt attempt = attempt();
		when(paymentAttemptRepository.findByCustomerIdAndUid(10L, "attempt-uid")).thenReturn(Optional.of(attempt));
		when(productVariantLookupPort.findByVariantUids(anyCollection()))
				.thenReturn(Map.of("variant-uid", variant(BigDecimal.valueOf(100), 1)));
		when(paymentAttemptRepository.save(any(CommercePaymentAttempt.class))).thenAnswer(invocation -> invocation.getArgument(0));

		var response = service.get(principal(), "attempt-uid");

		assertThat(response.status()).isEqualTo("EXPIRED");
		verify(paymentAttemptRepository).save(attempt);
	}

	@Test
	void create_ShouldReject_WhenCommerceDisabled() {
		doThrow(new IllegalStateException("commerce.module.not.enabled"))
				.when(commerceModuleAccessGuard).assertEnabledForCurrentTenant();

		assertThatThrownBy(() -> service.create(principal(), new CreatePaymentAttemptCommand("checkout-uid")))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("commerce.module.not.enabled");
	}

	private void stubPaymentEnabled() {
		when(configPropertyService.getBoolean(1L, "tenant_1", "commerce.payment.enabled", false)).thenReturn(true);
		when(configPropertyService.findRaw(1L, "tenant_1", "commerce.payment.provider")).thenReturn(Optional.empty());
	}

	private CommercePaymentAttempt attempt() {
		CommercePaymentAttempt attempt = new CommercePaymentAttempt();
		attempt.setId(40L);
		attempt.setUid("attempt-uid");
		attempt.setCustomer(customer());
		attempt.setCheckout(checkout());
		attempt.setProvider("iyzico");
		attempt.setStatus(CommercePaymentAttemptStatus.PENDING);
		attempt.setCurrencyIso("TRY");
		attempt.setSubtotal(BigDecimal.valueOf(200).setScale(2));
		attempt.setVatTotal(BigDecimal.valueOf(33.33));
		attempt.setShippingTotal(BigDecimal.ZERO.setScale(2));
		attempt.setTotal(BigDecimal.valueOf(200).setScale(2));
		attempt.setExpiresAt(LocalDateTime.now().plusMinutes(30));
		return attempt;
	}

	private CommerceCheckout checkout() {
		CommerceCheckout checkout = new CommerceCheckout();
		checkout.setId(30L);
		checkout.setUid("checkout-uid");
		checkout.setCustomer(customer());
		checkout.setCart(activeCart());
		checkout.setStatus(CommerceCheckoutStatus.READY);
		checkout.setCurrencyIso("TRY");
		checkout.setSubtotal(BigDecimal.valueOf(200).setScale(2));
		checkout.setVatTotal(BigDecimal.valueOf(33.33));
		checkout.setShippingTotal(BigDecimal.ZERO.setScale(2));
		checkout.setTotal(BigDecimal.valueOf(200).setScale(2));
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

	private CommerceCart activeCart() {
		CommerceCart cart = new CommerceCart();
		cart.setId(20L);
		cart.setUid("cart-uid");
		cart.setCustomer(customer());
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

	private CommerceVariantSnapshot variant(BigDecimal price, int stockQuantity) {
		return new CommerceVariantSnapshot(
				"product-uid",
				"PROD-1",
				true,
				true,
				"variant-uid",
				"VAR-1",
				true,
				price,
				BigDecimal.valueOf(20),
				stockQuantity);
	}

	private CommerceCustomer customer() {
		CommerceCustomer customer = new CommerceCustomer();
		customer.setId(10L);
		customer.setUid("customer-uid");
		customer.setEmail("user@example.com");
		return customer;
	}

	private CommerceCustomerPrincipal principal() {
		return new CommerceCustomerPrincipal(10L, "customer-uid", "user@example.com", 1L);
	}
}
