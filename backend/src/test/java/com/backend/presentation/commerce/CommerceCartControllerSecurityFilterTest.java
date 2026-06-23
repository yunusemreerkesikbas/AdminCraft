package com.backend.presentation.commerce;

import static java.math.BigDecimal.ZERO;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import com.backend.application.commerce.CartService;
import com.backend.application.commerce.CommerceCartRateLimitService;
import com.backend.application.commerce.dto.CartResponse;
import com.backend.application.commerce.dto.CartTotalsResponse;
import com.backend.infrastructure.config.CorsProperties;
import com.backend.infrastructure.config.SecurityConfig;
import com.backend.infrastructure.security.CommerceCustomerAuthenticationFilter;
import com.backend.infrastructure.security.JwtAuthenticationFilter;
import com.backend.infrastructure.tenant.TenantFilter;
import com.backend.presentation.filter.CmsPreviewFilter;
import com.backend.shared.common.GlobalExceptionHandler;

import jakarta.servlet.FilterChain;

@WebMvcTest(CommerceCartController.class)
@Import({ SecurityConfig.class, GlobalExceptionHandler.class })
@EnableConfigurationProperties(CorsProperties.class)
class CommerceCartControllerSecurityFilterTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean private CartService cartService;
	@MockBean private CommerceCartRateLimitService rateLimitService;
	@MockBean private JwtAuthenticationFilter jwtAuthenticationFilter;
	@MockBean private TenantFilter tenantFilter;
	@MockBean private CommerceCustomerAuthenticationFilter commerceCustomerAuthenticationFilter;
	@MockBean private CmsPreviewFilter cmsPreviewFilter;

	@BeforeEach
	void setUp() throws Exception {
		doAnswer(passThroughFilter()).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
		doAnswer(passThroughFilter()).when(tenantFilter).doFilter(any(), any(), any());
		doAnswer(passThroughFilter()).when(commerceCustomerAuthenticationFilter).doFilter(any(), any(), any());
		doAnswer(passThroughFilter()).when(cmsPreviewFilter).doFilter(any(), any(), any());
	}

	@Test
	void createCart_ShouldPermitAnonymousBasePathThroughSecurityChain() throws Exception {
		when(cartService.createCart(null)).thenReturn(cartResponse());

		mockMvc.perform(post("/commerce/cart")
						.header("X-Tenant-Subdomain", "demo"))
				.andExpect(status().isOk())
				.andExpect(header().string(CommerceCartController.CART_TOKEN_HEADER, "cart-token"));

		verify(cartService).createCart(null);
	}

	@Test
	void clearCart_ShouldPermitAnonymousBasePathThroughSecurityChain() throws Exception {
		mockMvc.perform(delete("/commerce/cart")
						.header("X-Tenant-Subdomain", "demo")
						.header(CommerceCartController.CART_TOKEN_HEADER, "cart-token"))
				.andExpect(status().isOk());

		verify(cartService).clearCart("cart-token", null);
	}

	private Answer<Void> passThroughFilter() {
		return invocation -> {
			FilterChain chain = invocation.getArgument(2);
			chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
			return null;
		};
	}

	private CartResponse cartResponse() {
		return new CartResponse(
				"cart-token",
				"cart-uid",
				"ACTIVE",
				LocalDateTime.of(2026, 6, 23, 12, 0),
				List.of(),
				new CartTotalsResponse("TRY", 0, ZERO, ZERO, ZERO, ZERO, ZERO));
	}
}
