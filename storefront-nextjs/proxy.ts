import { NextResponse } from "next/server";
import type { NextRequest } from "next/server";
import { fetchCmsJson } from "./lib/core/http/fetch-json";
import { resolveCmsEndpoint } from "./lib/core/http/endpoints";
import { isValidLocaleFormat } from "./lib/core/i18n/locale";

/**
 * Host header the browser used (reverse-proxy safe).
 * Behind Traefik/Docker, `nextUrl.hostname` may reflect an internal target; `Host` /
 * `X-Forwarded-Host` carry the public hostname used for TENANT_HOSTNAME checks.
 */
function getIncomingHostname(request: NextRequest): string {
  const forwarded = request.headers.get("x-forwarded-host");
  if (forwarded) {
    return forwarded.split(",")[0].trim().split(":")[0].toLowerCase();
  }
  const host = request.headers.get("host");
  if (host) {
    return host.split(":")[0].toLowerCase();
  }
  return request.nextUrl.hostname.toLowerCase();
}

/**
 * Resolves the tenant subdomain for the incoming request.
 *
 * Reads TENANT_SUBDOMAIN env var (set per deployment).
 * If TENANT_HOSTNAME is also set, validates that the incoming hostname matches — returns
 * null (→ 404) when it doesn't, preventing cross-tenant responses.
 */
function resolveTenantSubdomain(hostname: string): string | null {
  const subdomain = process.env.TENANT_SUBDOMAIN?.trim();
  if (!subdomain) return null;

  const expectedHostname = process.env.TENANT_HOSTNAME?.trim().toLowerCase();
  if (expectedHostname && hostname.toLowerCase() !== expectedHostname) {
    return null;
  }

  return subdomain;
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
  const subdomain = resolveTenantSubdomain(getIncomingHostname(request));
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
