package com.backend.application.commerce;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.backend.application.commerce.CommerceProductVariantLookupPort.CommerceVariantSnapshot;
import com.backend.application.commerce.dto.CartMergeStatus;
import com.backend.application.commerce.dto.CartResponse;
import com.backend.application.commerce.dto.CartTotalsResponse;
import com.backend.domain.commerce.CommerceCart;
import com.backend.domain.commerce.CommerceCartItem;
import com.backend.domain.commerce.CommerceCartStatus;
import com.backend.domain.commerce.CommerceCustomer;
import com.backend.domain.commerce.repository.CommerceCartRepository;
import com.backend.domain.commerce.repository.CommerceCustomerRepository;
import com.backend.testutil.BaseServiceTest;

class CustomerCartBridgeServiceImplTest extends BaseServiceTest {

	@Mock private CommerceModuleAccessGuard commerceModuleAccessGuard;
	@Mock private CommerceCartRepository cartRepository;
	@Mock private CommerceCustomerRepository customerRepository;
	@Mock private CommerceProductVariantLookupPort productVariantLookupPort;
	@Mock private CartTokenService cartTokenService;
	@Mock private CartService cartService;

	@InjectMocks
	private CustomerCartBridgeServiceImpl service;

	@Test
	void mergeOnAuth_ShouldLinkAnonymousCart_WhenCustomerHasNoActiveCart() {
		CommerceCustomer customer = customer();
		CommerceCart source = activeCart("source-cart", "source-hash");
		source.addItem(item("variant-uid", 2));
		when(cartTokenService.hashToken("source-token")).thenReturn("source-hash");
		when(cartRepository.findByTokenHashAndStatusForUpdate("source-hash", CommerceCartStatus.ACTIVE))
				.thenReturn(Optional.of(source));
		when(customerRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(customer));
		when(cartRepository.findFirstByCustomerIdAndStatusAndExpiresAtAfterOrderByIdAsc(
				eq(10L),
				eq(CommerceCartStatus.ACTIVE),
				any(LocalDateTime.class)))
				.thenReturn(Optional.empty())
				.thenReturn(Optional.of(source));
		when(productVariantLookupPort.findByVariantUids(any()))
				.thenReturn(Map.of("variant-uid", variant(10)));
		when(cartRepository.save(source)).thenReturn(source);
		when(cartService.getCart(eq(null), any(CommerceCustomerPrincipal.class))).thenReturn(emptyCartResponse());

		var result = service.mergeOnAuth(customer, "source-token");

		assertThat(source.getCustomer()).isEqualTo(customer);
		assertThat(result.merge().status()).isEqualTo(CartMergeStatus.LINKED);
		assertThat(result.merge().mergedItemCount()).isEqualTo(1);
		assertThat(result.cart()).isNotNull();
		verify(cartRepository).save(source);
	}

	@Test
	void mergeOnAuth_ShouldSkipInvalidSourceItems_WhenCustomerHasNoActiveCart() {
		CommerceCustomer customer = customer();
		CommerceCart source = activeCart("source-cart", "source-hash");
		source.addItem(item("variant-uid", 2));
		when(cartTokenService.hashToken("source-token")).thenReturn("source-hash");
		when(cartRepository.findByTokenHashAndStatusForUpdate("source-hash", CommerceCartStatus.ACTIVE))
				.thenReturn(Optional.of(source));
		when(customerRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(customer));
		when(cartRepository.findFirstByCustomerIdAndStatusAndExpiresAtAfterOrderByIdAsc(
				eq(10L),
				eq(CommerceCartStatus.ACTIVE),
				any(LocalDateTime.class)))
				.thenReturn(Optional.empty());
		when(productVariantLookupPort.findByVariantUids(any()))
				.thenReturn(Map.of());
		when(cartRepository.save(source)).thenReturn(source);
		when(cartService.getCart(eq(null), any(CommerceCustomerPrincipal.class))).thenReturn(emptyCartResponse());

		var result = service.mergeOnAuth(customer, "source-token");

		assertThat(source.getCustomer()).isEqualTo(customer);
		assertThat(source.getItems()).isEmpty();
		assertThat(result.merge().status()).isEqualTo(CartMergeStatus.PARTIAL);
		assertThat(result.merge().mergedItemCount()).isZero();
		assertThat(result.merge().skippedItemCount()).isEqualTo(1);
		assertThat(result.merge().warningMessageKeys()).contains("commerce.cart.merge.items.skipped");
	}

	@Test
	void mergeOnAuth_ShouldMergeIntoExistingCustomerCartAndClearSource() {
		CommerceCart source = activeCart("source-cart", "source-hash");
		source.addItem(item("variant-uid", 2));
		CommerceCart target = activeCart("target-cart", "target-hash");
		target.setCustomer(customer());
		target.addItem(item("variant-uid", 1));
		when(cartTokenService.hashToken("source-token")).thenReturn("source-hash");
		when(cartRepository.findByTokenHashAndStatusForUpdate("source-hash", CommerceCartStatus.ACTIVE))
				.thenReturn(Optional.of(source));
		when(customerRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(customer()));
		when(cartRepository.findFirstByCustomerIdAndStatusAndExpiresAtAfterOrderByIdAsc(
				eq(10L),
				eq(CommerceCartStatus.ACTIVE),
				any(LocalDateTime.class)))
				.thenReturn(Optional.of(target));
		when(productVariantLookupPort.findByVariantUids(any()))
				.thenReturn(Map.of("variant-uid", variant(10)));
		when(cartService.getCart(eq(null), any(CommerceCustomerPrincipal.class))).thenReturn(emptyCartResponse());

		var result = service.mergeOnAuth(customer(), "source-token");

		assertThat(target.getItems().getFirst().getQuantity()).isEqualTo(3);
		assertThat(source.getStatus()).isEqualTo(CommerceCartStatus.CLEARED);
		assertThat(source.getItems()).isEmpty();
		assertThat(result.merge().status()).isEqualTo(CartMergeStatus.MERGED);
		assertThat(result.merge().mergedItemCount()).isEqualTo(1);
		verify(cartRepository).save(target);
		verify(cartRepository).save(source);
	}

	@Test
	void mergeOnAuth_ShouldSkipConflictingItemsAndStillClearSource() {
		CommerceCart source = activeCart("source-cart", "source-hash");
		source.addItem(item("variant-uid", 2));
		CommerceCart target = activeCart("target-cart", "target-hash");
		target.setCustomer(customer());
		target.addItem(item("variant-uid", 98));
		when(cartTokenService.hashToken("source-token")).thenReturn("source-hash");
		when(cartRepository.findByTokenHashAndStatusForUpdate("source-hash", CommerceCartStatus.ACTIVE))
				.thenReturn(Optional.of(source));
		when(customerRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(customer()));
		when(cartRepository.findFirstByCustomerIdAndStatusAndExpiresAtAfterOrderByIdAsc(
				eq(10L),
				eq(CommerceCartStatus.ACTIVE),
				any(LocalDateTime.class)))
				.thenReturn(Optional.of(target));
		when(productVariantLookupPort.findByVariantUids(any()))
				.thenReturn(Map.of("variant-uid", variant(200)));
		when(cartService.getCart(eq(null), any(CommerceCustomerPrincipal.class))).thenReturn(emptyCartResponse());

		var result = service.mergeOnAuth(customer(), "source-token");

		assertThat(target.getItems().getFirst().getQuantity()).isEqualTo(98);
		assertThat(source.getStatus()).isEqualTo(CommerceCartStatus.CLEARED);
		assertThat(result.merge().status()).isEqualTo(CartMergeStatus.PARTIAL);
		assertThat(result.merge().skippedItemCount()).isEqualTo(1);
		assertThat(result.merge().warningMessageKeys()).contains("commerce.cart.merge.items.skipped");
	}

	@Test
	void mergeOnAuth_ShouldReturnSourceNotFound_WhenTokenIsInvalid() {
		when(cartTokenService.hashToken("missing-token")).thenReturn("missing-hash");
		when(cartRepository.findByTokenHashAndStatusForUpdate("missing-hash", CommerceCartStatus.ACTIVE))
				.thenReturn(Optional.empty());

		var result = service.mergeOnAuth(customer(), "missing-token");

		assertThat(result.cart()).isNull();
		assertThat(result.merge().status()).isEqualTo(CartMergeStatus.SOURCE_NOT_FOUND);
	}

	private CommerceCart activeCart(String uid, String tokenHash) {
		CommerceCart cart = new CommerceCart();
		cart.setId((long) uid.hashCode());
		cart.setUid(uid);
		cart.setTokenHash(tokenHash);
		cart.setStatus(CommerceCartStatus.ACTIVE);
		cart.setExpiresAt(LocalDateTime.now().plusDays(1));
		return cart;
	}

	private CommerceCartItem item(String variantUid, int quantity) {
		CommerceCartItem item = new CommerceCartItem();
		item.setProductUid("product-uid");
		item.setProductSku("PROD-1");
		item.setVariantUid(variantUid);
		item.setVariantSku("VAR-1");
		item.setQuantity(quantity);
		item.setUnitGrossPrice(BigDecimal.valueOf(100));
		item.setVatRate(BigDecimal.valueOf(20));
		return item;
	}

	private CommerceVariantSnapshot variant(int stockQuantity) {
		return new CommerceVariantSnapshot(
				"product-uid",
				"PROD-1",
				true,
				true,
				"variant-uid",
				"VAR-1",
				true,
				BigDecimal.valueOf(100),
				BigDecimal.valueOf(20),
				stockQuantity);
	}

	private CommerceCustomer customer() {
		CommerceCustomer customer = new CommerceCustomer();
		customer.setId(10L);
		customer.setUid("customer-uid");
		customer.setEmail("user@example.com");
		customer.setEmailNormalized("user@example.com");
		customer.setFirstName("Emre");
		customer.setLastName("Erkesikbas");
		customer.setPhone("+905551112233");
		return customer;
	}

	private CartResponse emptyCartResponse() {
		return new CartResponse(
				null,
				"cart-uid",
				"ACTIVE",
				LocalDateTime.now().plusDays(1),
				List.of(),
				new CartTotalsResponse("TRY", 0, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
	}
}
