import { NextResponse } from "next/server";
import type { NextRequest } from "next/server";
import { isValidLocaleFormat } from "./lib/core/i18n/locale";

type TenantHeader =
  | { name: "x-tenant-subdomain"; value: string }
  | { name: "x-tenant-id"; value: string };

const TRUST_X_FORWARDED_HOST =
  process.env.TRUST_X_FORWARDED_HOST === "1" ||
  process.env.TRUST_X_FORWARDED_HOST === "true";

function parseHostname(rawHeader: string): string {
  const raw = rawHeader.split(",")[0]?.trim() ?? "";
  if (!raw) {
    return "";
  }
  if (raw.startsWith("[")) {
    const end = raw.indexOf("]");
    if (end > 1) {
      return raw.slice(1, end).toLowerCase();
    }
  }
  try {
    const withScheme = raw.includes("://") ? raw : `http://${raw}`;
    return new URL(withScheme).hostname.toLowerCase();
  } catch {
    return raw.split(":")[0]?.toLowerCase() ?? "";
  }
}

function getIncomingHostname(request: NextRequest): string {
  if (TRUST_X_FORWARDED_HOST) {
    const forwarded = request.headers.get("x-forwarded-host");
    if (forwarded) {
      return parseHostname(forwarded);
    }
  }

  const host = request.headers.get("host");
  if (host) {
    return parseHostname(host);
  }

  return request.nextUrl.hostname.toLowerCase();
}

function readEnv(name: string): string | null {
  return process.env[name]?.trim() || null;
}

function resolveTenantHeader(hostname: string): TenantHeader | null {
  const expectedHostname = readEnv("TENANT_HOSTNAME")?.toLowerCase();
  if (expectedHostname && hostname.toLowerCase() !== expectedHostname) {
    return null;
  }

  const subdomain = process.env.TENANT_SUBDOMAIN?.trim();
  if (subdomain) {
    return { name: "x-tenant-subdomain", value: subdomain };
  }

  const tenantId = readEnv("TENANT_ID");
  if (tenantId) {
    return { name: "x-tenant-id", value: tenantId };
  }

  return null;
}

export function proxy(request: NextRequest) {
  const { pathname } = request.nextUrl;

  if (pathname.startsWith("/_next/") || pathname.startsWith("/api/")) {
    return NextResponse.next();
  }

  const tenantHeader = resolveTenantHeader(getIncomingHostname(request));
  if (!tenantHeader) {
    return new NextResponse(null, { status: 404 });
  }

  const segments = pathname.split("/").filter(Boolean);
  const locale = segments[0];
  const hasLocaleSegment = isValidLocaleFormat(locale);

  const requestHeaders = new Headers(request.headers);
  requestHeaders.set(tenantHeader.name, tenantHeader.value);

  if (hasLocaleSegment) {
    requestHeaders.set("x-lang", locale);
    requestHeaders.set("x-next-intl-locale", locale);
  }

  return NextResponse.next({ request: { headers: requestHeaders } });
}

export const config = {
  matcher: ["/((?!_next/static|_next/image|api/|favicon.ico).*)"],
};
