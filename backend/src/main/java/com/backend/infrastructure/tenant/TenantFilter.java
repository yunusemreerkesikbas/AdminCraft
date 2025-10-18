package com.backend.infrastructure.tenant;

import com.backend.infrastructure.persistence.platform.entity.Tenant;
import com.backend.infrastructure.persistence.platform.repository.TenantPlatformRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
public class TenantFilter extends OncePerRequestFilter {

  private static final String TENANT_HEADER = "X-Tenant-ID";
  private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";

  private final TenantContext tenantContext;
  private final TenantPlatformRepository tenantRepository;

  public TenantFilter(TenantContext tenantContext, TenantPlatformRepository tenantRepository) {
    this.tenantContext = tenantContext;
    this.tenantRepository = tenantRepository;
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
      filterChain.doFilter(request, response);
      return;
    }

    try {
      String tenantIdHeader = request.getHeader(TENANT_HEADER);

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

      if ((tenantIdHeader == null || tenantIdHeader.isBlank())) {
        if (isSuperAdmin) {
          filterChain.doFilter(request, response);
          return;
        } else {
          log.warn("Missing tenant header for request: {}", path);
          response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Tenant identifier required");
          return;
        }
      }

      Long tenantId = Long.parseLong(tenantIdHeader);
      Tenant tenant = tenantRepository.findById(tenantId).orElse(null);

      if (tenant == null) {
        log.warn("Tenant not found: {}", tenantId);
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Tenant not found");
        return;
      }

      if (!"ACTIVE".equals(tenant.getStatus())) {
        log.warn("Inactive tenant access attempt: {}", tenantId);
        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Tenant not active");
        return;
      }

      tenantContext.setTenantId(String.valueOf(tenantId));
      tenantContext.setTenantDbName(tenant.getDbName());

      MDC.put("tenantId", String.valueOf(tenantId));
      MDC.put("tenantDb", tenant.getDbName());

      log.debug("Tenant context set: tenantId={}, dbName={}", tenantId, tenant.getDbName());

      filterChain.doFilter(request, response);

    } catch (NumberFormatException e) {
      log.error("Invalid tenant ID format: {}", request.getHeader(TENANT_HEADER));
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid tenant identifier");
    } finally {
      tenantContext.clear();
      MDC.clear();
    }
  }

  private boolean isWhitelisted(String path) {
    return path.startsWith("/api/actuator") ||
        path.startsWith("/api/health") ||
        path.startsWith("/api/auth") ||
        path.startsWith("/api/platform") ||
        path.startsWith("/api/modules/catalog") ||
        path.startsWith("/api/provisioning");
  }
}
