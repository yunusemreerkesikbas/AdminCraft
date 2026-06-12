package com.backend.application.commerce;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.backend.application.commerce.CommerceProductVariantLookupPort.CommerceVariantSnapshot;
import com.backend.domain.commerce.CommerceCart;
import com.backend.domain.commerce.CommerceCartItem;
import com.backend.domain.commerce.CommerceCartStatus;
import com.backend.domain.commerce.CommerceCustomer;
import com.backend.domain.commerce.exception.CommerceDomainException;
import com.backend.domain.exception.EntityNotFoundException;
import com.backend.domain.commerce.repository.CommerceCartItemRepository;
import com.backend.domain.commerce.repository.CommerceCartRepository;
import com.backend.domain.commerce.repository.CommerceCustomerRepository;
import com.backend.testutil.BaseServiceTest;

class CartServiceImplTest extends BaseServiceTest {

    @Mock
    private CommerceModuleAccessGuard commerceModuleAccessGuard;

    @Mock
    private CommerceCartRepository cartRepository;

    @Mock
    private CommerceCartItemRepository cartItemRepository;

	@Mock
	private CommerceCustomerRepository customerRepository;

    @Mock
    private CommerceProductVariantLookupPort productVariantLookupPort;

    @Mock
    private CartTokenService cartTokenService;

    @InjectMocks
    private CartServiceImpl cartService;

    @BeforeEach
    void setUp() {
		lenient().when(cartTokenService.generateToken()).thenReturn("raw-cart-token");
		lenient().when(cartTokenService.hashToken(any(String.class))).thenAnswer(inv -> {
            String token = inv.getArgument(0);
            if ("cart-token".equals(token)) {
                return "hash-cart-token";
            }
            return "sha256-token-hash";
        });
		lenient().when(cartRepository.save(any(CommerceCart.class))).thenAnswer(inv -> {
            CommerceCart cart = inv.getArgument(0);
            if (cart.getId() == null) {
                cart.setId(1L);
            }
            if (cart.getUid() == null) {
                cart.setUid("cart-uid");
            }
            return cart;
        });
		lenient().when(productVariantLookupPort.findByVariantUids(any()))
				.thenReturn(Map.of("variant-uid", sellableVariant(BigDecimal.valueOf(100), 5)));
    }

    @Test
    void createCart_ShouldReturnRawTokenAndPersistOnlyTokenHash() {
        cartService.createCart();

        ArgumentCaptor<CommerceCart> cartCaptor = ArgumentCaptor.forClass(CommerceCart.class);
        verify(cartRepository).save(cartCaptor.capture());
        CommerceCart savedCart = cartCaptor.getValue();

        assertThat(savedCart.getTokenHash()).isEqualTo("sha256-token-hash");
        assertThat(savedCart.getTokenHash()).doesNotContain("raw-cart-token");
        assertThat(savedCart.getStatus()).isEqualTo(CommerceCartStatus.ACTIVE);
        assertThat(savedCart.getExpiresAt()).isAfter(LocalDateTime.now().plusDays(29));
    }

    @Test
    void addItem_ShouldCreateCart_WhenTokenMissing() {
        when(productVariantLookupPort.findByVariantUid("variant-uid"))
                .thenReturn(Optional.of(sellableVariant(BigDecimal.valueOf(100), 5)));

        var response = cartService.addItem(null, "variant-uid", 2);

        assertThat(response.cartToken()).isEqualTo("raw-cart-token");
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().getFirst().quantity()).isEqualTo(2);
        assertThat(response.totals().total()).isEqualByComparingTo("200.00");
    }

    @Test
    void addItem_ShouldMergeQuantity_WhenVariantAlreadyExists() {
        CommerceCart cart = activeCart();
        CommerceCartItem item = cartItem("item-uid", "variant-uid", 1, BigDecimal.valueOf(100));
        cart.addItem(item);
        when(cartRepository.findByTokenHashAndStatus("hash-cart-token", CommerceCartStatus.ACTIVE))
                .thenReturn(Optional.of(cart));
        when(productVariantLookupPort.findByVariantUid("variant-uid"))
                .thenReturn(Optional.of(sellableVariant(BigDecimal.valueOf(100), 5)));

        var response = cartService.addItem("cart-token", "variant-uid", 2);

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().getFirst().quantity()).isEqualTo(3);
    }

	@Test
	void addItem_ShouldTreatNullExistingQuantityAsZero_WhenMergingVariant() {
		CommerceCart cart = activeCart();
		CommerceCartItem item = cartItem("item-uid", "variant-uid", 1, BigDecimal.valueOf(100));
		item.setQuantity(null);
		cart.addItem(item);
		when(cartRepository.findByTokenHashAndStatus("hash-cart-token", CommerceCartStatus.ACTIVE))
				.thenReturn(Optional.of(cart));
		when(productVariantLookupPort.findByVariantUid("variant-uid"))
				.thenReturn(Optional.of(sellableVariant(BigDecimal.valueOf(100), 5)));

		var response = cartService.addItem("cart-token", "variant-uid", 2);

		assertThat(response.items()).hasSize(1);
		assertThat(response.items().getFirst().quantity()).isEqualTo(2);
	}

    @Test
    void addItem_ShouldRejectInvalidQuantity() {
        assertThatThrownBy(() -> cartService.addItem(null, "variant-uid", 0))
                .isInstanceOf(CommerceDomainException.class)
                .hasMessage("commerce.cart.quantity.invalid");
    }

    @Test
    void addItem_ShouldRejectNonSellableVariant() {
        when(productVariantLookupPort.findByVariantUid("variant-uid"))
                .thenReturn(Optional.of(new CommerceVariantSnapshot(
                        "product-uid",
                        "PROD-1",
                        false,
                        true,
                        "variant-uid",
                        "VAR-1",
                        true,
                        BigDecimal.valueOf(100),
                        BigDecimal.valueOf(20),
                        5)));

        assertThatThrownBy(() -> cartService.addItem(null, "variant-uid", 1))
                .isInstanceOf(CommerceDomainException.class)
                .hasMessage("commerce.cart.variant.not.sellable");
    }

    @Test
    void addItem_ShouldRejectOutOfStockQuantity() {
        when(productVariantLookupPort.findByVariantUid("variant-uid"))
                .thenReturn(Optional.of(sellableVariant(BigDecimal.valueOf(100), 1)));

        assertThatThrownBy(() -> cartService.addItem(null, "variant-uid", 2))
                .isInstanceOf(CommerceDomainException.class)
                .hasMessage("commerce.cart.stock.insufficient");
    }

    @Test
    void getCart_ShouldExposePriceChanged_WhenCurrentPriceDiffersFromSnapshot() {
        CommerceCart cart = activeCart();
        cart.addItem(cartItem("item-uid", "variant-uid", 1, BigDecimal.valueOf(100)));
        when(cartRepository.findByTokenHashAndStatus("hash-cart-token", CommerceCartStatus.ACTIVE))
                .thenReturn(Optional.of(cart));
		when(productVariantLookupPort.findByVariantUids(any()))
				.thenReturn(Map.of("variant-uid", sellableVariant(BigDecimal.valueOf(120), 5)));

        var response = cartService.getCart("cart-token");

        assertThat(response.items().getFirst().priceChanged()).isTrue();
        assertThat(response.items().getFirst().unitPrice()).isEqualByComparingTo("100");
        assertThat(response.items().getFirst().currentUnitPrice()).isEqualByComparingTo("120");
    }

    @Test
    void getCart_ShouldRejectExpiredCart() {
        CommerceCart cart = activeCart();
        cart.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        when(cartRepository.findByTokenHashAndStatus("hash-cart-token", CommerceCartStatus.ACTIVE))
                .thenReturn(Optional.of(cart));

        assertThatThrownBy(() -> cartService.getCart("cart-token"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("expired token");
    }

	@Test
	void getCart_ShouldReturnCustomerCart_WhenCustomerPrincipalPresentAndTokenMissing() {
		CommerceCart cart = activeCart();
		cart.setCustomer(customer());
		when(cartRepository.findFirstByCustomerIdAndStatusAndExpiresAtAfterOrderByIdAsc(
				eq(10L),
				eq(CommerceCartStatus.ACTIVE),
				any(LocalDateTime.class)))
				.thenReturn(Optional.of(cart));

		var response = cartService.getCart(null, principal());

		assertThat(response.cartToken()).isNull();
		assertThat(response.cartUid()).isEqualTo("cart-uid");
	}

	@Test
	void addItem_ShouldCreateCustomerCart_WhenCustomerPrincipalPresentAndNoActiveCartExists() {
		when(customerRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(customer()));
		when(cartRepository.findFirstByCustomerIdAndStatusAndExpiresAtAfterOrderByIdAsc(
				eq(10L),
				eq(CommerceCartStatus.ACTIVE),
				any(LocalDateTime.class)))
				.thenReturn(Optional.empty());
		when(productVariantLookupPort.findByVariantUid("variant-uid"))
				.thenReturn(Optional.of(sellableVariant(BigDecimal.valueOf(100), 5)));

		var response = cartService.addItem("stale-token", principal(), "variant-uid", 2);

		assertThat(response.cartToken()).isNull();
		assertThat(response.items()).hasSize(1);
		ArgumentCaptor<CommerceCart> cartCaptor = ArgumentCaptor.forClass(CommerceCart.class);
		verify(cartRepository).save(cartCaptor.capture());
		assertThat(cartCaptor.getValue().getCustomer().getId()).isEqualTo(10L);
	}

    @Test
    void createCart_ShouldReject_WhenCommerceModuleDisabled() {
        doThrow(new IllegalStateException("commerce.module.not.enabled"))
                .when(commerceModuleAccessGuard).assertEnabledForCurrentTenant();

        assertThatThrownBy(() -> cartService.createCart())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("commerce.module.not.enabled");
    }

    private CommerceCart activeCart() {
        CommerceCart cart = new CommerceCart();
        cart.setId(1L);
        cart.setUid("cart-uid");
        cart.setTokenHash("hash-cart-token");
        cart.setStatus(CommerceCartStatus.ACTIVE);
        cart.setExpiresAt(LocalDateTime.now().plusDays(1));
        return cart;
    }

    private CommerceCartItem cartItem(String itemUid, String variantUid, int quantity, BigDecimal unitPrice) {
        CommerceCartItem item = new CommerceCartItem();
        item.setId(10L);
        item.setUid(itemUid);
        item.setProductUid("product-uid");
        item.setProductSku("PROD-1");
        item.setVariantUid(variantUid);
        item.setVariantSku("VAR-1");
        item.setQuantity(quantity);
        item.setUnitGrossPrice(unitPrice);
        item.setVatRate(BigDecimal.valueOf(20));
        return item;
    }

    private CommerceVariantSnapshot sellableVariant(BigDecimal price, int stockQuantity) {
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

	private CommerceCustomerPrincipal principal() {
		return new CommerceCustomerPrincipal(10L, "customer-uid", "user@example.com", 1L);
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
}
