import type { NextRequest } from "next/server";
import { getTenantHeaders, getTenantHeadersAsync } from "../config/runtime-env";

export const buildJsonHeaders = async (): Promise<HeadersInit> => ({
  Accept: "application/json",
  ...(await getTenantHeadersAsync()),
});

export const buildForwardHeaders = (
  request: NextRequest,
  names: string[] = ["accept"],
): Headers => {
  const headers = new Headers();

  for (const name of names) {
    const value = request.headers.get(name);
    if (value) {
      headers.set(name, value);
    }
  }

  const subdomain = request.headers.get("x-tenant-subdomain");
  const tenantId = request.headers.get("x-tenant-id");
  if (subdomain) {
    headers.set("X-Tenant-Subdomain", subdomain);
  } else if (tenantId) {
    headers.set("X-Tenant-ID", tenantId);
  } else {
    for (const [name, value] of Object.entries(getTenantHeaders())) {
      headers.set(name, value);
    }
  }

  return headers;
};
