import { getTranslations } from "next-intl/server";
import {
  ProductListView,
  type ProductListCopy,
} from "@/components/product/ProductListView";
import { PageShell } from "@/components/ui/PageShell";
import { ProductFrame } from "@/components/ui/StorefrontPrimitives";
import { searchProducts } from "@/lib/commerce/product/product-client";
import type {
  PageResponse,
  ProductListDeliveryResponse,
} from "@/lib/commerce/product/types";

type ProductListingPageProps = {
  params: Promise<{ lang: string }>;
  searchParams: Promise<{
    q?: string | string[];
    page?: string | string[];
  }>;
};

const firstParamValue = (value?: string | string[]): string =>
  Array.isArray(value) ? value[0] ?? "" : value ?? "";

const parsePageNumber = (value?: string | string[]): number => {
  const parsed = Number.parseInt(firstParamValue(value), 10);
  return Number.isFinite(parsed) && parsed >= 0 ? parsed : 0;
};

export default async function ProductsPage({
  params,
  searchParams,
}: ProductListingPageProps) {
  const { lang } = await params;
  const queryParams = await searchParams;
  const query = firstParamValue(queryParams.q).trim();
  const pageNumber = parsePageNumber(queryParams.page);
  const translate = await getTranslations("ProductListing");
  let products: PageResponse<ProductListDeliveryResponse> | null = null;
  let errorMessage: string | null = null;

  try {
    products = await searchProducts(query, lang, pageNumber);
  } catch {
    errorMessage = translate("errorDescription");
  }

  const copy: ProductListCopy = {
    searchLabel: translate("searchLabel"),
    searchPlaceholder: translate("searchPlaceholder"),
    searchAction: translate("searchAction"),
    clearAction: translate("clearAction"),
    resultsLabel: translate("resultsLabel"),
    emptyTitle: translate("emptyTitle"),
    emptyDescription: translate("emptyDescription"),
    productTypeFallback: translate("productTypeFallback"),
    priceLabel: translate("priceLabel"),
    detailsAction: translate("detailsAction"),
    previousAction: translate("previousAction"),
    nextAction: translate("nextAction"),
    pageLabel: translate("pageLabel"),
    imageAltFallback: translate("imageAltFallback"),
    errorTitle: translate("errorTitle"),
    errorDescription: translate("errorDescription"),
    retryAction: translate("retryAction"),
  };

  return (
    <PageShell
      eyebrow={translate("eyebrow")}
      title={translate("title")}
      description={translate("description")}
      visual={
        <ProductFrame
          label={translate("visualLabel")}
          status={[
            translate("visualStatusSearch"),
            translate("visualStatusProducts"),
            translate("visualStatusDetail"),
          ]}
        />
      }
    >
      <ProductListView
        lang={lang}
        query={query}
        pageNumber={pageNumber}
        products={products}
        errorMessage={errorMessage}
        copy={copy}
      />
    </PageShell>
  );
}
