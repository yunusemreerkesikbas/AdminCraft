package com.backend.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.backend.domain.commerce.CommerceCustomer;

class CommerceCustomerJwtTokenAdapterTest {

	private JwtTokenProvider jwtTokenProvider;
	private CommerceCustomerJwtTokenAdapter adapter;

	@BeforeEach
	void setUp() {
		JwtProperties properties = new JwtProperties();
		properties.setSecret("0123456789012345678901234567890123456789012345678901234567890123");
		jwtTokenProvider = new JwtTokenProvider(properties);
		adapter = new CommerceCustomerJwtTokenAdapter(jwtTokenProvider);
	}

	@Test
	void customerAccessToken_ShouldNotBeAcceptedAsAdminAccessToken() {
		CommerceCustomer customer = new CommerceCustomer();
		customer.setId(10L);
		customer.setEmail("user@example.com");

		String token = adapter.createAccessToken(customer, 1L);

		assertThat(adapter.validateAccessToken(token)).isTrue();
		assertThat(jwtTokenProvider.isAccessToken(token)).isFalse();
		assertThat(jwtTokenProvider.getTokenType(token)).isEqualTo("commerce_customer_access");
	}
}
