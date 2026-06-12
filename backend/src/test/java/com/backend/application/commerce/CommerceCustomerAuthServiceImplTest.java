package com.backend.application.commerce;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.backend.application.commerce.dto.LoginCommerceCustomerCommand;
import com.backend.application.commerce.dto.RegisterCommerceCustomerCommand;
import com.backend.application.commerce.dto.CartMergeResponse;
import com.backend.domain.commerce.CommerceCustomer;
import com.backend.domain.commerce.CommerceCustomerConsent;
import com.backend.domain.commerce.CommerceCustomerRefreshToken;
import com.backend.domain.commerce.repository.CommerceCustomerConsentRepository;
import com.backend.domain.commerce.repository.CommerceCustomerRefreshTokenRepository;
import com.backend.domain.commerce.repository.CommerceCustomerRepository;
import com.backend.domain.exception.DuplicateEntityException;
import com.backend.domain.port.TenantContextPort;
import com.backend.testutil.BaseServiceTest;

class CommerceCustomerAuthServiceImplTest extends BaseServiceTest {

	@Mock private CommerceModuleAccessGuard commerceModuleAccessGuard;
	@Mock private CommerceCustomerRepository customerRepository;
	@Mock private CommerceCustomerConsentRepository consentRepository;
	@Mock private CommerceCustomerRefreshTokenRepository refreshTokenRepository;
	@Mock private CommerceCustomerTokenPort tokenPort;
	@Mock private CommerceCustomerTokenHashService tokenHashService;
	@Mock private CommerceCustomerRateLimitService rateLimitService;
	@Mock private CustomerCartBridgeService customerCartBridgeService;
	@Mock private TenantContextPort tenantContext;
	@Mock private PasswordEncoder passwordEncoder;

	@InjectMocks
	private CommerceCustomerAuthServiceImpl service;

	@BeforeEach
	void setUp() {
		lenient().when(tenantContext.getTenantId()).thenReturn("1");
		lenient().when(passwordEncoder.encode("Password123")).thenReturn("hashed-password");
		lenient().when(passwordEncoder.matches("Password123", "hashed-password")).thenReturn(true);
		lenient().when(tokenPort.createAccessToken(any(CommerceCustomer.class), any())).thenReturn("access-token");
		lenient().when(tokenPort.createRefreshToken(any(CommerceCustomer.class), any(), any(Boolean.class))).thenReturn("refresh-token");
		lenient().when(tokenPort.getAccessTokenExpiration()).thenReturn(86_400_000L);
		lenient().when(tokenPort.getRefreshTokenExpiration(false)).thenReturn(604_800_000L);
		lenient().when(tokenHashService.hashToken("refresh-token")).thenReturn("refresh-token-hash");
		lenient().when(customerCartBridgeService.mergeOnAuth(any(CommerceCustomer.class), any()))
				.thenReturn(new CustomerCartBridgeService.CustomerCartBridgeResult(null, CartMergeResponse.none()));
		lenient().when(customerRepository.save(any(CommerceCustomer.class))).thenAnswer(invocation -> {
			CommerceCustomer customer = invocation.getArgument(0);
			customer.setId(10L);
			customer.setUid("customer-uid");
			return customer;
		});
		lenient().when(refreshTokenRepository.save(any(CommerceCustomerRefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));
	}

	@Test
	void register_ShouldCreateCustomerConsentsAndRefreshToken() {
		when(customerRepository.existsByEmailNormalized("user@example.com")).thenReturn(false);
		RegisterCommerceCustomerCommand command = registerCommand();

		var result = service.register(command);

		assertThat(result.response().accessToken()).isEqualTo("access-token");
		assertThat(result.refreshToken()).isEqualTo("refresh-token");
		ArgumentCaptor<CommerceCustomer> customerCaptor = ArgumentCaptor.forClass(CommerceCustomer.class);
		verify(customerRepository).save(customerCaptor.capture());
		assertThat(customerCaptor.getValue().getEmailNormalized()).isEqualTo("user@example.com");
		assertThat(customerCaptor.getValue().getPasswordHash()).isEqualTo("hashed-password");
		ArgumentCaptor<List<CommerceCustomerConsent>> consentCaptor = ArgumentCaptor.forClass(List.class);
		verify(consentRepository).saveAll(consentCaptor.capture());
		assertThat(consentCaptor.getValue()).hasSize(5);
		ArgumentCaptor<CommerceCustomerRefreshToken> tokenCaptor = ArgumentCaptor.forClass(CommerceCustomerRefreshToken.class);
		verify(refreshTokenRepository).save(tokenCaptor.capture());
		assertThat(tokenCaptor.getValue().getTokenHash()).isEqualTo("refresh-token-hash");
	}

	@Test
	void register_ShouldRejectDuplicateEmail() {
		when(customerRepository.existsByEmailNormalized("user@example.com")).thenReturn(true);

		assertThatThrownBy(() -> service.register(registerCommand()))
				.isInstanceOf(DuplicateEntityException.class)
				.hasMessage("commerce.customer.email.duplicate");
	}

	@Test
	void login_ShouldRejectUnknownCustomerWithGenericCredentialsError() {
		when(customerRepository.findByEmailNormalized("user@example.com")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.login(loginCommand()))
				.isInstanceOf(BadCredentialsException.class);
	}

	@Test
	void login_ShouldInvokeCartBridge_WhenCartTokenIsPresent() {
		when(customerRepository.findByEmailNormalized("user@example.com")).thenReturn(Optional.of(customer()));

		service.login(new LoginCommerceCustomerCommand(
				"User@Example.com",
				"Password123",
				false,
				"device",
				"source-cart-token",
				"127.0.0.1",
				"JUnit"));

		verify(customerCartBridgeService).mergeOnAuth(any(CommerceCustomer.class), eq("source-cart-token"));
	}

	@Test
	void refresh_ShouldRotateStoredToken() {
		CommerceCustomer customer = customer();
		CommerceCustomerRefreshToken stored = new CommerceCustomerRefreshToken();
		stored.setCustomer(customer);
		stored.setExpiresAt(LocalDateTime.now().plusDays(1));
		when(tokenPort.validateRefreshToken("old-refresh")).thenReturn(true);
		when(tokenPort.getTenantId("old-refresh")).thenReturn(1L);
		when(tokenPort.isRememberMeToken("old-refresh")).thenReturn(false);
		when(tokenHashService.hashToken("old-refresh")).thenReturn("old-refresh-hash");
		when(refreshTokenRepository.findByTokenHash("old-refresh-hash")).thenReturn(Optional.of(stored));
		when(refreshTokenRepository.revokeByTokenHash("old-refresh-hash")).thenReturn(1);

		var result = service.refresh("old-refresh", null, "127.0.0.1", "JUnit");

		assertThat(result.refreshToken()).isEqualTo("refresh-token");
		verify(refreshTokenRepository).revokeByTokenHash("old-refresh-hash");
		verify(refreshTokenRepository).save(any(CommerceCustomerRefreshToken.class));
	}

	@Test
	void refresh_ShouldRejectReplay_WhenStoredTokenWasAlreadyRevokedConcurrently() {
		CommerceCustomerRefreshToken stored = new CommerceCustomerRefreshToken();
		stored.setCustomer(customer());
		stored.setExpiresAt(LocalDateTime.now().plusDays(1));
		when(tokenPort.validateRefreshToken("old-refresh")).thenReturn(true);
		when(tokenPort.getTenantId("old-refresh")).thenReturn(1L);
		when(tokenHashService.hashToken("old-refresh")).thenReturn("old-refresh-hash");
		when(refreshTokenRepository.findByTokenHash("old-refresh-hash")).thenReturn(Optional.of(stored));
		when(refreshTokenRepository.revokeByTokenHash("old-refresh-hash")).thenReturn(0);

		assertThatThrownBy(() -> service.refresh("old-refresh", null, "127.0.0.1", "JUnit"))
				.isInstanceOf(BadCredentialsException.class)
				.hasMessage("commerce.customer.auth.refresh.invalid");
	}

	private RegisterCommerceCustomerCommand registerCommand() {
		return new RegisterCommerceCustomerCommand(
				"User@Example.com",
				"Password123",
				"Emre",
				"Erkesikbas",
				"+905551112233",
				null,
				null,
				true,
				true,
				true,
				false,
				false,
				false,
				"device",
				"storefront",
				null,
				"127.0.0.1",
				"JUnit");
	}

	private LoginCommerceCustomerCommand loginCommand() {
		return new LoginCommerceCustomerCommand("User@Example.com", "Password123", false, "device", null, "127.0.0.1", "JUnit");
	}

	private CommerceCustomer customer() {
		CommerceCustomer customer = new CommerceCustomer();
		customer.setId(10L);
		customer.setUid("customer-uid");
		customer.setEmail("user@example.com");
		customer.setEmailNormalized("user@example.com");
		customer.setPasswordHash("hashed-password");
		customer.setFirstName("Emre");
		customer.setLastName("Erkesikbas");
		customer.setPhone("+905551112233");
		return customer;
	}
}
