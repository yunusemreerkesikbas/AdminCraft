package com.backend.presentation.commerce;

import java.net.URI;
import java.util.List;
import java.util.Locale;

import org.springframework.security.web.util.matcher.IpAddressMatcher;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.backend.application.commerce.CommerceCustomerPrincipal;
import com.backend.application.commerce.CommercePaymentProperties;
import com.backend.application.commerce.PaymentAttemptService;
import com.backend.application.commerce.dto.CreatePaymentAttemptCommand;
import com.backend.application.commerce.dto.InitializePaymentAttemptCommand;
import com.backend.application.commerce.dto.PaymentAttemptResponse;
import com.backend.application.commerce.dto.PaymentInitializeResponse;
import com.backend.domain.port.TenantContextPort;
import com.backend.infrastructure.config.AppSecurityProperties;
import com.backend.shared.common.ApiResponse;
import com.backend.shared.validation.Uid;

import com.google.common.net.InetAddresses;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/commerce/payments")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Commerce Payments", description = "Customer payment attempt foundation API")
public class CommercePaymentController {

	private static final String IYZICO_CALLBACK_PATH = "/commerce/payments/iyzico/checkout-form/callback";

	private final PaymentAttemptService paymentAttemptService;
	private final MessageSource messageSource;
	private final TenantContextPort tenantContext;
	private final AppSecurityProperties appSecurityProperties;
	private final CommercePaymentProperties paymentProperties;

	@PostMapping("/attempts")
	@Operation(summary = "Create payment attempt")
	public ResponseEntity<ApiResponse<PaymentAttemptResponse>> createAttempt(
			Authentication authentication,
			@Valid @RequestBody CreatePaymentAttemptRequest request) {
		PaymentAttemptResponse response = paymentAttemptService.create(
				principal(authentication),
				new CreatePaymentAttemptCommand(request.checkoutUid()));
		return ResponseEntity.ok(ApiResponse.success(message("commerce.payment.attempt.created"), response));
	}

	@GetMapping("/attempts/{attemptUid}")
	@Operation(summary = "Get payment attempt")
	public ResponseEntity<ApiResponse<PaymentAttemptResponse>> getAttempt(
			Authentication authentication,
			@PathVariable @Uid String attemptUid) {
		PaymentAttemptResponse response = paymentAttemptService.get(principal(authentication), attemptUid);
		return ResponseEntity.ok(ApiResponse.success(message("commerce.payment.attempt.retrieved"), response));
	}

	@PostMapping("/attempts/{attemptUid}/initialize")
	@Operation(summary = "Initialize iyzico CheckoutForm payment")
	public ResponseEntity<ApiResponse<PaymentInitializeResponse>> initializeAttempt(
			Authentication authentication,
			@PathVariable @Uid String attemptUid,
			HttpServletRequest request) {
		PaymentInitializeResponse response = paymentAttemptService.initialize(
				principal(authentication),
				new InitializePaymentAttemptCommand(
						attemptUid,
						callbackUrl(),
						clientIp(request)));
		return ResponseEntity.ok(ApiResponse.success(message("commerce.payment.attempt.initialized"), response));
	}

	@PostMapping("/iyzico/checkout-form/callback")
	@Operation(summary = "Handle iyzico CheckoutForm callback")
	public ResponseEntity<Void> iyzicoCheckoutFormCallback(@RequestParam(required = true) @NotBlank String token) {
		String redirectUrl = paymentAttemptService.handleIyzicoCheckoutFormCallback(token);
		return ResponseEntity.status(HttpStatus.FOUND)
				.location(URI.create(redirectUrl))
				.build();
	}

	private CommerceCustomerPrincipal principal(Authentication authentication) {
		if (authentication == null || !(authentication.getPrincipal() instanceof CommerceCustomerPrincipal principal)) {
			throw new IllegalArgumentException("commerce.customer.auth.required");
		}
		return principal;
	}

	private String callbackUrl() {
		String baseUrl = resolveCallbackBaseUrl();
		return UriComponentsBuilder.fromUriString(trimTrailingSlash(baseUrl))
				.path(IYZICO_CALLBACK_PATH)
				.toUriString();
	}

	private String clientIp(HttpServletRequest request) {
		String remoteAddr = request.getRemoteAddr();
		if (remoteAddr == null) {
			return "";
		}
		String normalizedRemoteAddr = remoteAddr.trim();
		if (remoteAddrMatchesTrustedProxy(normalizedRemoteAddr)) {
			String forwardedIp = firstForwardedIp(request.getHeader("X-Forwarded-For"));
			if (forwardedIp != null) {
				return forwardedIp;
			}
		}
		return normalizedRemoteAddr;
	}

	private String resolveCallbackBaseUrl() {
		String baseUrl = paymentProperties.getCallbackBaseUrl();
		if (baseUrl == null || baseUrl.isBlank()) {
			throw new IllegalStateException("commerce.payment.callback.url.required");
		}
		String resolved = baseUrl.trim();
		if (resolved.contains("%s")) {
			String subdomain = tenantContext.getSubdomain();
			if (subdomain == null || subdomain.isBlank()) {
				throw new IllegalStateException("commerce.tenant.context.required");
			}
			resolved = String.format(Locale.ROOT, resolved, subdomain.trim());
		}
		return resolved;
	}

	private String trimTrailingSlash(String value) {
		String trimmed = value.trim();
		while (trimmed.endsWith("/")) {
			trimmed = trimmed.substring(0, trimmed.length() - 1);
		}
		return trimmed;
	}

	private boolean remoteAddrMatchesTrustedProxy(String remoteAddr) {
		List<String> cidrs = appSecurityProperties.getTrustedProxyCidrs();
		if (cidrs == null || cidrs.isEmpty() || remoteAddr.isBlank()) {
			return false;
		}
		for (String cidr : cidrs) {
			if (cidr == null || cidr.isBlank()) {
				continue;
			}
			try {
				if (new IpAddressMatcher(cidr.trim()).matches(remoteAddr)) {
					return true;
				}
			} catch (IllegalArgumentException ex) {
				log.warn("Invalid trusted proxy CIDR skipped: {}", cidr);
			}
		}
		return false;
	}

	private static String firstForwardedIp(String headerValue) {
		if (headerValue == null || headerValue.isBlank()) {
			return null;
		}
		String first = headerValue.split(",")[0].trim();
		if (first.isEmpty()) {
			return null;
		}
		try {
			InetAddresses.forString(first);
			return first;
		} catch (IllegalArgumentException ex) {
			return null;
		}
	}

	private String message(String key) {
		Locale locale = LocaleContextHolder.getLocale();
		return messageSource.getMessage(key, null, key, locale);
	}
}
