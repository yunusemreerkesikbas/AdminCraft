import { NextResponse } from "next/server";
import type { NextRequest } from "next/server";
import { fetchCmsJson } from "./lib/core/http/fetch-json";
import { resolveCmsEndpoint } from "./lib/core/http/endpoints";
import { isValidLocaleFormat } from "./lib/core/i18n/locale";
import { extractSubdomainFromPattern } from "./lib/core/config/runtime-env";

/**
 * Resolves the tenant subdomain for the incoming request.
 *
 * Priority:
 * 1. TENANT_HOSTNAME_PATTERN env var — dynamic multi-tenant: one deployment, many tenants.
 *    Pattern uses `{subdomain}` as a placeholder, e.g. `s1-{subdomain}.craftive.io`.
 *    Returns null when the hostname does not match (→ 404).
 * 2. TENANT_SUBDOMAIN env var — single-tenant deployment (backward compat).
 */
function resolveTenantSubdomain(hostname: string): string | null {
  const pattern = process.env.TENANT_HOSTNAME_PATTERN?.trim();
  if (pattern) {
    return extractSubdomainFromPattern(hostname.toLowerCase(), pattern.toLowerCase());
  }
  return process.env.TENANT_SUBDOMAIN?.trim() ?? null;
}

export async function proxy(request: NextRequest) {
  const { pathname } = request.nextUrl;

  if (
    pathname.startsWith("/_next/") ||
    pathname.startsWith("/api/") ||
    pathname.startsWith("/cms-media/") ||
    pathname === "/favicon.ico" ||
    pathname === "/robots.txt" ||
    pathname === "/sitemap.xml"
  ) {
    return NextResponse.next();
  }

  // Resolve tenant subdomain from hostname (dynamic) or env var (static).
  const subdomain = resolveTenantSubdomain(request.nextUrl.hostname);
  if (!subdomain) {
    return new NextResponse(null, { status: 404 });
  }

  const segments = pathname.split("/").filter(Boolean);
  const locale = segments[0];

  // Tarayıcı/sistem özel yolları: locale segmentinden sonra gelen .well-known vb.
  if (segments[1] === ".well-known") {
    return new NextResponse(null, { status: 404 });
  }

  const hasLocaleSegment = isValidLocaleFormat(locale);

  if (!hasLocaleSegment) {
    return NextResponse.next();
  }

  if (segments[1] === "maintenance") {
    const requestHeaders = new Headers(request.headers);
    requestHeaders.set("x-lang", locale);
    requestHeaders.set("x-next-intl-locale", locale);
    requestHeaders.set("x-tenant-subdomain", subdomain);
    return NextResponse.next({ request: { headers: requestHeaders } });
  }

  try {
    // Pass tenant header explicitly — next/headers is not available in proxy context.
    const site = await fetchCmsJson<{ maintenanceMode?: boolean }>(
      resolveCmsEndpoint("cmsSite"),
      undefined,
      { cache: "no-store", extraHeaders: { "X-Tenant-Subdomain": subdomain } },
    );

    if (site?.maintenanceMode) {
      const url = request.nextUrl.clone();
      url.pathname = `/${locale}/maintenance`;
      url.search = "";
      return NextResponse.redirect(url);
    }
  } catch (error) {
    console.warn("[proxy] CMS site check failed:", error);
  }

  const requestHeaders = new Headers(request.headers);
  requestHeaders.set("x-lang", locale);
  requestHeaders.set("x-next-intl-locale", locale);
  requestHeaders.set("x-tenant-subdomain", subdomain);

  return NextResponse.next({ request: { headers: requestHeaders } });
}

export const config = {
  matcher: ["/((?!_next/static|_next/image|api/|cms-media/|favicon.ico|robots.txt|sitemap.xml).*)"],
};
