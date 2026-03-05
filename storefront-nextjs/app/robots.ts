import type { MetadataRoute } from "next";
import { getCmsBaseUrl } from "@/lib/utils";

const getTenantHeaders = (): HeadersInit => {
  const headers: HeadersInit = {};
  const subdomain = process.env.TENANT_SUBDOMAIN ?? process.env.NEXT_PUBLIC_TENANT_SUBDOMAIN;
  const tenantId = process.env.TENANT_ID ?? process.env.NEXT_PUBLIC_TENANT_ID;
  if (subdomain) headers["X-Tenant-Subdomain"] = subdomain;
  if (tenantId)  headers["X-Tenant-ID"] = tenantId;
  return headers;
};

const DEFAULT_ROBOTS: MetadataRoute.Robots = {
  rules: { userAgent: "*", allow: "/" },
};

export default async function robots(): Promise<MetadataRoute.Robots> {
  try {
    const url = `${getCmsBaseUrl()}/cms/robots.txt`;
    const response = await fetch(url, {
      headers: getTenantHeaders(),
      next: { revalidate: 3600 },
    });

    if (!response.ok) {
      return DEFAULT_ROBOTS;
    }

    const text = await response.text();
    return { rules: parseRulesFromText(text) };
  } catch {
    return DEFAULT_ROBOTS;
  }
}

// Parse plain-text robots.txt into MetadataRoute.Robots rules
function parseRulesFromText(text: string): MetadataRoute.Robots["rules"] {
  if (text.includes("Disallow: /\n") || text.trimEnd() === "Disallow: /") {
    return { userAgent: "*", disallow: "/" };
  }
  return { userAgent: "*", allow: "/" };
}
