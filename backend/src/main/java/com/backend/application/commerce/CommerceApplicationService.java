package com.backend.application.commerce;

/**
 * Marker for commerce application services.
 * <p>
 * Commerce use-case services should implement this interface when they are part
 * of the tenant-scoped commerce application boundary. The marker gives the
 * module a stable grouping point for future DI, policy, or AOP rules without
 * exposing infrastructure concerns to callers.
 */
public interface CommerceApplicationService {
}
