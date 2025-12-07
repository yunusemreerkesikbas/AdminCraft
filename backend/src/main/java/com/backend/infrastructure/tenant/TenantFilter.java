package com.backend.infrastructure.tenant;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

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

  public TenantFilter(TenantContext tenantContext,
      TenantPlatformRepository tenantRepository,
      MultiTenantConnectionProvider connectionProvider) {
    this.tenantContext = tenantContext;
    this.tenantRepository = tenantRepository;
    this.connectionProvider = connectionProvider;
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
    if (isWhitelisted(path)) {
      if (path.startsWith("/api/tenants")) {
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
        if (!isSuperAdmin) {
        } else {
          filterChain.doFilter(request, response);
          return;
        }
      } else {
        filterChain.doFilter(request, response);
        return;
      }
    }

    try {
      Tenant tenant = resolveTenantFromHeaders(request);

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

      if (tenant == null) {
        if (isSuperAdmin) {
          filterChain.doFilter(request, response);
          return;
        } else {
          log.warn("Missing or invalid tenant header for request: {}", path);
          response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Tenant identifier required");
          return;
        }
      }

      if (!"ACTIVE".equals(tenant.getStatus())) {
        log.warn("Inactive tenant access attempt: tenantId={}", tenant.getId());
        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Tenant not active");
        return;
      }

      tenantContext.setTenantId(String.valueOf(tenant.getId()));
      tenantContext.setTenantDbName(tenant.getDatabaseName());

      MDC.put("tenantId", String.valueOf(tenant.getId()));
      MDC.put("tenantDb", tenant.getDatabaseName());

      log.debug("Tenant context set: tenantId={}, dbName={}", tenant.getId(), tenant.getDatabaseName());
      try {
        connectionProvider.warmUpConnectionPool(tenant.getDatabaseName());
      } catch (RuntimeException e) {
        log.error("Failed to initialize connection pool for tenant {}: {}", tenant.getId(), e.getMessage());
        response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
            "Tenant database is initializing, please retry in a moment");
        return;
      }

      filterChain.doFilter(request, response);

    } catch (NumberFormatException e) {
      log.error("Invalid tenant ID format: {}", request.getHeader(TENANT_ID_HEADER));
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

  private boolean isWhitelisted(String path) {
    return path.startsWith("/api/actuator") ||
        path.startsWith("/api/health") ||
        path.startsWith("/api/auth") ||
        path.startsWith("/api/platform") ||
        path.startsWith("/api/modules/catalog") ||
        path.startsWith("/api/provisioning") ||
        path.startsWith("/api/tenants");
  }
}
