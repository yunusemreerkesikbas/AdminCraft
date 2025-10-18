package com.backend.infrastructure.tenant;

import com.backend.infrastructure.persistence.platform.entity.Tenant;
import com.backend.infrastructure.persistence.platform.repository.TenantPlatformRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantFilterTest {

  @Mock
  private TenantContext tenantContext;

  @Mock
  private TenantPlatformRepository tenantRepository;

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private FilterChain filterChain;

  private TenantFilter tenantFilter;

  @BeforeEach
  void setUp() {
    tenantFilter = new TenantFilter(tenantContext, tenantRepository);
  }

  @Test
  void shouldBypassFilterForWhitelistedPaths() throws Exception {
    when(request.getRequestURI()).thenReturn("/api/provisioning/modules/catalog");

    tenantFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(tenantContext, never()).setTenantId(anyString());
    verify(tenantRepository, never()).findById(any());
  }

  @Test
  void shouldReturnBadRequestWhenTenantHeaderMissing() throws Exception {
    when(request.getRequestURI()).thenReturn("/api/pages");
    when(request.getHeader("X-Correlation-ID")).thenReturn(null);
    when(request.getHeader("X-Tenant-ID")).thenReturn(null);

    tenantFilter.doFilterInternal(request, response, filterChain);

    verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Tenant identifier required");
    verify(filterChain, never()).doFilter(request, response);
  }

  // SUPER_ADMIN tokens should bypass tenant header requirement
  // Note: We simulate SecurityContext here minimally by not asserting details;
  // focus is bypass behavior
  @Test
  void shouldBypassWhenSuperAdminAndNoTenantHeader() throws Exception {
    when(request.getRequestURI()).thenReturn("/api/pages");
    when(request.getHeader("X-Correlation-ID")).thenReturn(null);
    when(request.getHeader("X-Tenant-ID")).thenReturn(null);

    // Simulate JwtAuthenticationFilter having set ROLE_SUPER_ADMIN
    org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
        new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
            "admin@platform.local",
            null,
            java.util.List
                .of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_SUPER_ADMIN"))));

    tenantFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(response, never()).sendError(anyInt(), anyString());

    // cleanup
    org.springframework.security.core.context.SecurityContextHolder.clearContext();
  }

  @Test
  void shouldReturnBadRequestWhenTenantHeaderBlank() throws Exception {
    when(request.getRequestURI()).thenReturn("/api/pages");
    when(request.getHeader("X-Correlation-ID")).thenReturn(null);
    when(request.getHeader("X-Tenant-ID")).thenReturn("");

    tenantFilter.doFilterInternal(request, response, filterChain);

    verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Tenant identifier required");
    verify(filterChain, never()).doFilter(request, response);
  }

  @Test
  void shouldReturnBadRequestWhenTenantIdInvalid() throws Exception {
    when(request.getRequestURI()).thenReturn("/api/pages");
    when(request.getHeader("X-Correlation-ID")).thenReturn(null);
    when(request.getHeader("X-Tenant-ID")).thenReturn("invalid");

    tenantFilter.doFilterInternal(request, response, filterChain);

    verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid tenant identifier");
    verify(filterChain, never()).doFilter(request, response);
  }

  @Test
  void shouldReturnBadRequestWhenTenantNotFound() throws Exception {
    when(request.getRequestURI()).thenReturn("/api/pages");
    when(request.getHeader("X-Correlation-ID")).thenReturn(null);
    when(request.getHeader("X-Tenant-ID")).thenReturn("999");
    when(tenantRepository.findById(999L)).thenReturn(Optional.empty());

    tenantFilter.doFilterInternal(request, response, filterChain);

    verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Tenant not found");
    verify(filterChain, never()).doFilter(request, response);
  }

  @Test
  void shouldReturnForbiddenWhenTenantInactive() throws Exception {
    Tenant inactiveTenant = Tenant.builder()
        .id(1L)
        .databaseName("ac_tenant_1")
        .status("PENDING")
        .build();

    when(request.getRequestURI()).thenReturn("/api/pages");
    when(request.getHeader("X-Correlation-ID")).thenReturn(null);
    when(request.getHeader("X-Tenant-ID")).thenReturn("1");
    when(tenantRepository.findById(1L)).thenReturn(Optional.of(inactiveTenant));

    tenantFilter.doFilterInternal(request, response, filterChain);

    verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "Tenant not active");
    verify(filterChain, never()).doFilter(request, response);
  }

  @Test
  void shouldSetContextAndProceedWhenTenantActive() throws Exception {
    Tenant activeTenant = Tenant.builder()
        .id(1L)
        .databaseName("ac_tenant_1")
        .status("ACTIVE")
        .build();

    when(request.getRequestURI()).thenReturn("/api/pages");
    when(request.getHeader("X-Correlation-ID")).thenReturn(null);
    when(request.getHeader("X-Tenant-ID")).thenReturn("1");
    when(tenantRepository.findById(1L)).thenReturn(Optional.of(activeTenant));

    tenantFilter.doFilterInternal(request, response, filterChain);

    verify(tenantContext).setTenantId("1");
    verify(tenantContext).setTenantDbName("ac_tenant_1");
    verify(filterChain).doFilter(request, response);
    verify(tenantContext).clear();
  }

  @Test
  void shouldClearContextInFinallyBlock() throws Exception {
    Tenant activeTenant = Tenant.builder()
        .id(1L)
        .databaseName("ac_tenant_1")
        .status("ACTIVE")
        .build();

    when(request.getRequestURI()).thenReturn("/api/pages");
    when(request.getHeader("X-Correlation-ID")).thenReturn(null);
    when(request.getHeader("X-Tenant-ID")).thenReturn("1");
    when(tenantRepository.findById(1L)).thenReturn(Optional.of(activeTenant));
    doThrow(new RuntimeException("Test exception")).when(filterChain).doFilter(request, response);

    try {
      tenantFilter.doFilterInternal(request, response, filterChain);
    } catch (RuntimeException e) {
      // Expected
    }

    verify(tenantContext).clear();
  }

  @Test
  void shouldBypassFilterForActuatorPaths() throws Exception {
    when(request.getRequestURI()).thenReturn("/api/actuator/health");

    tenantFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(tenantContext, never()).setTenantId(anyString());
  }

  @Test
  void shouldBypassFilterForPlatformPaths() throws Exception {
    when(request.getRequestURI()).thenReturn("/api/platform/tenants");

    tenantFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(tenantContext, never()).setTenantId(anyString());
  }

  @Test
  void shouldBypassFilterForProvisioningPaths() throws Exception {
    when(request.getRequestURI()).thenReturn("/api/provisioning/tenants/1/provision");

    tenantFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(tenantContext, never()).setTenantId(anyString());
  }

  @Test
  void shouldBypassFilterForTenantsPathsWhenSuperAdmin() throws Exception {
    when(request.getRequestURI()).thenReturn("/api/tenants");

    // SUPER_ADMIN auth
    org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
        new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
            "admin@platform.local",
            null,
            java.util.List
                .of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_SUPER_ADMIN"))));

    tenantFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(tenantContext, never()).setTenantId(anyString());

    org.springframework.security.core.context.SecurityContextHolder.clearContext();
  }

  @Test
  void shouldNotBypassTenantsPathsForNonSuperAdmin() throws Exception {
    when(request.getRequestURI()).thenReturn("/api/tenants");
    when(request.getHeader("X-Correlation-ID")).thenReturn(null);
    when(request.getHeader("X-Tenant-ID")).thenReturn(null);

    // Non-super admin (no authorities)
    org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
        new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
            "user@tenant.local", null, java.util.List.of()));

    tenantFilter.doFilterInternal(request, response, filterChain);

    verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Tenant identifier required");
    verify(filterChain, never()).doFilter(request, response);

    org.springframework.security.core.context.SecurityContextHolder.clearContext();
  }

  @Test
  void shouldBypassFilterForTenantsModulesPathWhenSuperAdmin() throws Exception {
    when(request.getRequestURI()).thenReturn("/api/tenants/1/modules");

    // SUPER_ADMIN auth
    org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
        new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
            "admin@platform.local",
            null,
            java.util.List
                .of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_SUPER_ADMIN"))));

    tenantFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(tenantContext, never()).setTenantId(anyString());

    org.springframework.security.core.context.SecurityContextHolder.clearContext();
  }
}
