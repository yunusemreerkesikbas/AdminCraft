import type {
  CmsMediaDelivery,
  PageDeliveryResponse,
  PageResponse,
  ProductDeliveryResponse,
  ProductListDeliveryResponse,
  ShellDeliveryResponse,
  SiteDeliveryResponse,
  SitemapPageEntry,
} from "../../types";
import { normalizeLanguage } from "../i18n/locale";
import { resolveCmsEndpoint } from "../http/endpoints";
import { fetchCmsJson } from "../http/fetch-json";

export interface GetCmsPageOptions {
  pageType?: string;
  pageLabelOrId?: string;
  code?: string;
  previewTicket?: string;
  previewPageId?: number;
}

export const getCmsPage = async (
  lang?: string,
  options?: GetCmsPageOptions,
): Promise<PageDeliveryResponse | null> => {
  const language = normalizeLanguage(lang);
  const query: Record<string, string | number | undefined> = { lang: language };

  if (options?.pageType) {
    query.pageType = options.pageType;
  }

  if (options?.pageLabelOrId) {
    query.pageLabelOrId = options.pageLabelOrId;
  }

  if (options?.code) {
    query.code = options.code;
  }

  if (options?.previewPageId) {
    query.previewPageId = options.previewPageId;
  }

  return fetchCmsJson<PageDeliveryResponse>(resolveCmsEndpoint("cmsPages"), query, {
    revalidate: 30,
    previewTicket: options?.previewTicket,
  });
};

export const getSiteConfig = async (
  lang?: string,
  previewTicket?: string,
): Promise<SiteDeliveryResponse | null> =>
  fetchCmsJson<SiteDeliveryResponse>(
    resolveCmsEndpoint("cmsSite"),
    { lang: normalizeLanguage(lang) },
    { revalidate: 300, previewTicket },
  );

export const getShell = async (
  lang?: string,
  previewTicket?: string,
): Promise<ShellDeliveryResponse | null> =>
  fetchCmsJson<ShellDeliveryResponse>(
    resolveCmsEndpoint("cmsShell"),
    { lang: normalizeLanguage(lang) },
    { revalidate: 300, previewTicket },
  );

export const getSitemapPages = async (lang: string): Promise<SitemapPageEntry[] | null> =>
  fetchCmsJson<SitemapPageEntry[]>(
    resolveCmsEndpoint("cmsSitemapPages"),
    { lang: normalizeLanguage(lang) },
    { revalidate: 3600 },
  );

export const getProduct = async (
  uid: string,
  lang?: string,
): Promise<ProductDeliveryResponse | null> =>
  fetchCmsJson<ProductDeliveryResponse>(
    resolveCmsEndpoint("cmsProductByUid", { uid }),
    { lang: normalizeLanguage(lang) },
    { revalidate: 30 },
  );

export const getCategoryProducts = async (
  categoryUid: string,
  lang?: string,
  page = 0,
  size = 20,
): Promise<PageResponse<ProductListDeliveryResponse> | null> =>
  fetchCmsJson<PageResponse<ProductListDeliveryResponse>>(
    resolveCmsEndpoint("cmsProductsByCategory", { categoryUid }),
    { lang: normalizeLanguage(lang), page, size },
    { revalidate: 30 },
  );

export const searchProducts = async (
  query: string,
  lang?: string,
  page = 0,
  size = 20,
): Promise<PageResponse<ProductListDeliveryResponse> | null> =>
  fetchCmsJson<PageResponse<ProductListDeliveryResponse>>(
    resolveCmsEndpoint("cmsProductSearch"),
    {
      lang: normalizeLanguage(lang),
      q: query,
      page,
      size,
    },
    { cache: "no-store" },
  );

export const getMediaByUids = async (
  uids: string[],
): Promise<Record<string, CmsMediaDelivery>> => {
  const distinctUids = [...new Set(uids.map((uid) => uid.trim()).filter(Boolean))];
  if (distinctUids.length === 0) {
    return {};
  }

  const result = await fetchCmsJson<CmsMediaDelivery[]>(
    resolveCmsEndpoint("cmsMediaBatch"),
    { uids: distinctUids.join(",") },
    { revalidate: 300 },
  );

  if (!result) {
    return {};
  }

  return result.reduce<Record<string, CmsMediaDelivery>>((accumulator, item) => {
    if (item?.uid) {
      accumulator[item.uid] = item;
    }

    return accumulator;
  }, {});
};
