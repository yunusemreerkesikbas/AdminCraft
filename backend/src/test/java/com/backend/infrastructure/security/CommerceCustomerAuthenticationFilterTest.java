package com.backend.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import com.backend.application.commerce.CommerceCustomerPrincipal;
import com.backend.application.commerce.CommerceCustomerTokenPort;
import com.backend.domain.port.TenantContextPort;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class CommerceCustomerAuthenticationFilterTest {

	@Mock private CommerceCustomerTokenPort tokenPort;
	@Mock private TenantContextPort tenantContext;
	@Mock private FilterChain filterChain;
	private final ObjectMapper objectMapper = new ObjectMapper();

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
		LocaleContextHolder.resetLocaleContext();
	}

	@Test
	void doFilterInternal_ShouldRejectToken_WhenTokenTenantDoesNotMatchCurrentTenant() throws Exception {
		CommerceCustomerAuthenticationFilter filter = filter();
		MockHttpServletRequest request = requestWithToken("customer-token");
		MockHttpServletResponse response = new MockHttpServletResponse();
		when(tokenPort.validateAccessToken("customer-token")).thenReturn(true);
		when(tokenPort.getCustomerId("customer-token")).thenReturn(10L);
		when(tokenPort.getTenantId("customer-token")).thenReturn(2L);
		when(tenantContext.getTenantId()).thenReturn("1");

		filter.doFilterInternal(request, response, filterChain);

		assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FORBIDDEN);
		JsonNode body = objectMapper.readTree(response.getContentAsString());
		assertThat(body.get("result").asText()).isEqualTo("ERROR");
		assertThat(body.get("message").asText()).isEqualTo("Access denied. Resource belongs to different tenant.");
		assertThat(body.get("code").asInt()).isEqualTo(HttpServletResponse.SC_FORBIDDEN);
		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
		verify(filterChain, never()).doFilter(request, response);
	}

	@Test
	void doFilterInternal_ShouldAuthenticateCustomer_WhenTokenTenantMatchesCurrentTenant() throws Exception {
		CommerceCustomerAuthenticationFilter filter = filter();
		MockHttpServletRequest request = requestWithToken("customer-token");
		MockHttpServletResponse response = new MockHttpServletResponse();
		when(tokenPort.validateAccessToken("customer-token")).thenReturn(true);
		when(tokenPort.getCustomerId("customer-token")).thenReturn(10L);
		when(tokenPort.getTenantId("customer-token")).thenReturn(1L);
		when(tokenPort.getEmail("customer-token")).thenReturn("user@example.com");
		when(tenantContext.getTenantId()).thenReturn("1");

		filter.doFilterInternal(request, response, filterChain);

		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
		assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
				.isEqualTo(new CommerceCustomerPrincipal(10L, null, "user@example.com", 1L));
		verify(filterChain).doFilter(request, response);
	}

	@Test
	void shouldNotFilter_ShouldAllowCustomerAuthenticationOnCartEndpoints() throws Exception {
		CommerceCustomerAuthenticationFilter filter = filter();
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/commerce/cart");
		MockHttpServletRequest apiRequest = new MockHttpServletRequest("GET", "/api/commerce/cart");

		assertThat(filter.shouldNotFilter(request)).isFalse();
		assertThat(filter.shouldNotFilter(apiRequest)).isFalse();
	}

	@Test
	void shouldNotFilter_ShouldAllowCustomerAuthenticationOnCheckoutEndpoints() throws Exception {
		CommerceCustomerAuthenticationFilter filter = filter();
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/commerce/checkout");

		assertThat(filter.shouldNotFilter(request)).isFalse();
	}

	@Test
	void shouldNotFilter_ShouldAllowCustomerAuthenticationOnPaymentEndpoints() throws Exception {
		CommerceCustomerAuthenticationFilter filter = filter();
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/commerce/payments/attempts");
		MockHttpServletRequest apiRequest = new MockHttpServletRequest("GET", "/api/commerce/payments/attempts/attempt-uid");

		assertThat(filter.shouldNotFilter(request)).isFalse();
		assertThat(filter.shouldNotFilter(apiRequest)).isFalse();
	}

	@Test
	void shouldNotFilter_ShouldAllowCustomerAuthenticationOnOrderEndpoints() throws Exception {
		CommerceCustomerAuthenticationFilter filter = filter();
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/commerce/orders");
		MockHttpServletRequest detailRequest = new MockHttpServletRequest("GET", "/commerce/orders/order-uid");
		MockHttpServletRequest apiRequest = new MockHttpServletRequest("GET", "/api/commerce/orders/order-uid");

		assertThat(filter.shouldNotFilter(request)).isFalse();
		assertThat(filter.shouldNotFilter(detailRequest)).isFalse();
		assertThat(filter.shouldNotFilter(apiRequest)).isFalse();
	}

	private MockHttpServletRequest requestWithToken(String token) {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/commerce/customers/me");
		request.addHeader("Authorization", "Bearer " + token);
		return request;
	}

	private CommerceCustomerAuthenticationFilter filter() {
		LocaleContextHolder.setLocale(java.util.Locale.ENGLISH);
		StaticMessageSource messageSource = new StaticMessageSource();
		messageSource.addMessage(
				"common.tenant.mismatch",
				java.util.Locale.ENGLISH,
				"Access denied. Resource belongs to different tenant.");
		return new CommerceCustomerAuthenticationFilter(tokenPort, tenantContext, messageSource, objectMapper);
	}
}
