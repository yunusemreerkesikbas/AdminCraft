import type { ApiResponse } from "@/lib/core/http/api-response";
import { getCommerceBaseUrl } from "@/lib/core/config/runtime-env";
import { buildJsonHeaders } from "@/lib/core/http/headers";
import { buildCommerceUrl } from "@/lib/core/http/query";
import { toApiLocale } from "@/lib/core/i18n/locale";
import type {
  ProductDeliveryResponse,
  ProductMediaDelivery,
  ResponsiveMediaDelivery,
} from "./types";

const productByUidPath = (productUid: string): string =>
  `cms/products/${encodeURIComponent(productUid)}`;

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
