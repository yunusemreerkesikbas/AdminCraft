import { NextResponse } from "next/server";
import type { NextRequest } from "next/server";
import { fetchCmsJson } from "./lib/core/http/fetch-json";
import { resolveCmsEndpoint } from "./lib/core/http/endpoints";
import { isValidLocaleFormat } from "./lib/core/i18n/locale";

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

  const expectedHostname = process.env.TENANT_HOSTNAME?.trim().toLowerCase();
  if (expectedHostname) {
    // request.nextUrl.hostname: Next.js middleware'in X-Forwarded-Host/Host'tan
    // otomatik olarak çözümlediği güvenilir hostname değeri.
    const requestHost = request.nextUrl.hostname.toLowerCase();
    if (requestHost && requestHost !== expectedHostname) {
      return new NextResponse(null, { status: 404 });
    }
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
    return NextResponse.next({ request: { headers: requestHeaders } });
  }

  try {
    const site = await fetchCmsJson<{ maintenanceMode?: boolean }>(
      resolveCmsEndpoint("cmsSite"),
      undefined,
      { cache: "no-store" },
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

  return NextResponse.next({ request: { headers: requestHeaders } });
}

export const proxyConfig = {
  matcher: ["/((?!_next/static|_next/image|api/|cms-media/|favicon.ico|robots.txt|sitemap.xml).*)"],
};
