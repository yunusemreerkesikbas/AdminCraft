package com.backend.application.commerce;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.application.commerce.dto.CommerceCustomerAuthResponse;
import com.backend.application.commerce.dto.CommerceCustomerResponse;
import com.backend.application.commerce.dto.LoginCommerceCustomerCommand;
import com.backend.application.commerce.dto.RegisterCommerceCustomerCommand;
import com.backend.domain.commerce.CommerceCustomer;
import com.backend.domain.commerce.CommerceCustomerConsent;
import com.backend.domain.commerce.CommerceCustomerConsentType;
import com.backend.domain.commerce.CommerceCustomerGender;
import com.backend.domain.commerce.CommerceCustomerRefreshToken;
import com.backend.domain.commerce.exception.CommerceDomainException;
import com.backend.domain.commerce.repository.CommerceCustomerConsentRepository;
import com.backend.domain.commerce.repository.CommerceCustomerRefreshTokenRepository;
import com.backend.domain.commerce.repository.CommerceCustomerRepository;
import com.backend.domain.exception.DuplicateEntityException;
import com.backend.domain.port.TenantContextPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
class CommerceCustomerAuthServiceImpl implements CommerceCustomerAuthService {

	private static final String CUSTOMER_ROLE = "COMMERCE_CUSTOMER";
	private static final String DEFAULT_SOURCE = "storefront";

	private final CommerceModuleAccessGuard commerceModuleAccessGuard;
	private final CommerceCustomerRepository customerRepository;
	private final CommerceCustomerConsentRepository consentRepository;
	private final CommerceCustomerRefreshTokenRepository refreshTokenRepository;
	private final CommerceCustomerTokenPort tokenPort;
	private final CommerceCustomerTokenHashService tokenHashService;
	private final CommerceCustomerRateLimitService rateLimitService;
	private final CustomerCartBridgeService customerCartBridgeService;
	private final TenantContextPort tenantContext;
	private final PasswordEncoder passwordEncoder;

	@Override
	@Transactional
	public CommerceCustomerAuthResult register(RegisterCommerceCustomerCommand command) {
		commerceModuleAccessGuard.assertEnabledForCurrentTenant();
		rateLimitService.checkRegisterOrThrow(command.email(), command.ipAddress());
		if (!command.termsAccepted() || !command.privacyAccepted()) {
			throw new IllegalArgumentException("commerce.customer.consent.required");
		}
		String emailNormalized = normalizeEmail(command.email());
		if (customerRepository.existsByEmailNormalized(emailNormalized)) {
			throw new DuplicateEntityException("commerce.customer.email.duplicate");
		}
		CommerceCustomer customer = new CommerceCustomer();
		customer.setEmail(command.email().trim());
		customer.setEmailNormalized(emailNormalized);
		customer.setPasswordHash(passwordEncoder.encode(command.password()));
		customer.setFirstName(command.firstName().trim());
		customer.setLastName(command.lastName().trim());
		customer.setPhone(command.phone().trim());
		customer.setGender(parseGender(command.gender()));
		customer.setBirthDate(command.birthDate());
		CommerceCustomer saved = customerRepository.save(customer);
		consentRepository.saveAll(createRegistrationConsents(saved, command));
		return issueAuthResult(saved, Boolean.TRUE.equals(command.rememberMe()), command.deviceFingerprint(), command.ipAddress(), command.userAgent(), command.cartToken());
	}

	@Override
	@Transactional
	public CommerceCustomerAuthResult login(LoginCommerceCustomerCommand command) {
		commerceModuleAccessGuard.assertEnabledForCurrentTenant();
		rateLimitService.checkLoginOrThrow(command.email(), command.ipAddress());
		CommerceCustomer customer = customerRepository.findByEmailNormalized(normalizeEmail(command.email()))
				.orElseThrow(() -> new BadCredentialsException("commerce.customer.auth.invalid.credentials"));
		if (!customer.canLogin() || customer.getPasswordHash() == null || !passwordEncoder.matches(command.password(), customer.getPasswordHash())) {
			throw new BadCredentialsException("commerce.customer.auth.invalid.credentials");
		}
		customer.recordLogin(command.ipAddress());
		CommerceCustomer saved = customerRepository.save(customer);
		return issueAuthResult(saved, Boolean.TRUE.equals(command.rememberMe()), command.deviceFingerprint(), command.ipAddress(), command.userAgent(), command.cartToken());
	}

	@Override
	@Transactional
	public CommerceCustomerAuthResult refresh(String refreshToken, String deviceFingerprint, String ipAddress, String userAgent) {
		commerceModuleAccessGuard.assertEnabledForCurrentTenant();
		if (refreshToken == null || refreshToken.isBlank() || !tokenPort.validateRefreshToken(refreshToken)) {
			throw new BadCredentialsException("commerce.customer.auth.refresh.invalid");
		}
		Long tokenTenantId = tokenPort.getTenantId(refreshToken);
		if (!currentTenantId().equals(tokenTenantId)) {
			throw new BadCredentialsException("commerce.customer.auth.refresh.invalid");
		}
		String tokenHash = tokenHashService.hashToken(refreshToken);
		CommerceCustomerRefreshToken stored = refreshTokenRepository.findByTokenHash(tokenHash)
				.orElseThrow(() -> new BadCredentialsException("commerce.customer.auth.refresh.invalid"));
		if (!stored.isValid()) {
			throw new BadCredentialsException("commerce.customer.auth.refresh.invalid");
		}
		CommerceCustomer customer = stored.getCustomer();
		if (!customer.canLogin()) {
			throw new CommerceDomainException("commerce.customer.auth.account.disabled");
		}
		int revoked = refreshTokenRepository.revokeByTokenHash(tokenHash);
		if (revoked != 1) {
			throw new BadCredentialsException("commerce.customer.auth.refresh.invalid");
		}
		return issueAuthResult(customer, tokenPort.isRememberMeToken(refreshToken), deviceFingerprint, ipAddress, userAgent, null);
	}

	@Override
	@Transactional
	public void logout(String refreshToken) {
		commerceModuleAccessGuard.assertEnabledForCurrentTenant();
		if (refreshToken == null || refreshToken.isBlank()) {
			return;
		}
		refreshTokenRepository.revokeByTokenHash(tokenHashService.hashToken(refreshToken));
	}

	private CommerceCustomerAuthResult issueAuthResult(
			CommerceCustomer customer,
			boolean rememberMe,
			String deviceFingerprint,
			String ipAddress,
			String userAgent,
			String sourceCartToken) {
		Long tenantId = currentTenantId();
		String accessToken = tokenPort.createAccessToken(customer, tenantId);
		String refreshToken = tokenPort.createRefreshToken(customer, tenantId, rememberMe);
		CommerceCustomerRefreshToken stored = new CommerceCustomerRefreshToken();
		stored.setCustomer(customer);
		stored.setTokenHash(tokenHashService.hashToken(refreshToken));
		stored.setExpiresAt(LocalDateTime.now().plusSeconds(tokenPort.getRefreshTokenExpiration(rememberMe) / 1000));
		stored.setRememberMe(rememberMe);
		stored.setDeviceFingerprint(truncate(deviceFingerprint, 255));
		stored.setIpAddress(truncate(ipAddress, 45));
		stored.setUserAgent(truncate(userAgent, 512));
		refreshTokenRepository.save(stored);
		CustomerCartBridgeService.CustomerCartBridgeResult bridgeResult =
				customerCartBridgeService.mergeOnAuth(customer, sourceCartToken);
		CommerceCustomerAuthResponse response = new CommerceCustomerAuthResponse(
				accessToken,
				tokenPort.getAccessTokenExpiration() / 1000,
				CommerceCustomerResponse.from(customer),
				bridgeResult.cart(),
				bridgeResult.merge());
		return new CommerceCustomerAuthResult(response, refreshToken, rememberMe);
	}

	private List<CommerceCustomerConsent> createRegistrationConsents(CommerceCustomer customer, RegisterCommerceCustomerCommand command) {
		LocalDateTime acceptedAt = LocalDateTime.now();
		List<CommerceCustomerConsent> consents = new ArrayList<>();
		consents.add(consent(customer, CommerceCustomerConsentType.LEGAL_TERMS, true, acceptedAt, command));
		consents.add(consent(customer, CommerceCustomerConsentType.PRIVACY_NOTICE, true, acceptedAt, command));
		consents.add(consent(customer, CommerceCustomerConsentType.MARKETING_EMAIL, Boolean.TRUE.equals(command.marketingEmailAccepted()), acceptedAt, command));
		consents.add(consent(customer, CommerceCustomerConsentType.MARKETING_SMS, Boolean.TRUE.equals(command.marketingSmsAccepted()), acceptedAt, command));
		consents.add(consent(customer, CommerceCustomerConsentType.MARKETING_PHONE, Boolean.TRUE.equals(command.marketingPhoneAccepted()), acceptedAt, command));
		return consents;
	}

	private CommerceCustomerConsent consent(
			CommerceCustomer customer,
			CommerceCustomerConsentType type,
			boolean accepted,
			LocalDateTime acceptedAt,
			RegisterCommerceCustomerCommand command) {
		CommerceCustomerConsent consent = new CommerceCustomerConsent();
		consent.setCustomer(customer);
		consent.setConsentType(type);
		consent.setAccepted(accepted);
		consent.setAcceptedAt(acceptedAt);
		consent.setSource(command.source() == null || command.source().isBlank() ? DEFAULT_SOURCE : truncate(command.source(), 50));
		consent.setIpAddress(truncate(command.ipAddress(), 45));
		consent.setUserAgent(truncate(command.userAgent(), 512));
		return consent;
	}

	private CommerceCustomerGender parseGender(String gender) {
		if (gender == null || gender.isBlank()) {
			return null;
		}
		try {
			return CommerceCustomerGender.valueOf(gender.trim().toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException ex) {
			throw new IllegalArgumentException("commerce.customer.gender.invalid", ex);
		}
	}

	private String normalizeEmail(String email) {
		if (email == null || email.isBlank()) {
			throw new BadCredentialsException("commerce.customer.auth.invalid.credentials");
		}
		return email.trim().toLowerCase(Locale.ROOT);
	}

	private Long currentTenantId() {
		try {
			return Long.parseLong(tenantContext.getTenantId());
		} catch (NumberFormatException ex) {
			throw new IllegalStateException("commerce.tenant.context.required", ex);
		}
	}

	private String truncate(String value, int maxLength) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.length() <= maxLength ? trimmed : trimmed.substring(0, maxLength);
	}
}
