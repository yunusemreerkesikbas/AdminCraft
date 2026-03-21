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
    "Tenant context is required. Set TENANT_SUBDOMAIN or TENANT_ID before starting the storefront.",
  );
};

export const getTenantHeaders = (): Record<string, string> => {
  const tenantContext = getTenantContext();
  return { [tenantContext.headerName]: tenantContext.headerValue };
};
