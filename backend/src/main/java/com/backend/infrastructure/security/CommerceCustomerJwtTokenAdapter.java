package com.backend.infrastructure.security;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.backend.application.commerce.CommerceCustomerTokenPort;
import com.backend.domain.commerce.CommerceCustomer;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CommerceCustomerJwtTokenAdapter implements CommerceCustomerTokenPort {

	private static final String CUSTOMER_ROLE = "COMMERCE_CUSTOMER";
	private static final String ACCESS_TYPE = "commerce_customer_access";
	private static final String REFRESH_TYPE = "commerce_customer_refresh";

	private final JwtTokenProvider jwtTokenProvider;

	@Override
	public String createAccessToken(CommerceCustomer customer, Long tenantId) {
		return jwtTokenProvider.createToken(
				customer.getEmail(),
				CUSTOMER_ROLE,
				customer.getId(),
				tenantId,
				ACCESS_TYPE,
				jwtTokenProvider.getAccessTokenExpiration(),
				false);
	}

	@Override
	public String createRefreshToken(CommerceCustomer customer, Long tenantId, boolean rememberMe) {
		return jwtTokenProvider.createToken(
				customer.getEmail(),
				CUSTOMER_ROLE,
				customer.getId(),
				tenantId,
				REFRESH_TYPE,
				jwtTokenProvider.getRefreshTokenExpiration(rememberMe),
				rememberMe);
	}

	@Override
	public boolean validateAccessToken(String token) {
		return hasTypeAndRequiredClaims(token, ACCESS_TYPE);
	}

	@Override
	public boolean validateRefreshToken(String token) {
		return hasTypeAndRequiredClaims(token, REFRESH_TYPE);
	}

	@Override
	public boolean isRememberMeToken(String token) {
		return jwtTokenProvider.isRememberMeToken(token);
	}

	@Override
	public Long getCustomerId(String token) {
		return jwtTokenProvider.getUserIdFromToken(token);
	}

	@Override
	public Long getTenantId(String token) {
		return jwtTokenProvider.getTenantIdFromToken(token);
	}

	@Override
	public String getEmail(String token) {
		return jwtTokenProvider.getEmailFromToken(token);
	}

	@Override
	public long getAccessTokenExpiration() {
		return jwtTokenProvider.getAccessTokenExpiration();
	}

	@Override
	public long getRefreshTokenExpiration(boolean rememberMe) {
		return jwtTokenProvider.getRefreshTokenExpiration(rememberMe);
	}

	private boolean hasTypeAndRequiredClaims(String token, String expectedType) {
		try {
			return jwtTokenProvider.validateToken(token)
					&& expectedType.equals(jwtTokenProvider.getTokenType(token))
					&& jwtTokenProvider.getUserIdFromToken(token) != null
					&& jwtTokenProvider.getTenantIdFromToken(token) != null
					&& StringUtils.hasText(jwtTokenProvider.getEmailFromToken(token));
		} catch (Exception ex) {
			return false;
		}
	}
}
