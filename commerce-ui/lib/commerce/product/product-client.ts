import type { ApiResponse } from "@/lib/core/http/api-response";
import { getCommerceBaseUrl } from "@/lib/core/config/runtime-env";
import { buildJsonHeaders } from "@/lib/core/http/headers";
import { buildCommerceUrl } from "@/lib/core/http/query";
import { toApiLocale } from "@/lib/core/i18n/locale";
import type {
  PageResponse,
  ProductDeliveryResponse,
  ProductListDeliveryResponse,
  ProductMediaDelivery,
  ResponsiveMediaDelivery,
} from "./types";

const productByUidPath = (productUid: string): string =>
  `cms/products/${encodeURIComponent(productUid)}`;

const productSearchPath = "cms/products/search";

const normalizeMediaUrl = (
  media: ProductMediaDelivery | null,
): ProductMediaDelivery | null => {
  if (!media?.url) {
    return media;
  }

  const apiBaseUrl = getCommerceBaseUrl();
  const apiOrigin = new URL(apiBaseUrl).origin;

  return {
    ...media,
    url: new URL(media.url, apiOrigin).toString(),
  };
};

const normalizeResponsiveMedia = (
  media: ResponsiveMediaDelivery | null,
): ResponsiveMediaDelivery | null => {
  if (!media) {
    return null;
  }

  return {
    ...media,
    desktop: normalizeMediaUrl(media.desktop),
    mobile: normalizeMediaUrl(media.mobile),
  };
};

const isResponsiveMedia = (
  media: ResponsiveMediaDelivery | null,
): media is ResponsiveMediaDelivery => Boolean(media);

const normalizeProduct = (
  product: ProductDeliveryResponse,
): ProductDeliveryResponse => ({
  ...product,
  mainImage: normalizeResponsiveMedia(product.mainImage),
  gallery: product.gallery.map(normalizeResponsiveMedia).filter(isResponsiveMedia),
});

const normalizeAbsoluteUrl = (url: string | null): string | null => {
  if (!url) {
    return null;
  }

  const apiBaseUrl = getCommerceBaseUrl();
  const apiOrigin = new URL(apiBaseUrl).origin;

  return new URL(url, apiOrigin).toString();
};

const normalizeProductListItem = (
  product: ProductListDeliveryResponse,
): ProductListDeliveryResponse => ({
  ...product,
  thumbnailUrl: normalizeAbsoluteUrl(product.thumbnailUrl),
});

const normalizeProductPage = (
  page: PageResponse<ProductListDeliveryResponse>,
): PageResponse<ProductListDeliveryResponse> => ({
  ...page,
  content: page.content.map(normalizeProductListItem),
});

const buildProductSearchUrl = (
  query: string,
  lang: string,
  page: number,
  size: number,
): string => {
  const apiLocale = toApiLocale(lang);
  const url = new URL(buildCommerceUrl(productSearchPath, { lang: apiLocale, page, size }));
  url.searchParams.set("q", query);

  return url.toString();
};

export const getProductByUid = async (
  productUid: string,
  lang: string,
): Promise<ProductDeliveryResponse | null> => {
  const apiLocale = toApiLocale(lang);
  const response = await fetch(
    buildCommerceUrl(productByUidPath(productUid), { lang: apiLocale }),
    {
      headers: {
        ...(await buildJsonHeaders()),
        "Accept-Language": lang,
      },
      cache: "no-store",
    },
  );

  if (response.status === 404) {
    return null;
  }

  const payload = (await response.json()) as ApiResponse<ProductDeliveryResponse>;

  if (!response.ok || payload.result === "ERROR") {
    throw new Error(payload.message ?? "Product request failed.");
  }

  return payload.data ? normalizeProduct(payload.data) : null;
};

export const PRODUCT_LIST_PAGE_SIZE = 20;

export const searchProducts = async (
  query: string,
  lang: string,
  pageNumber = 1,
  size = PRODUCT_LIST_PAGE_SIZE,
): Promise<PageResponse<ProductListDeliveryResponse> | null> => {
  const pageIndex = Math.max(pageNumber, 1) - 1;
  const response = await fetch(buildProductSearchUrl(query, lang, pageIndex, size), {
    headers: {
      ...(await buildJsonHeaders()),
      "Accept-Language": lang,
    },
    cache: "no-store",
  });

  const payload = (await response.json()) as ApiResponse<
    PageResponse<ProductListDeliveryResponse>
  >;

  if (!response.ok || payload.result === "ERROR") {
    throw new Error(payload.message ?? "Product search request failed.");
  }

  return payload.data ? normalizeProductPage(payload.data) : null;
};
