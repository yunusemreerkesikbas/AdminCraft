package com.backend.infrastructure.tenant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.backend.infrastructure.persistence.platform.repository.TenantPlatformRepository;
import com.backend.infrastructure.persistence.platform.entity.Tenant;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TenantFilterTest {

  @Mock
  private TenantContext tenantContext;

  @Mock
  private TenantPlatformRepository tenantRepository;

  @Mock
  private MultiTenantConnectionProvider connectionProvider;

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private FilterChain filterChain;

  @Mock
  private SecurityContext securityContext;

  private TenantFilter tenantFilter;

  @BeforeEach
  void setUp() {
    tenantFilter = new TenantFilter(tenantContext, tenantRepository, connectionProvider, false);
  }

  @Test
  void shouldBypassFilterForWhitelistedPaths() throws Exception {
    when(request.getRequestURI()).thenReturn("/api/provisioning/modules/catalog");
    mockSuperAdmin();

    tenantFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(tenantContext, never()).setTenantId(anyString());
    verify(tenantRepository, never()).findById(any());
    SecurityContextHolder.clearContext();
  }

  @Test
  void shouldReturnBadRequestWhenTenantHeaderMissing() throws Exception {
    when(request.getRequestURI()).thenReturn("/api/pages");
    when(request.getHeader("X-Correlation-ID")).thenReturn(null);
    when(request.getHeader("X-Tenant-ID")).thenReturn(null);
    mockWaitUser();

    tenantFilter.doFilterInternal(request, response, filterChain);

    verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Tenant identifier required");
    verify(filterChain, never()).doFilter(request, response);
    SecurityContextHolder.clearContext();
  }

  @Test
  void shouldBypassWhenSuperAdminAndNoTenantHeader() throws Exception {
    when(request.getRequestURI()).thenReturn("/api/platform/users");
    when(request.getHeader("X-Correlation-ID")).thenReturn(null);
    when(request.getHeader("X-Tenant-ID")).thenReturn(null);
    mockSuperAdmin();

    tenantFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(response, never()).sendError(anyInt(), anyString());
    SecurityContextHolder.clearContext();
  }

  @Test
  void shouldReturnBadRequestWhenTenantHeaderBlank() throws Exception {
    when(request.getRequestURI()).thenReturn("/api/pages");
    when(request.getHeader("X-Correlation-ID")).thenReturn(null);
    when(request.getHeader("X-Tenant-ID")).thenReturn("");
    mockWaitUser();

    tenantFilter.doFilterInternal(request, response, filterChain);

    verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Tenant identifier required");
    verify(filterChain, never()).doFilter(request, response);
    SecurityContextHolder.clearContext();
  }

  @Test
  void shouldReturnBadRequestWhenTenantIdInvalid() throws Exception {
    when(request.getRequestURI()).thenReturn("/api/pages");
    when(request.getHeader("X-Correlation-ID")).thenReturn(null);
    when(request.getHeader("X-Tenant-ID")).thenReturn("invalid");
    mockWaitUser();

    tenantFilter.doFilterInternal(request, response, filterChain);

    // Code swallows NumberFormatException and returns generic error
    verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Tenant identifier required");
    verify(filterChain, never()).doFilter(request, response);
    SecurityContextHolder.clearContext();
  }

  @Test
  void shouldReturnBadRequestWhenTenantNotFound() throws Exception {
    when(request.getRequestURI()).thenReturn("/api/pages");
    when(request.getHeader("X-Correlation-ID")).thenReturn(null);
    when(request.getHeader("X-Tenant-ID")).thenReturn("999");
    when(tenantRepository.findById(999L)).thenReturn(Optional.empty());
    mockWaitUser();

    tenantFilter.doFilterInternal(request, response, filterChain);

    // Code returns generic error if tenant not found from headers
    verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Tenant identifier required");
    verify(filterChain, never()).doFilter(request, response);
    SecurityContextHolder.clearContext();
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
    mockWaitUser();

    tenantFilter.doFilterInternal(request, response, filterChain);

    verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "Tenant not active");
    verify(filterChain, never()).doFilter(request, response);
    SecurityContextHolder.clearContext();
  }

  @Test
  void shouldSetContextAndProceedWhenTenantActive() throws Exception {
    Tenant activeTenant = Tenant.builder()
        .id(1L)
        .databaseName("ac_tenant_1")
        .status("ACTIVE")
        .defaultLanguage("TR")
        .build();

    when(request.getRequestURI()).thenReturn("/api/pages");
    when(request.getHeader("X-Correlation-ID")).thenReturn(null);
    when(request.getHeader("X-Tenant-ID")).thenReturn("1");
    when(tenantRepository.findById(1L)).thenReturn(Optional.of(activeTenant));
    doNothing().when(connectionProvider).warmUpConnectionPool("ac_tenant_1");
    mockWaitUser();

    tenantFilter.doFilterInternal(request, response, filterChain);

    verify(tenantContext).setTenantId("1");
    verify(tenantContext).setTenantDbName("ac_tenant_1");
    verify(connectionProvider).warmUpConnectionPool("ac_tenant_1");
    verify(filterChain).doFilter(request, response);
    verify(tenantContext).clear();
    SecurityContextHolder.clearContext();
  }

  @Test
  void shouldClearContextInFinallyBlock() throws Exception {
    Tenant activeTenant = Tenant.builder()
        .id(1L)
        .databaseName("ac_tenant_1")
        .status("ACTIVE")
        .defaultLanguage("TR")
        .build();

    when(request.getRequestURI()).thenReturn("/api/pages");
    when(request.getHeader("X-Correlation-ID")).thenReturn(null);
    when(request.getHeader("X-Tenant-ID")).thenReturn("1");
    when(tenantRepository.findById(1L)).thenReturn(Optional.of(activeTenant));
    doNothing().when(connectionProvider).warmUpConnectionPool("ac_tenant_1");
    doThrow(new RuntimeException("Test exception")).when(filterChain).doFilter(request, response);
    mockWaitUser();

    try {
      tenantFilter.doFilterInternal(request, response, filterChain);
    } catch (RuntimeException e) {
      // Expected
    }

    verify(tenantContext).clear();
    SecurityContextHolder.clearContext();
  }

  @Test
  void shouldReturnServiceUnavailableWhenConnectionPoolInitializationFails() throws Exception {
    Tenant activeTenant = Tenant.builder()
        .id(1L)
        .databaseName("ac_tenant_1")
        .status("ACTIVE")
        .defaultLanguage("TR")
        .build();

    when(request.getRequestURI()).thenReturn("/api/pages");
    when(request.getHeader("X-Correlation-ID")).thenReturn(null);
    when(request.getHeader("X-Tenant-ID")).thenReturn("1");
    when(tenantRepository.findById(1L)).thenReturn(Optional.of(activeTenant));
    doThrow(new RuntimeException("Connection pool initialization failed"))
        .when(connectionProvider).warmUpConnectionPool("ac_tenant_1");
    mockWaitUser();

    tenantFilter.doFilterInternal(request, response, filterChain);

    verify(tenantContext).setTenantId("1");
    verify(tenantContext).setTenantDbName("ac_tenant_1");
    verify(connectionProvider).warmUpConnectionPool("ac_tenant_1");
    verify(response).sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
        "Tenant database is initializing, please retry in a moment");
    verify(filterChain, never()).doFilter(request, response);
    verify(tenantContext).clear();
    SecurityContextHolder.clearContext();
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
    mockSuperAdmin();

    tenantFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(tenantContext, never()).setTenantId(anyString());
    SecurityContextHolder.clearContext();
  }

  @Test
  void shouldBypassFilterForProvisioningPaths() throws Exception {
    givenRequestUri("/api/provisioning/tenants/1/provision");
    mockSuperAdmin();

    tenantFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(tenantContext, never()).setTenantId(anyString());
    SecurityContextHolder.clearContext();
  }

  @Test
  void shouldBypassFilterForTenantsPathsWhenSuperAdmin() throws Exception {
    when(request.getRequestURI()).thenReturn("/api/tenants");
    mockSuperAdmin();

    tenantFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(tenantContext, never()).setTenantId(anyString());
    SecurityContextHolder.clearContext();
  }

  @Test
  void shouldNotBypassTenantsPathsForNonSuperAdmin() throws Exception {
    when(request.getRequestURI()).thenReturn("/api/tenants");
    when(request.getHeader("X-Correlation-ID")).thenReturn(null);
    when(request.getHeader("X-Tenant-ID")).thenReturn(null);
    mockWaitUser();

    tenantFilter.doFilterInternal(request, response, filterChain);

    verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
    verify(filterChain, never()).doFilter(request, response);
    SecurityContextHolder.clearContext();
  }

  @Test
  void shouldBypassFilterForTenantsModulesPathWhenSuperAdmin() throws Exception {
    when(request.getRequestURI()).thenReturn("/api/tenants/1/modules");
    mockSuperAdmin();

    tenantFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(tenantContext, never()).setTenantId(anyString());
    SecurityContextHolder.clearContext();
  }

  // SEC-003: cross-check JWT tenantId against the resolved tenant from headers.

  @Test
  void shouldReturnForbiddenWhenJwtTenantIdMismatchesHeaderTenant() throws Exception {
    Tenant tenantTwo = activeTenant(2L, "ac_tenant_2");
    when(request.getRequestURI()).thenReturn("/api/sites");
    when(request.getHeader("X-Correlation-ID")).thenReturn(null);
    when(request.getHeader("X-Tenant-ID")).thenReturn("2");
    when(tenantRepository.findById(2L)).thenReturn(Optional.of(tenantTwo));
    mockTenantBoundUser("ROLE_TENANT_ADMIN", 1L);

    tenantFilter.doFilterInternal(request, response, filterChain);

    verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "Tenant mismatch");
    verify(filterChain, never()).doFilter(request, response);
    verify(tenantContext, never()).setTenantId(anyString());
    verify(connectionProvider, never()).warmUpConnectionPool(anyString());
    SecurityContextHolder.clearContext();
  }

  @Test
  void shouldAllowSuperAdminToAccessAnyTenantHeader() throws Exception {
    Tenant tenantTwo = activeTenant(2L, "ac_tenant_2");
    when(request.getRequestURI()).thenReturn("/api/sites");
    when(request.getHeader("X-Correlation-ID")).thenReturn(null);
    when(request.getHeader("X-Tenant-ID")).thenReturn("2");
    when(tenantRepository.findById(2L)).thenReturn(Optional.of(tenantTwo));
    doNothing().when(connectionProvider).warmUpConnectionPool("ac_tenant_2");
    mockTenantBoundSuperAdmin(null);

    tenantFilter.doFilterInternal(request, response, filterChain);

    verify(response, never()).sendError(anyInt(), anyString());
    verify(tenantContext).setTenantId("2");
    SecurityContextHolder.clearContext();
  }

  @Test
  void shouldAllowAnonymousPublicTrafficWithoutCrossCheck() throws Exception {
    Tenant tenantTwo = activeTenant(2L, "ac_tenant_2");
    when(request.getRequestURI()).thenReturn("/api/cms/site");
    when(request.getHeader("X-Correlation-ID")).thenReturn(null);
    when(request.getHeader("X-Tenant-ID")).thenReturn("2");
    when(tenantRepository.findById(2L)).thenReturn(Optional.of(tenantTwo));
    doNothing().when(connectionProvider).warmUpConnectionPool("ac_tenant_2");
    // No SecurityContext authentication — simulates anonymous public storefront call.
    SecurityContextHolder.clearContext();

    tenantFilter.doFilterInternal(request, response, filterChain);

    verify(response, never()).sendError(anyInt(), anyString());
    verify(tenantContext).setTenantId("2");
  }

  // SEC-106: bypass paths (e.g. /api/config/admin) must enforce JWT/header tenant
  // alignment when both are present.

  @Test
  void shouldReturnForbiddenOnConfigAdminWhenJwtTenantMismatchesHeader() throws Exception {
    when(request.getRequestURI()).thenReturn("/api/config/admin/security/recaptcha");
    when(request.getHeader("X-Correlation-ID")).thenReturn(null);
    when(request.getHeader("X-Tenant-ID")).thenReturn("99");
    mockTenantBoundUser("ROLE_CONFIG_TENANT_ADMIN", 1L);

    tenantFilter.doFilterInternal(request, response, filterChain);

    verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "Tenant mismatch");
    verify(filterChain, never()).doFilter(request, response);
    SecurityContextHolder.clearContext();
  }

  @Test
  void shouldAllowConfigAdminWhenJwtTenantMatchesHeader() throws Exception {
    when(request.getRequestURI()).thenReturn("/api/config/admin/security/recaptcha");
    when(request.getHeader("X-Correlation-ID")).thenReturn(null);
    when(request.getHeader("X-Tenant-ID")).thenReturn("1");
    mockTenantBoundUser("ROLE_CONFIG_TENANT_ADMIN", 1L);

    tenantFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(response, never()).sendError(anyInt(), anyString());
    SecurityContextHolder.clearContext();
  }

  // SEC-006: hostname fallback must ignore Origin/Referer (attacker-controlled).

  @Test
  void shouldIgnoreOriginHeaderWhenResolvingTenantFromHostname() throws Exception {
    when(request.getRequestURI()).thenReturn("/api/cms/site");
    when(request.getHeader("X-Correlation-ID")).thenReturn(null);
    when(request.getHeader("X-Tenant-ID")).thenReturn(null);
    when(request.getHeader("X-Tenant-Subdomain")).thenReturn(null);
    when(request.getHeader("X-Forwarded-Host")).thenReturn(null);
    when(request.getHeader("Origin")).thenReturn("https://victim.craftive.io");
    when(request.getServerName()).thenReturn("localhost");
    SecurityContextHolder.clearContext();

    tenantFilter.doFilterInternal(request, response, filterChain);

    // Origin must NOT be used to look up a tenant.
    verify(tenantRepository, never()).findBySubdomain("victim");
    verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Tenant identifier required");
  }

  @Test
  void shouldAllowForgotPasswordWithoutResolvedTenant() throws Exception {
    when(request.getRequestURI()).thenReturn("/api/auth/forgot-password");
    when(request.getHeader("X-Correlation-ID")).thenReturn(null);
    when(request.getHeader("X-Tenant-ID")).thenReturn(null);
    when(request.getHeader("X-Tenant-Subdomain")).thenReturn(null);
    when(request.getHeader("X-Forwarded-Host")).thenReturn(null);
    when(request.getServerName()).thenReturn("localhost");
    SecurityContextHolder.clearContext();

    tenantFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(response, never()).sendError(anyInt(), anyString());
  }

  @Test
  void shouldIgnoreForwardedHostWhenTrustForwardedHostDisabled() throws Exception {
    when(request.getRequestURI()).thenReturn("/api/cms/site");
    when(request.getHeader("X-Correlation-ID")).thenReturn(null);
    when(request.getHeader("X-Tenant-ID")).thenReturn(null);
    when(request.getHeader("X-Tenant-Subdomain")).thenReturn(null);
    when(request.getHeader("X-Forwarded-Host")).thenReturn("victim.craftive.io");
    when(request.getServerName()).thenReturn("localhost");
    SecurityContextHolder.clearContext();

    tenantFilter.doFilterInternal(request, response, filterChain);

    verify(tenantRepository, never()).findBySubdomain("victim");
    verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Tenant identifier required");
  }

  @Test
  void shouldResolveForwardedHostWhenTrustForwardedHostEnabled() throws Exception {
    tenantFilter = new TenantFilter(tenantContext, tenantRepository, connectionProvider, true);
    Tenant activeTenant = activeTenant(1L, "ac_tenant_1");
    when(request.getRequestURI()).thenReturn("/api/cms/site");
    when(request.getHeader("X-Correlation-ID")).thenReturn(null);
    when(request.getHeader("X-Tenant-ID")).thenReturn(null);
    when(request.getHeader("X-Tenant-Subdomain")).thenReturn(null);
    when(request.getHeader("X-Forwarded-Host")).thenReturn("tenant.craftive.io");
    when(request.getServerName()).thenReturn("localhost");
    when(tenantRepository.findBySubdomain("tenant")).thenReturn(Optional.of(activeTenant));
    doNothing().when(connectionProvider).warmUpConnectionPool("ac_tenant_1");
    SecurityContextHolder.clearContext();

    tenantFilter.doFilterInternal(request, response, filterChain);

    verify(tenantContext).setTenantId("1");
    verify(filterChain).doFilter(request, response);
  }

  private Tenant activeTenant(long id, String dbName) {
    return Tenant.builder()
        .id(id)
        .databaseName(dbName)
        .status("ACTIVE")
        .defaultLanguage("TR")
        .build();
  }

  private void mockTenantBoundUser(String role, Long tenantId) {
    SecurityContextHolder.setContext(securityContext);
    org.springframework.security.core.Authentication authMock =
        mock(org.springframework.security.core.Authentication.class);
    when(securityContext.getAuthentication()).thenReturn(authMock);
    when(authMock.isAuthenticated()).thenReturn(true);
    doReturn(List.of(new SimpleGrantedAuthority(role))).when(authMock).getAuthorities();
    Map<String, Object> details = new HashMap<>();
    details.put("tenantId", tenantId);
    when(authMock.getDetails()).thenReturn(details);
  }

  private void mockTenantBoundSuperAdmin(Long tenantId) {
    SecurityContextHolder.setContext(securityContext);
    org.springframework.security.core.Authentication authMock =
        mock(org.springframework.security.core.Authentication.class);
    when(securityContext.getAuthentication()).thenReturn(authMock);
    when(authMock.isAuthenticated()).thenReturn(true);
    doReturn(List.of(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"))).when(authMock).getAuthorities();
    if (tenantId != null) {
      Map<String, Object> details = new HashMap<>();
      details.put("tenantId", tenantId);
      when(authMock.getDetails()).thenReturn(details);
    }
  }

  private void givenRequestUri(String uri) {
    when(request.getRequestURI()).thenReturn(uri);
  }

  private void mockSuperAdmin() {
    SecurityContextHolder.setContext(securityContext);
    org.springframework.security.core.Authentication authMock = mock(org.springframework.security.core.Authentication.class);
    when(securityContext.getAuthentication()).thenReturn(authMock);
    doReturn(List.of(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"))).when(authMock).getAuthorities();
  }

  private void mockWaitUser() {
    SecurityContextHolder.setContext(securityContext);
    org.springframework.security.core.Authentication authMock = mock(org.springframework.security.core.Authentication.class);
    when(securityContext.getAuthentication()).thenReturn(authMock);
    // User has no special authorities
    doReturn(List.of(new SimpleGrantedAuthority("ROLE_USER"))).when(authMock).getAuthorities();
  }
}
