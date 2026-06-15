package com.backend.infrastructure.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.backend.application.commerce.CommerceCustomerPrincipal;
import com.backend.application.commerce.CommerceCustomerTokenPort;
import com.backend.domain.port.TenantContextPort;
import com.backend.shared.common.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommerceCustomerAuthenticationFilter extends OncePerRequestFilter {

	private static final String ROLE = "ROLE_COMMERCE_CUSTOMER";
	private static final String TENANT_MISMATCH_MESSAGE_KEY = "common.tenant.mismatch";

	private final CommerceCustomerTokenPort tokenPort;
	private final TenantContextPort tenantContext;
	private final MessageSource messageSource;
	private final ObjectMapper objectMapper;

	@Override
	protected void doFilterInternal(
			@NonNull HttpServletRequest request,
			@NonNull HttpServletResponse response,
			@NonNull FilterChain filterChain) throws ServletException, IOException {
		try {
			String token = getBearerToken(request);
			if (StringUtils.hasText(token) && tokenPort.validateAccessToken(token)) {
				Long customerId = tokenPort.getCustomerId(token);
				Long tenantId = tokenPort.getTenantId(token);
				if (!matchesCurrentTenant(tenantId)) {
					log.warn("Commerce customer token tenant mismatch");
					writeForbiddenResponse(response);
					return;
				}
				String email = tokenPort.getEmail(token);
				CommerceCustomerPrincipal principal = new CommerceCustomerPrincipal(customerId, null, email, tenantId);
				UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
						principal,
						null,
						List.of(new SimpleGrantedAuthority(ROLE)));
				Map<String, Object> details = new HashMap<>();
				details.put("tenantId", tenantId);
				details.put("customerId", customerId);
				details.put("email", email);
				details.put("role", "COMMERCE_CUSTOMER");
				authentication.setDetails(details);
				SecurityContextHolder.getContext().setAuthentication(authentication);
			}
		} catch (Exception ex) {
			log.warn("Commerce customer authentication failed: {}", ex.getMessage());
			SecurityContextHolder.clearContext();
		}
		filterChain.doFilter(request, response);
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String path = requestPath(request);
		boolean commerceCustomerAuthEndpoint = path.startsWith("/commerce/customers/auth/register")
				|| path.startsWith("/commerce/customers/auth/login")
				|| path.startsWith("/commerce/customers/auth/refresh");
		boolean commerceCustomerEndpoint = path.startsWith("/commerce/customers/");
		boolean commerceCartEndpoint = path.equals("/commerce/cart") || path.startsWith("/commerce/cart/");
		boolean commerceCheckoutEndpoint = path.equals("/commerce/checkout") || path.startsWith("/commerce/checkout/");
		boolean commercePaymentEndpoint = path.equals("/commerce/payments") || path.startsWith("/commerce/payments/");
		boolean commerceOrderEndpoint = path.equals("/commerce/orders") || path.startsWith("/commerce/orders/");
		return commerceCustomerAuthEndpoint
				|| (!commerceCustomerEndpoint
						&& !commerceCartEndpoint
						&& !commerceCheckoutEndpoint
						&& !commercePaymentEndpoint
						&& !commerceOrderEndpoint);
	}

	private String requestPath(HttpServletRequest request) {
		String path = request.getServletPath();
		if (!StringUtils.hasText(path)) {
			path = request.getRequestURI();
		}
		String contextPath = request.getContextPath();
		if (StringUtils.hasText(contextPath) && path.startsWith(contextPath)) {
			path = path.substring(contextPath.length());
		}
		if (path.startsWith("/api/")) {
			path = path.substring(4);
		}
		return path;
	}

	private String getBearerToken(HttpServletRequest request) {
		String bearerToken = request.getHeader("Authorization");
		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7);
		}
		return null;
	}

	private boolean matchesCurrentTenant(Long tokenTenantId) {
		if (tokenTenantId == null) {
			return false;
		}
		String currentTenantId = tenantContext.getTenantId();
		if (!StringUtils.hasText(currentTenantId)) {
			return false;
		}
		try {
			return tokenTenantId.equals(Long.parseLong(currentTenantId));
		} catch (NumberFormatException ex) {
			log.warn("Commerce customer tenant context is not numeric");
			return false;
		}
	}

	private void writeForbiddenResponse(HttpServletResponse response) throws IOException {
		String message = messageSource.getMessage(
				TENANT_MISMATCH_MESSAGE_KEY,
				null,
				TENANT_MISMATCH_MESSAGE_KEY,
				LocaleContextHolder.getLocale());
		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		objectMapper.writeValue(response.getWriter(), ApiResponse.error(HttpServletResponse.SC_FORBIDDEN, message));
	}
}
