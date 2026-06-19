package com.backend.application.commerce;

import java.net.URI;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.backend.application.commerce.CommercePaymentProviderPort.Credentials;
import com.backend.application.service.config.ConfigPropertyService;
import com.backend.domain.entity.ConfigProperty;
import com.backend.domain.port.EncryptionServicePort;
import com.backend.domain.port.TenantContextPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
class DefaultCommercePaymentConfigResolver implements CommercePaymentConfigResolver {

	private static final String IYZICO_API_KEY = "commerce.payment.iyzico.api_key";
	private static final String IYZICO_SECRET_KEY = "commerce.payment.iyzico.secret_key";
	private static final String IYZICO_BASE_URL = "commerce.payment.iyzico.base_url";
	private static final String DEFAULT_IYZICO_BASE_URL = "https://sandbox-api.iyzipay.com";
	private static final String DEFAULT_PROVIDER = "iyzico";

	private final ConfigPropertyService configPropertyService;
	private final TenantContextPort tenantContext;
	private final EncryptionServicePort encryptionService;

	@Override
	public Credentials credentialsForProvider(String providerCode) {
		String provider = providerCode == null ? DEFAULT_PROVIDER : providerCode.trim().toLowerCase(Locale.ROOT);
		if (!DEFAULT_PROVIDER.equals(provider)) {
			throw new IllegalArgumentException("commerce.payment.provider.unsupported");
		}
		return new Credentials(
				decryptedRequiredSecret(IYZICO_API_KEY),
				decryptedRequiredSecret(IYZICO_SECRET_KEY),
				requiredUrl(configPropertyService.findRaw(currentTenantId(), tenantContext.getTenantDbName(), IYZICO_BASE_URL)
						.map(String::trim)
						.filter(value -> !value.isBlank())
						.orElse(DEFAULT_IYZICO_BASE_URL)));
	}

	private String decryptedRequiredSecret(String key) {
		ConfigProperty property = configPropertyService.find(currentTenantId(), tenantContext.getTenantDbName(), key)
				.orElseThrow(() -> new IllegalStateException("commerce.payment.config.required"));
		if (!Boolean.TRUE.equals(property.getSecret())) {
			throw new IllegalStateException("commerce.payment.config.secret.required");
		}
		String encrypted = property.getConfigValue();
		if (encrypted == null || encrypted.isBlank()) {
			throw new IllegalStateException("commerce.payment.config.required");
		}
		try {
			String decrypted = encryptionService.decrypt(encrypted);
			if (decrypted == null || decrypted.isBlank()) {
				throw new IllegalStateException("commerce.payment.config.required");
			}
			return decrypted.trim();
		} catch (RuntimeException ex) {
			throw new IllegalStateException("commerce.payment.config.invalid", ex);
		}
	}

	private String requiredUrl(String value) {
		try {
			URI uri = URI.create(value.trim());
			String scheme = uri.getScheme();
			if (scheme == null || (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) || uri.getHost() == null) {
				throw new IllegalStateException("commerce.payment.iyzico.base.url.invalid");
			}
			return uri.toString();
		} catch (IllegalArgumentException ex) {
			throw new IllegalStateException("commerce.payment.iyzico.base.url.invalid", ex);
		}
	}

	private Long currentTenantId() {
		try {
			return Long.parseLong(tenantContext.getTenantId());
		} catch (NumberFormatException ex) {
			throw new IllegalStateException("commerce.tenant.context.required", ex);
		}
	}
}
