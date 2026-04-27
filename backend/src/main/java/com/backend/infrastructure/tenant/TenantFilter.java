package com.backend.infrastructure.tenant;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.backend.domain.enums.Currency;
import com.backend.domain.enums.Language;
import com.backend.infrastructure.persistence.platform.entity.Tenant;
import com.backend.infrastructure.persistence.platform.repository.TenantPlatformRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TenantFilter extends OncePerRequestFilter {

  private static final String TENANT_ID_HEADER = "X-Tenant-ID";
  private static final String TENANT_SUBDOMAIN_HEADER = "X-Tenant-Subdomain";
  private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";

  private final TenantContext tenantContext;
  private final TenantPlatformRepository tenantRepository;
  private final MultiTenantConnectionProvider connectionProvider;
  private final boolean trustForwardedHost;

  public TenantFilter(TenantContext tenantContext,
      TenantPlatformRepository tenantRepository,
      MultiTenantConnectionProvider connectionProvider,
      @Value("${app.tenant.trust-forwarded-host:false}") boolean trustForwardedHost) {
    this.tenantContext = tenantContext;
    this.tenantRepository = tenantRepository;
    this.connectionProvider = connectionProvider;
    this.trustForwardedHost = trustForwardedHost;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String correlationId = request.getHeader(CORRELATION_ID_HEADER);
    if (correlationId == null) {
      correlationId = UUID.randomUUID().toString();
    }
    MDC.put("correlationId", correlationId);

    String path = request.getRequestURI();

    try {
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      boolean isSuperAdmin = false;
      if (auth != null && auth.getAuthorities() != null) {
        for (GrantedAuthority authority : auth.getAuthorities()) {
          if ("ROLE_SUPER_ADMIN".equals(authority.getAuthority())) {
            isSuperAdmin = true;
            break;
          }
        }
      }

      if (isPublicNoTenantRequired(path)) {
        filterChain.doFilter(request, response);
        return;
      }

      if (isPlatformEndpoint(path)) {
        if (!isSuperAdmin) {
          response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
          return;
        }
        filterChain.doFilter(request, response);
        return;
      }

      if (isConfigAdminEndpoint(path)) {
        // SEC-106: when the caller carries a tenant-bound JWT, enforce that the
        // X-Tenant-ID header refers to the same tenant. Anonymous and SUPER_ADMIN
        // callers keep the existing pass-through behaviour.
        if (!isSuperAdmin && rejectIfTenantHeaderMismatchesJwt(request, response, auth)) {
          return;
        }
        filterChain.doFilter(request, response);
        return;
      }

      // SEC-002: ImpEx bypass removed. SUPER_ADMIN must also send X-Tenant-ID header.
      // Platform-DB seed data is applied via Flyway migrations, not via ImpEx at runtime.

      Tenant tenant = resolveTenantFromHeaders(request);
      if (tenant == null) {
        tenant = resolveTenantFromHostname(request);
      }

        if (tenant == null) {
          // Allow platform auth flows without tenant context.
          // Tenant-scoped validation is handled inside auth service (tenantId/subdomain + token checks).
          if (path.startsWith("/api/auth/login")
              || path.startsWith("/api/auth/refresh")
              || path.startsWith("/api/auth/logout")
              || path.startsWith("/api/auth/forgot-password")
              || path.startsWith("/api/auth/verify-otp")) {
            filterChain.doFilter(request, response);
            return;
          }

        log.warn("Missing or invalid tenant header for request: {}", path);
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Tenant identifier required");
        return;
      }

      if (!"ACTIVE".equals(tenant.getStatus())) {
        log.warn("Inactive tenant access attempt: tenantId={}", tenant.getId());
        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Tenant not active");
        return;
      }

      // SEC-003: cross-check JWT tenantId against the resolved tenant. SUPER_ADMIN is
      // platform-wide and exempt; anonymous public-tenant traffic also has no JWT tenant.
      if (!isSuperAdmin) {
        Long jwtTenantId = extractJwtTenantId(auth);
        if (jwtTenantId != null && !jwtTenantId.equals(tenant.getId())) {
          log.warn("Tenant mismatch: jwtTenantId={} headerTenantId={}", jwtTenantId, tenant.getId());
          response.sendError(HttpServletResponse.SC_FORBIDDEN, "Tenant mismatch");
          return;
        }
      }

      tenantContext.setTenantId(String.valueOf(tenant.getId()));
      tenantContext.setTenantDbName(tenant.getDatabaseName());
      tenantContext.setSubdomain(tenant.getSubdomain());
      tenantContext.setCurrency(Currency.fromCodeOrDefault(tenant.getCurrency()));
      tenantContext.setDefaultLanguage(Language.fromCode(tenant.getDefaultLanguage())
          .orElseThrow(() -> new IllegalStateException("Tenant default_language is required")));
      tenantContext.setSupportedLanguages(parseSupportedLanguages(tenant.getSupportedLanguages()));

      MDC.put("tenantId", String.valueOf(tenant.getId()));
      MDC.put("tenantDb", tenant.getDatabaseName());

      log.debug("Tenant context set: tenantId={}, dbName={}, subdomain={}", tenant.getId(), tenant.getDatabaseName(),
          tenant.getSubdomain());
      try {
        connectionProvider.warmUpConnectionPool(tenant.getDatabaseName());
      } catch (RuntimeException e) {
        log.error("Failed to initialize connection pool for tenant {}: {}", tenant.getId(), e.getMessage());
        response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
            "Tenant database is initializing, please retry in a moment");
        return;
      }

      filterChain.doFilter(request, response);

    } catch (NumberFormatException | IllegalStateException e) {
      log.error("Invalid tenant ID format or state: {}", e.getMessage());
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid tenant identifier");
    } finally {
      tenantContext.clear();
      MDC.clear();
    }
  }

  private Tenant resolveTenantFromHeaders(HttpServletRequest request) {
    String tenantIdHeader = request.getHeader(TENANT_ID_HEADER);
    if (tenantIdHeader != null && !tenantIdHeader.isBlank()) {
      try {
        Long tenantId = Long.parseLong(tenantIdHeader);
        Tenant tenant = tenantRepository.findById(tenantId).orElse(null);
        if (tenant != null) {
          log.debug("Tenant resolved by ID: {}", tenantId);
          return tenant;
        }
        log.warn("Tenant not found for ID: {}", tenantId);
      } catch (NumberFormatException e) {
        log.warn("Invalid tenant ID format: {}", tenantIdHeader);
      }
    }
    String subdomainHeader = request.getHeader(TENANT_SUBDOMAIN_HEADER);
    if (subdomainHeader != null && !subdomainHeader.isBlank()) {
      String subdomain = subdomainHeader.trim().toLowerCase();
      Tenant tenant = tenantRepository.findBySubdomain(subdomain).orElse(null);
      if (tenant != null) {
        log.debug("Tenant resolved by subdomain: {}", subdomain);
        return tenant;
      }
      log.warn("Tenant not found for subdomain: {}", subdomain);
    }

    return null;
  }

  private Tenant resolveTenantFromHostname(HttpServletRequest request) {
    // SEC-006: hostname-based resolution must rely only on trusted sources.
    // Origin/Referer headers are attacker-controlled (any browser/client can spoof
    // them) and were dropped to prevent cross-tenant resolution.
    String hostname = null;

    String forwardedHost = trustForwardedHost ? request.getHeader("X-Forwarded-Host") : null;
    if (forwardedHost != null && !forwardedHost.isBlank()) {
      hostname = forwardedHost.split(",")[0].trim();
      log.debug("Using X-Forwarded-Host: {}", hostname);
    }

    if (hostname == null) {
      hostname = request.getServerName();
    }

    if (hostname == null || hostname.isBlank() || "localhost".equals(hostname)) {
      return null;
    }

    String subdomain = null;
    int firstDot = hostname.indexOf('.');
    if (firstDot > 0) {
      subdomain = hostname.substring(0, firstDot);
    }

    if (subdomain != null && !subdomain.isBlank()) {
      Tenant tenant = tenantRepository.findBySubdomain(subdomain.toLowerCase()).orElse(null);
      if (tenant != null) {
        log.debug("Tenant resolved by hostname: {} -> {}", hostname, subdomain);
        return tenant;
      }
    }
    return null;
  }

  /**
   * SEC-003: read the {@code tenantId} claim that {@link com.backend.infrastructure.security.JwtAuthenticationFilter}
   * stored in the authentication details map. Returns {@code null} when the caller
   * is anonymous or the claim is absent (e.g. SUPER_ADMIN access tokens).
   */
  private Long extractJwtTenantId(Authentication auth) {
    if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
      return null;
    }
    Object details = auth.getDetails();
    if (!(details instanceof Map<?, ?> map)) {
      return null;
    }
    Object claim = map.get("tenantId");
    if (claim == null) {
      return null;
    }
    if (claim instanceof Number number) {
      return number.longValue();
    }
    try {
      return Long.parseLong(claim.toString());
    } catch (NumberFormatException ex) {
      log.warn("JWT tenantId claim is not a number: {}", claim);
      return null;
    }
  }

  /**
   * SEC-106: enforce that the {@code X-Tenant-ID} header (when supplied) matches the
   * JWT tenantId for non-SUPER_ADMIN callers on bypass paths that skip full tenant
   * resolution (e.g. config admin). Returns {@code true} after writing 403, in which
   * case the caller must abort the filter chain.
   */
  private boolean rejectIfTenantHeaderMismatchesJwt(HttpServletRequest request,
      HttpServletResponse response, Authentication auth) throws IOException {
    Long jwtTenantId = extractJwtTenantId(auth);
    if (jwtTenantId == null) {
      return false;
    }
    String headerValue = request.getHeader(TENANT_ID_HEADER);
    if (headerValue == null || headerValue.isBlank()) {
      return false;
    }
    try {
      Long headerTenantId = Long.parseLong(headerValue);
      if (!jwtTenantId.equals(headerTenantId)) {
        log.warn("Tenant mismatch on bypass path: jwtTenantId={} headerTenantId={}",
            jwtTenantId, headerTenantId);
        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Tenant mismatch");
        return true;
      }
    } catch (NumberFormatException ex) {
      log.warn("Invalid tenant header on bypass path: {}", headerValue);
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid tenant identifier");
      return true;
    }
    return false;
  }

  private boolean isPublicNoTenantRequired(String path) {
    // NOTE: Swagger/OpenAPI accessible for development. Disable springdoc in
    // production via config.
    return path.startsWith("/api/actuator") ||
        path.startsWith("/api/health") ||
        path.startsWith("/api/platform/cms/config") ||
        path.startsWith("/api/config/auth") ||
        path.startsWith("/api/platform/public/newsletter") ||
        path.startsWith("/api/platform/public/demo-requests") ||
        path.startsWith("/api/swagger-ui") ||
        path.startsWith("/api/v3/api-docs");
  }

  private boolean isPlatformEndpoint(String path) {
    if (path.startsWith("/api/platform/cms/config")) {
      return false;
    }
    if (path.startsWith("/api/platform/public/newsletter")) {
      return false;
    }
    if (path.startsWith("/api/platform/public/demo-requests")) {
      return false;
    }
    if (path.startsWith("/api/tenants/current")) {
      return false;
    }
    return path.startsWith("/api/platform") ||
        path.startsWith("/api/modules/catalog") ||
        path.startsWith("/api/provisioning") ||
        path.startsWith("/api/tenants");
  }

  private boolean isConfigAdminEndpoint(String path) {
    return path.startsWith("/api/config/admin");
  }

  private Set<Language> parseSupportedLanguages(String json) {
    if (json == null || json.isBlank()) {
      return Set.of();
    }
    return Arrays.stream(json.replaceAll("[\\[\\]\"\\s]", "").split(","))
        .filter(s -> !s.isBlank())
        .flatMap(code -> Language.fromCode(code).stream())
        .collect(Collectors.toSet());
  }
}
