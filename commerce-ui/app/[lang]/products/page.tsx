import { getTranslations } from "next-intl/server";
import {
  ProductListView,
} from "@/components/product/ProductListView";
import { createProductListModel } from "@/components/product/product-list-model";
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

  const model = createProductListModel(translate);

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
        model={model}
      />
    </PageShell>
  );
}
