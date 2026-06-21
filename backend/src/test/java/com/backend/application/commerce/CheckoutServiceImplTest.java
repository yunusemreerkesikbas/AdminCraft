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
import com.backend.application.commerce.dto.CheckoutAddressSelectionCommand;
import com.backend.application.commerce.dto.CheckoutLegalResponse;
import com.backend.application.service.config.ConfigPropertyService;
import com.backend.domain.commerce.CommerceCart;
import com.backend.domain.commerce.CommerceCartItem;
import com.backend.domain.commerce.CommerceCartStatus;
import com.backend.domain.commerce.CommerceCheckout;
import com.backend.domain.commerce.CommerceCheckoutItem;
import com.backend.domain.commerce.CommerceCheckoutStatus;
import com.backend.domain.commerce.CommerceCustomer;
import com.backend.domain.commerce.CommerceCustomerAddress;
import com.backend.domain.commerce.CommerceCustomerInvoiceType;
import com.backend.domain.commerce.exception.CommerceDomainException;
import com.backend.domain.commerce.repository.CommerceCartRepository;
import com.backend.domain.commerce.repository.CommerceCheckoutRepository;
import com.backend.domain.commerce.repository.CommerceCustomerAddressRepository;
import com.backend.domain.commerce.repository.CommerceCustomerRepository;
import com.backend.domain.enums.Currency;
import com.backend.domain.port.TenantContextPort;
import com.backend.testutil.BaseServiceTest;
import com.fasterxml.jackson.databind.ObjectMapper;

class CheckoutServiceImplTest extends BaseServiceTest {

	@Mock private CommerceModuleAccessGuard commerceModuleAccessGuard;
	@Mock private CommerceCartRepository cartRepository;
	@Mock private CommerceCheckoutRepository checkoutRepository;
	@Mock private CommerceCustomerRepository customerRepository;
	@Mock private CommerceCustomerAddressRepository addressRepository;
	@Mock private CommerceProductVariantLookupPort productVariantLookupPort;
	@Mock private CommerceLegalService commerceLegalService;
	@Mock private ConfigPropertyService configPropertyService;
	@Mock private TenantContextPort tenantContext;

	private CheckoutServiceImpl service;

	@BeforeEach
	void setUp() {
		service = new CheckoutServiceImpl(
				commerceModuleAccessGuard,
				cartRepository,
				checkoutRepository,
				customerRepository,
				addressRepository,
				productVariantLookupPort,
				commerceLegalService,
				configPropertyService,
				tenantContext,
				new ObjectMapper());
		lenient().when(tenantContext.getTenantId()).thenReturn("1");
		lenient().when(tenantContext.getTenantDbName()).thenReturn("tenant_1");
		lenient().when(tenantContext.getCurrency()).thenReturn(Currency.TRY);
		lenient().when(configPropertyService.getBoolean(1L, "tenant_1", "commerce.shipping.enabled", true)).thenReturn(true);
		lenient().when(configPropertyService.getDecimal(1L, "tenant_1", "commerce.shipping.standard_fee", BigDecimal.ZERO))
				.thenReturn(BigDecimal.ZERO);
		lenient().when(configPropertyService.findRaw(1L, "tenant_1", "commerce.shipping.free_shipping_threshold"))
				.thenReturn(Optional.empty());
		lenient().when(commerceLegalService.legalForCheckout(any(), any()))
				.thenReturn(new CheckoutLegalResponse(true, "EN", java.util.List.of(), java.util.List.of()));
	}

	@Test
	void start_ShouldCreateCheckoutFromCustomerCartWithDefaultAddresses() {
		stubCustomerCartAndAddresses();
		when(productVariantLookupPort.findByVariantUids(anyCollection()))
				.thenReturn(Map.of("variant-uid", variant(BigDecimal.valueOf(100), 5)));
		when(checkoutRepository.save(any(CommerceCheckout.class))).thenAnswer(invocation -> {
			CommerceCheckout checkout = invocation.getArgument(0);
			checkout.setUid("checkout-uid");
			checkout.getItems().forEach(item -> item.setUid("checkout-item-uid"));
			return checkout;
		});

		var response = service.start(principal(), new CheckoutAddressSelectionCommand(null, null, null));

		assertThat(response.checkoutUid()).isEqualTo("checkout-uid");
		assertThat(response.status()).isEqualTo("READY");
		assertThat(response.items()).hasSize(1);
		assertThat(response.totals().subtotal()).isEqualByComparingTo("200.00");
		assertThat(response.totals().shippingTotal()).isEqualByComparingTo("0.00");
		assertThat(response.totals().total()).isEqualByComparingTo("200.00");
		assertThat(response.validation().valid()).isTrue();
		verify(checkoutRepository).expireOpenCheckouts(eq(10L), anyCollection(), any(LocalDateTime.class));
	}

	@Test
	void start_ShouldApplyFreeShippingThreshold_WhenSubtotalMeetsThreshold() {
		stubCustomerCartAndAddresses();
		when(configPropertyService.getDecimal(1L, "tenant_1", "commerce.shipping.standard_fee", BigDecimal.ZERO))
				.thenReturn(BigDecimal.valueOf(20));
		when(configPropertyService.findRaw(1L, "tenant_1", "commerce.shipping.free_shipping_threshold"))
				.thenReturn(Optional.of("100"));
		when(productVariantLookupPort.findByVariantUids(anyCollection()))
				.thenReturn(Map.of("variant-uid", variant(BigDecimal.valueOf(100), 5)));
		when(checkoutRepository.save(any(CommerceCheckout.class))).thenAnswer(invocation -> invocation.getArgument(0));

		var response = service.start(principal(), new CheckoutAddressSelectionCommand(null, null, null));

		assertThat(response.totals().shippingTotal()).isEqualByComparingTo("0.00");
	}

	@Test
	void start_ShouldUseDeliveryAddressForBilling_WhenBillingSameAsDelivery() {
		CommerceCustomerAddress delivery = address("delivery-uid", true, false);
		stubCustomerAndCart();
		when(addressRepository.findByCustomerIdAndUid(10L, "delivery-uid")).thenReturn(Optional.of(delivery));
		when(productVariantLookupPort.findByVariantUids(anyCollection()))
				.thenReturn(Map.of("variant-uid", variant(BigDecimal.valueOf(100), 5)));
		when(checkoutRepository.save(any(CommerceCheckout.class))).thenAnswer(invocation -> invocation.getArgument(0));

		var response = service.start(principal(), new CheckoutAddressSelectionCommand("delivery-uid", null, true));

		assertThat(response.deliveryAddress().uid()).isEqualTo("delivery-uid");
		assertThat(response.billingAddress().uid()).isEqualTo("delivery-uid");
	}

	@Test
	void start_ShouldReject_WhenDefaultDeliveryAddressMissing() {
		stubCustomerAndCart();
		when(addressRepository.findFirstByCustomerIdAndDefaultDeliveryTrueOrderByIdAsc(10L))
				.thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.start(principal(), new CheckoutAddressSelectionCommand(null, null, null)))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("commerce.checkout.delivery.address.required");
	}

	@Test
	void start_ShouldReject_WhenCartIsEmpty() {
		CommerceCart cart = activeCart();
		cart.getItems().clear();
		when(customerRepository.findById(10L)).thenReturn(Optional.of(customer()));
		when(cartRepository.findFirstByCustomerIdAndStatusAndExpiresAtAfterOrderByIdAsc(
				eq(10L),
				eq(CommerceCartStatus.ACTIVE),
				any(LocalDateTime.class)))
				.thenReturn(Optional.of(cart));

		assertThatThrownBy(() -> service.start(principal(), new CheckoutAddressSelectionCommand(null, null, null)))
				.isInstanceOf(CommerceDomainException.class)
				.hasMessageContaining("commerce.checkout.cart.empty");
	}

	@Test
	void start_ShouldReject_WhenStockIsInsufficient() {
		stubCustomerCartAndAddresses();
		when(productVariantLookupPort.findByVariantUids(anyCollection()))
				.thenReturn(Map.of("variant-uid", variant(BigDecimal.valueOf(100), 1)));

		assertThatThrownBy(() -> service.start(principal(), new CheckoutAddressSelectionCommand(null, null, null)))
				.isInstanceOf(CommerceDomainException.class)
				.hasMessageContaining("commerce.checkout.stock.insufficient");
	}

	@Test
	void getCurrent_ShouldReturnInvalidWithoutMutating_WhenPriceChanged() {
		CommerceCheckout checkout = checkout();
		when(checkoutRepository.findFirstByCustomerIdAndStatusInAndExpiresAtAfterOrderByIdDesc(
				eq(10L),
				anyCollection(),
				any(LocalDateTime.class)))
				.thenReturn(Optional.of(checkout));
		when(productVariantLookupPort.findByVariantUids(anyCollection()))
				.thenReturn(Map.of("variant-uid", variant(BigDecimal.valueOf(120), 5)));

		var response = service.getCurrent(principal());

		assertThat(response.validation().valid()).isFalse();
		assertThat(response.validation().priceChanged()).isTrue();
		assertThat(response.validation().warningMessageKeys()).contains("commerce.checkout.price.changed");
		verify(checkoutRepository, never()).save(any());
		verify(checkoutRepository, never()).expireOpenCheckouts(any(), anyCollection(), any());
	}

	@Test
	void updateAddresses_ShouldUpdateAddressSnapshotsAndRecalculateShipping() {
		CommerceCheckout checkout = checkout();
		CommerceCustomerAddress delivery = address("new-delivery", false, false);
		CommerceCustomerAddress billing = address("new-billing", false, false);
		when(checkoutRepository.findByCustomerIdAndUid(10L, "checkout-uid")).thenReturn(Optional.of(checkout));
		when(addressRepository.findByCustomerIdAndUid(10L, "new-delivery")).thenReturn(Optional.of(delivery));
		when(addressRepository.findByCustomerIdAndUid(10L, "new-billing")).thenReturn(Optional.of(billing));
		when(configPropertyService.getDecimal(1L, "tenant_1", "commerce.shipping.standard_fee", BigDecimal.ZERO))
				.thenReturn(BigDecimal.TEN);
		when(productVariantLookupPort.findByVariantUids(anyCollection()))
				.thenReturn(Map.of("variant-uid", variant(BigDecimal.valueOf(100), 5)));
		when(checkoutRepository.save(any(CommerceCheckout.class))).thenAnswer(invocation -> invocation.getArgument(0));

		var response = service.updateAddresses(
				principal(),
				"checkout-uid",
				new CheckoutAddressSelectionCommand("new-delivery", "new-billing", false));

		assertThat(response.deliveryAddress().uid()).isEqualTo("new-delivery");
		assertThat(response.billingAddress().uid()).isEqualTo("new-billing");
		assertThat(response.totals().shippingTotal()).isEqualByComparingTo("10.00");
	}

	@Test
	void updateAddresses_ShouldReject_WhenCheckoutStatusExpiredEvenIfExpiresAtFuture() {
		CommerceCheckout checkout = checkout();
		checkout.setStatus(CommerceCheckoutStatus.EXPIRED);
		checkout.setExpiresAt(LocalDateTime.now().plusHours(1));
		when(checkoutRepository.findByCustomerIdAndUid(10L, "checkout-uid")).thenReturn(Optional.of(checkout));

		assertThatThrownBy(() -> service.updateAddresses(
				principal(),
				"checkout-uid",
				new CheckoutAddressSelectionCommand("new-delivery", "new-billing", false)))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("commerce.checkout.expired");
	}

	@Test
	void start_ShouldReject_WhenCommerceDisabled() {
		doThrow(new IllegalStateException("commerce.module.not.enabled"))
				.when(commerceModuleAccessGuard).assertEnabledForCurrentTenant();

		assertThatThrownBy(() -> service.start(principal(), new CheckoutAddressSelectionCommand(null, null, null)))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("commerce.module.not.enabled");
	}

	private void stubCustomerCartAndAddresses() {
		stubCustomerAndCart();
		when(addressRepository.findFirstByCustomerIdAndDefaultDeliveryTrueOrderByIdAsc(10L))
				.thenReturn(Optional.of(address("delivery-uid", true, false)));
		when(addressRepository.findFirstByCustomerIdAndDefaultBillingTrueOrderByIdAsc(10L))
				.thenReturn(Optional.of(address("billing-uid", false, true)));
	}

	private void stubCustomerAndCart() {
		when(customerRepository.findById(10L)).thenReturn(Optional.of(customer()));
		when(cartRepository.findFirstByCustomerIdAndStatusAndExpiresAtAfterOrderByIdAsc(
				eq(10L),
				eq(CommerceCartStatus.ACTIVE),
				any(LocalDateTime.class)))
				.thenReturn(Optional.of(activeCart()));
	}

	private CommerceCheckout checkout() {
		CommerceCheckout checkout = new CommerceCheckout();
		checkout.setId(30L);
		checkout.setUid("checkout-uid");
		checkout.setStatus(CommerceCheckoutStatus.READY);
		checkout.setCustomer(customer());
		checkout.setCart(activeCart());
		checkout.setCurrencyIso("TRY");
		checkout.setSubtotal(BigDecimal.valueOf(200).setScale(2));
		checkout.setVatTotal(BigDecimal.valueOf(33.33));
		checkout.setShippingTotal(BigDecimal.ZERO.setScale(2));
		checkout.setTotal(BigDecimal.valueOf(200).setScale(2));
		checkout.setShippingMethodCode("STANDARD");
		checkout.setShippingMethodName("commerce.shipping.method.standard");
		checkout.setDeliveryAddressUid("delivery-uid");
		checkout.setBillingAddressUid("billing-uid");
		checkout.setDeliveryAddressSnapshot(addressJson("delivery-uid"));
		checkout.setBillingAddressSnapshot(addressJson("billing-uid"));
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
		cart.setStatus(CommerceCartStatus.ACTIVE);
		cart.setExpiresAt(LocalDateTime.now().plusDays(1));
		cart.setCustomer(customer());
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

	private CommerceCustomerAddress address(String uid, boolean delivery, boolean billing) {
		CommerceCustomerAddress address = new CommerceCustomerAddress();
		address.setId((long) uid.hashCode());
		address.setUid(uid);
		address.setCustomer(customer());
		address.setFirstName("Emre");
		address.setLastName("Erkesikbas");
		address.setPhone("+905551112233");
		address.setCountryIso("TR");
		address.setCity("Istanbul");
		address.setDistrict("Kadikoy");
		address.setAddressLine1("Address line");
		address.setDefaultDelivery(delivery);
		address.setDefaultBilling(billing);
		address.setInvoiceType(CommerceCustomerInvoiceType.INDIVIDUAL);
		return address;
	}

	private String addressJson(String uid) {
		return """
				{"uid":"%s","label":null,"firstName":"Emre","lastName":"Erkesikbas","phone":"+905551112233","countryIso":"TR","city":"Istanbul","district":"Kadikoy","addressLine1":"Address line","addressLine2":null,"postalCode":null,"invoiceType":"INDIVIDUAL","companyName":null,"taxNumber":null,"taxOffice":null,"invoiceIdentityNumber":null}
				""".formatted(uid).trim();
	}

	private CommerceCustomerPrincipal principal() {
		return new CommerceCustomerPrincipal(10L, "customer-uid", "user@example.com", 1L);
	}
}
