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

  // Prefer the subdomain injected by proxy.ts; fall back to static env var.
  const subdomain = request.headers.get("x-tenant-subdomain");
  if (subdomain) {
    headers.set("X-Tenant-Subdomain", subdomain);
  } else {
    for (const [name, value] of Object.entries(getTenantHeaders())) {
      headers.set(name, value);
    }
  }

  return headers;
};
