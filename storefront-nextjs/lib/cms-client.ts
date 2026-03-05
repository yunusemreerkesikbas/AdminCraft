import { cache } from "react";
import {
  ApiResponse,
  CategoryDeliveryResponse,
  NavigationDeliveryResponse,
  PageDeliveryResponse,
  PageResponse,
  ProductDeliveryResponse,
  ProductListDeliveryResponse,
  SiteDeliveryResponse,
  TenantConfigResponse,
} from "./types";
import { normalizeLanguage } from "./i18n";
import { getCmsBaseUrl } from "./utils";

const getTenantSubdomain = (): string | undefined =>
  process.env.TENANT_SUBDOMAIN ?? process.env.NEXT_PUBLIC_TENANT_SUBDOMAIN ?? undefined;

const getTenantId = (): string | undefined =>
  process.env.TENANT_ID ?? process.env.NEXT_PUBLIC_TENANT_ID ?? undefined;

const buildUrl = (path: string, params?: Record<string, string | number | undefined>): string => {
  const baseUrl = getCmsBaseUrl();
  const normalizedPath = path.startsWith("/") ? path.slice(1) : path;
  const url = new URL(normalizedPath, `${baseUrl}/`);

  if (params) {
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && `${value}`.length > 0) {
        url.searchParams.set(key, String(value));
      }
    });
  }

  return url.toString();
};

type RequestOptions = {
  cache?: RequestCache;
  revalidate?: number;
};

const request = async <T>(
  path: string,
  params?: Record<string, string | number | undefined>,
  options?: RequestOptions
): Promise<T | null> => {
  const url = buildUrl(path, params);
  const headers: HeadersInit = { Accept: "application/json" };
  const tenantSubdomain = getTenantSubdomain();
  const tenantId = getTenantId();

  if (tenantSubdomain) {
    headers["X-Tenant-Subdomain"] = tenantSubdomain;
  }
  if (tenantId) {
    headers["X-Tenant-ID"] = tenantId;
  }

  const fetchOptions: RequestInit & { next?: { revalidate?: number } } = {
    headers,
    cache: options?.cache ?? "force-cache",
  };

  if (options?.revalidate !== undefined) {
    fetchOptions.next = { revalidate: options.revalidate };
  }

  const response = await fetch(url, fetchOptions);

  if (response.status === 404) {
    return null;
  }

  // 429 rate-limit ve 5xx server hataları — JSON parse'tan önce graceful null dön
  if (response.status === 429 || response.status >= 500) {
    console.error(`[CMS] Request failed ${response.status}: ${url}`);
    return null;
  }

  const payload = (await response.json()) as ApiResponse<T>;

  if (!response.ok) {
    const message = payload?.message ?? `Request failed with status ${response.status}`;
    throw new Error(message);
  }

  if (payload.result === "ERROR") {
    return null;
  }

  return payload.data ?? null;
};

export const resolvePage = cache(
  async (
    lang?: string,
    pageType?: string,
    pageLabelOrId?: string,
    code?: string
  ): Promise<PageDeliveryResponse | null> => {
    const language = normalizeLanguage(lang);
    const query: Record<string, string | number | undefined> = { lang: language };

    if (pageType) query.pageType = pageType;
    if (pageLabelOrId) query.pageLabelOrId = pageLabelOrId;
    if (code) query.code = code;

    return request<PageDeliveryResponse>("cms/pages", query, { revalidate: 30 });
  }
);

export const fetchSiteConfig = cache(
  async (): Promise<SiteDeliveryResponse | null> =>
    request<SiteDeliveryResponse>("cms/site", undefined, { revalidate: 300 })
);

export const fetchTenantConfig = cache(async (): Promise<TenantConfigResponse | null> => {
  try {
    return await request<TenantConfigResponse>("tenants/current/detail", undefined, {
      revalidate: 300,
    });
  } catch {
    // Endpoint auth gerektiriyor (401) veya erişilemiyor — graceful null dön
    return null;
  }
});

export const fetchProduct = async (
  uid: string,
  lang?: string
): Promise<ProductDeliveryResponse | null> => {
  const language = normalizeLanguage(lang);
  return request<ProductDeliveryResponse>(`cms/products/${uid}`, { lang: language }, { revalidate: 30 });
};

export const fetchProductsByCategory = async (
  categoryUid: string,
  lang?: string,
  page = 0,
  size = 20
): Promise<PageResponse<ProductListDeliveryResponse> | null> => {
  const language = normalizeLanguage(lang);
  return request<PageResponse<ProductListDeliveryResponse>>(
    `cms/products/category/${categoryUid}`,
    { lang: language, page, size },
    { revalidate: 30 }
  );
};

export const searchProducts = async (
  query: string,
  lang?: string,
  page = 0,
  size = 20
): Promise<PageResponse<ProductListDeliveryResponse> | null> => {
  const language = normalizeLanguage(lang);
  return request<PageResponse<ProductListDeliveryResponse>>(
    "cms/products/search",
    {
      lang: language,
      q: query,
      page,
      size,
    },
    { cache: "no-store" }
  );
};

export const fetchCategoryTree = async (
  lang?: string
): Promise<CategoryDeliveryResponse[] | null> => {
  const language = normalizeLanguage(lang);
  return request<CategoryDeliveryResponse[]>(
    "cms/products/categories",
    { lang: language },
    { revalidate: 300 }
  );
};

export const fetchNavigation = cache(
  async (uid: string, lang?: string): Promise<NavigationDeliveryResponse | null> => {
    const params = lang ? { lang: normalizeLanguage(lang) } : {};
    return request<NavigationDeliveryResponse>(`cms/navigation/${uid}`, params, { revalidate: 300 });
  }
);
