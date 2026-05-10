package com.backend.presentation.filter;

import java.io.IOException;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.backend.application.cms.preview.CmsPreviewTicket;
import com.backend.application.cms.preview.CmsPreviewTicketService;
import com.backend.application.cms.preview.CmsRequestContext;
import com.backend.domain.port.TenantContextPort;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class CmsPreviewFilter extends OncePerRequestFilter {

  private static final String HEADER = "X-Cms-Preview-Ticket";
  private static final String QUERY_PARAM = "preview";
  private static final String CMS_PATH_PREFIX = "/cms/";
  private static final String CMS_PREVIEW_PREFIX = "/cms/preview";

  private final CmsPreviewTicketService ticketService;
  private final CmsRequestContext requestContext;
  private final TenantContextPort tenantContext;

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getServletPath();
    if (path == null || !path.startsWith(CMS_PATH_PREFIX)) {
      return true;
    }
    return path.startsWith(CMS_PREVIEW_PREFIX);
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    try {
      String token = request.getHeader(HEADER);
      if (token == null || token.isBlank()) {
        token = request.getParameter(QUERY_PARAM);
      }
      if (token != null && !token.isBlank()) {
        Optional<CmsPreviewTicket> verified = ticketService.verify(token);
        if (verified.isEmpty() || !activate(verified.get())) {
          response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid CMS preview ticket");
          return;
        }
      }
      filterChain.doFilter(request, response);
    } finally {
      requestContext.clear();
    }
  }

  private boolean activate(CmsPreviewTicket ticket) {
    String tenantIdStr = tenantContext.getTenantId();
    if (tenantIdStr == null) {
      log.warn("CMS preview ticket activation failed: tenant context not set");
      return false;
    }
    long currentTenantId;
    try {
      currentTenantId = Long.parseLong(tenantIdStr);
    } catch (NumberFormatException ex) {
      return false;
    }
    if (!ticket.matchesTenant(currentTenantId)) {
      log.debug("Preview ticket tenant mismatch: ticket={}, request={}", ticket.tenantId(), currentTenantId);
      return false;
    }
    requestContext.enablePreview();
    log.debug("Preview activated: tenantId={}, pageId={}", ticket.tenantId(), ticket.pageId());
    return true;
  }
}
