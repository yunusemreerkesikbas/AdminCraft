package com.backend.presentation.commerce;

import java.time.Duration;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.application.commerce.CommerceCustomerAuthService;
import com.backend.application.commerce.CommerceCustomerAuthService.CommerceCustomerAuthResult;
import com.backend.application.commerce.dto.LoginCommerceCustomerCommand;
import com.backend.application.commerce.dto.RegisterCommerceCustomerCommand;
import com.backend.infrastructure.security.JwtProperties;
import com.backend.shared.common.ApiResponse;
import com.backend.shared.common.RequestUtils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/commerce/customers/auth")
@RequiredArgsConstructor
public class CommerceCustomerAuthController {

	public static final String REFRESH_COOKIE_NAME = "commerce_customer_refresh_token";
	private static final String REFRESH_COOKIE_PATH = "/api/commerce/customers/auth";

	private final CommerceCustomerAuthService authService;
	private final MessageSource messageSource;
	private final JwtProperties jwtProperties;

	@PostMapping("/register")
	public ResponseEntity<ApiResponse<?>> register(
			@RequestHeader(value = CommerceCartController.CART_TOKEN_HEADER, required = false) String cartToken,
			@Valid @RequestBody RegisterCommerceCustomerRequest request,
			HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) {
		CommerceCustomerAuthResult result = authService.register(new RegisterCommerceCustomerCommand(
				request.email(),
				request.password(),
				request.firstName(),
				request.lastName(),
				request.phone(),
				request.gender(),
				request.birthDate(),
				request.termsAccepted(),
				request.privacyAccepted(),
				request.marketingEmailAccepted(),
				request.marketingSmsAccepted(),
				request.marketingPhoneAccepted(),
				request.rememberMe(),
				request.deviceFingerprint(),
				request.source(),
				cartToken,
				RequestUtils.getClientIpAddress(httpRequest),
				RequestUtils.getUserAgent(httpRequest)));
		setRefreshCookie(httpResponse, result);
		setCartTokenHeader(httpResponse, result);
		return ResponseEntity.ok(ApiResponse.success(message("commerce.customer.auth.registered"), result.response()));
	}

	@PostMapping("/login")
	public ResponseEntity<ApiResponse<?>> login(
			@RequestHeader(value = CommerceCartController.CART_TOKEN_HEADER, required = false) String cartToken,
			@Valid @RequestBody LoginCommerceCustomerRequest request,
			HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) {
		CommerceCustomerAuthResult result = authService.login(new LoginCommerceCustomerCommand(
				request.email(),
				request.password(),
				request.rememberMe(),
				request.deviceFingerprint(),
				cartToken,
				RequestUtils.getClientIpAddress(httpRequest),
				RequestUtils.getUserAgent(httpRequest)));
		setRefreshCookie(httpResponse, result);
		setCartTokenHeader(httpResponse, result);
		return ResponseEntity.ok(ApiResponse.success(message("commerce.customer.auth.logged.in"), result.response()));
	}

	@PostMapping("/refresh")
	public ResponseEntity<ApiResponse<?>> refresh(
			HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) {
		CommerceCustomerAuthResult result = authService.refresh(
				getRefreshCookie(httpRequest),
				httpRequest.getHeader("X-Device-Fingerprint"),
				RequestUtils.getClientIpAddress(httpRequest),
				RequestUtils.getUserAgent(httpRequest));
		setRefreshCookie(httpResponse, result);
		return ResponseEntity.ok(ApiResponse.success(message("commerce.customer.auth.refreshed"), result.response()));
	}

	@PostMapping("/logout")
	public ResponseEntity<ApiResponse<Void>> logout(
			HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) {
		authService.logout(getRefreshCookie(httpRequest));
		clearRefreshCookie(httpResponse);
		return ResponseEntity.ok(ApiResponse.success(message("commerce.customer.auth.logged.out"), null));
	}

	private void setRefreshCookie(HttpServletResponse response, CommerceCustomerAuthResult result) {
		long maxAgeSeconds = result.rememberMe()
				? jwtProperties.getRememberMeExpiration() / 1000
				: jwtProperties.getRefreshExpiration() / 1000;
		ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE_NAME, result.refreshToken())
				.httpOnly(true)
				.secure(jwtProperties.getCookie().isSecure())
				.sameSite(jwtProperties.getCookie().getSameSite())
				.path(REFRESH_COOKIE_PATH)
				.maxAge(Duration.ofSeconds(maxAgeSeconds))
				.build();
		response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
	}

	private void clearRefreshCookie(HttpServletResponse response) {
		ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE_NAME, "")
				.httpOnly(true)
				.secure(jwtProperties.getCookie().isSecure())
				.sameSite(jwtProperties.getCookie().getSameSite())
				.path(REFRESH_COOKIE_PATH)
				.maxAge(Duration.ZERO)
				.build();
		response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
	}

	private void setCartTokenHeader(HttpServletResponse response, CommerceCustomerAuthResult result) {
		if (result.response().cart() != null && result.response().cart().cartToken() != null) {
			response.addHeader(CommerceCartController.CART_TOKEN_HEADER, result.response().cart().cartToken());
		}
	}

	private String getRefreshCookie(HttpServletRequest request) {
		if (request.getCookies() == null) {
			return null;
		}
		for (Cookie cookie : request.getCookies()) {
			if (REFRESH_COOKIE_NAME.equals(cookie.getName())) {
				return cookie.getValue();
			}
		}
		return null;
	}

	private String message(String key) {
		Locale locale = LocaleContextHolder.getLocale();
		return messageSource.getMessage(key, null, key, locale);
	}
}
