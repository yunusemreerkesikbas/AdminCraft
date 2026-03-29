const readFirstEnv = (names: string[]): string | undefined => {
  for (const name of names) {
    const value = process.env[name]?.trim();
    if (value) {
      return value;
    }
  }

  return undefined;
};

export type TenantContext =
  | { headerName: "X-Tenant-Subdomain"; headerValue: string }
  | { headerName: "X-Tenant-ID"; headerValue: string };

export const getCmsBaseUrl = (): string => {
  const baseUrl = readFirstEnv(["NEXT_PUBLIC_CMS_API_URL"]);
  if (!baseUrl) {
    throw new Error("NEXT_PUBLIC_CMS_API_URL is required.");
  }

  return baseUrl.replace(/\/$/, "");
};

export const getGoogleAnalyticsId = (): string | undefined =>
  process.env.NEXT_PUBLIC_GA_ID?.trim() || undefined;

export const getGtmId = (): string | undefined =>
  process.env.NEXT_PUBLIC_GTM_ID?.trim() || undefined;

export const getTenantContext = (): TenantContext => {
  const tenantSubdomain = readFirstEnv(["TENANT_SUBDOMAIN", "NEXT_PUBLIC_TENANT_SUBDOMAIN"]);
  const tenantId = readFirstEnv(["TENANT_ID", "NEXT_PUBLIC_TENANT_ID"]);

  if (tenantSubdomain) {
    return {
      headerName: "X-Tenant-Subdomain",
      headerValue: tenantSubdomain,
    };
  }

  if (tenantId) {
    return {
      headerName: "X-Tenant-ID",
      headerValue: tenantId,
    };
  }

  throw new Error(
    "Tenant context is required. Set TENANT_SUBDOMAIN, TENANT_ID, or TENANT_HOSTNAME_PATTERN before starting the storefront.",
  );
};

export const getTenantHeaders = (): Record<string, string> => {
  const tenantContext = getTenantContext();
  return { [tenantContext.headerName]: tenantContext.headerValue };
};

/**
 * Async version that reads the tenant subdomain injected by proxy.ts via the
 * `x-tenant-subdomain` request header (next/headers). Falls back to the static
 * TENANT_SUBDOMAIN / TENANT_ID env vars for single-tenant deployments.
 */
export const getTenantHeadersAsync = async (): Promise<Record<string, string>> => {
  try {
    const { headers } = await import("next/headers");
    const h = await headers();
    const subdomain = h.get("x-tenant-subdomain");
    if (subdomain) {
      return { "X-Tenant-Subdomain": subdomain };
    }
  } catch {
    // Not in a server component context (e.g. proxy/middleware) — fall back to env
  }
  try {
    return getTenantHeaders();
  } catch {
    // No tenant context resolvable — TENANT_HOSTNAME_PATTERN deployment at build time
    // or outside a request context. Return empty and let the backend reject gracefully.
    console.warn("[tenant] No tenant context available. Set TENANT_SUBDOMAIN for build-time fetches.");
    return {};
  }
};
