package com.backend.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

	@Mock private JwtTokenProvider jwtTokenProvider;

	@Test
	void shouldNotFilter_ShouldSkipOnlyExactCartEndpointOrSubpath() throws Exception {
		JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtTokenProvider);

		assertThat(filter.shouldNotFilter(new MockHttpServletRequest("GET", "/commerce/cart"))).isTrue();
		assertThat(filter.shouldNotFilter(new MockHttpServletRequest("GET", "/commerce/cart/items"))).isTrue();
		assertThat(filter.shouldNotFilter(new MockHttpServletRequest("GET", "/api/commerce/cart"))).isTrue();
		assertThat(filter.shouldNotFilter(new MockHttpServletRequest("GET", "/api/commerce/cart/items"))).isTrue();
		assertThat(filter.shouldNotFilter(new MockHttpServletRequest("GET", "/commerce/cart-summary"))).isFalse();
		assertThat(filter.shouldNotFilter(new MockHttpServletRequest("GET", "/api/commerce/cart-summary"))).isFalse();
	}
}
