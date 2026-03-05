import { NextResponse } from "next/server";
import type { NextRequest } from "next/server";
import { FALLBACK_LOCALE, isValidLocaleFormat } from "./lib/i18n";
import { getCmsBaseUrl } from "./lib/utils";

const getTenantSubdomain = (): string | undefined =>
  process.env.TENANT_SUBDOMAIN ?? process.env.NEXT_PUBLIC_TENANT_SUBDOMAIN ?? undefined;

const getTenantId = (): string | undefined =>
  process.env.TENANT_ID ?? process.env.NEXT_PUBLIC_TENANT_ID ?? undefined;

const buildCmsUrl = (path: string): string => {
  const baseUrl = getCmsBaseUrl();
  const normalizedPath = path.startsWith("/") ? path.slice(1) : path;
  return `${baseUrl}/${normalizedPath}`;
};

export async function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl;

  if (
    pathname.startsWith("/_next/") ||
    pathname === "/favicon.ico" ||
    pathname === "/robots.txt" ||
    pathname === "/sitemap.xml"
  ) {
    return NextResponse.next();
  }

  const segments = pathname.split("/").filter(Boolean);
  const locale = segments[0];

  // Tarayıcı/sistem özel yolları: locale segmentinden sonra gelen .well-known vb.
  if (segments[1] === ".well-known") {
    return new NextResponse(null, { status: 404 });
  }

  // Format validasyonu: gerçek dil kontrolü [lang]/layout.tsx'de yapılır
  if (!isValidLocaleFormat(locale)) {
    const url = request.nextUrl.clone();
    url.pathname = `/${FALLBACK_LOCALE}${pathname === "/" ? "" : pathname}`;
    return NextResponse.redirect(url);
  }

  if (segments[1] === "maintenance") {
    const requestHeaders = new Headers(request.headers);
    requestHeaders.set("x-lang", locale);
    return NextResponse.next({ request: { headers: requestHeaders } });
  }

  try {
    const cmsUrl = buildCmsUrl("cms/site");
    const headers: HeadersInit = { Accept: "application/json" };
    const tenantSubdomain = getTenantSubdomain();
    const tenantId = getTenantId();

    if (tenantSubdomain) {
      headers["X-Tenant-Subdomain"] = tenantSubdomain;
    }
    if (tenantId) {
      headers["X-Tenant-ID"] = tenantId;
    }

    const response = await fetch(cmsUrl, { headers, cache: "no-store" });
    if (response.ok) {
      const payload = (await response.json()) as {
        result?: string;
        data?: { maintenanceMode?: boolean };
      };
      const maintenanceMode =
        payload?.result === "SUCCESS" && payload?.data?.maintenanceMode === true;
      if (maintenanceMode) {
        const url = request.nextUrl.clone();
        url.pathname = `/${locale}/maintenance`;
        url.search = "";
        return NextResponse.redirect(url);
      }
    }
  } catch {
  }

  const requestHeaders = new Headers(request.headers);
  requestHeaders.set("x-lang", locale);

  return NextResponse.next({ request: { headers: requestHeaders } });
}

export const config = {
  matcher: ["/((?!_next/static|_next/image|favicon.ico|robots.txt|sitemap.xml).*)"],
};
