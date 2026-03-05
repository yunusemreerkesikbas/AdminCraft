import { getTranslations } from "next-intl/server";
import CmsPage from "@/components/cms/CmsPage";
import { fetchSiteConfig, resolvePage, searchProducts } from "@/lib/cms-client";
import { normalizeLanguage } from "@/lib/i18n";
import { buildPageMetadata } from "@/lib/seo";
import { PageResponse, ProductListDeliveryResponse } from "@/lib/types";
import { notFound } from "next/navigation";

const emptyResults: PageResponse<ProductListDeliveryResponse> = {
  content: [],
  totalElements: 0,
  totalPages: 0,
  size: 0,
  number: 0,
  numberOfElements: 0,
  first: true,
  last: true,
};

export async function generateMetadata({
  params,
}: {
  params: Promise<{ lang: string }>;
}) {
  const { lang } = await params;
  const normalizedLang = normalizeLanguage(lang);
  const [page, site] = await Promise.all([
    resolvePage(normalizedLang, "SearchResultPage"),
    fetchSiteConfig(),
  ]);

  if (!page || !site) {
    return {};
  }

  return buildPageMetadata(page, site);
}

export default async function SearchPage({
  params,
  searchParams,
}: {
  params: Promise<{ lang: string }>;
  searchParams?: Promise<{ q?: string }>;
}) {
  const { lang } = await params;
  const normalizedLang = normalizeLanguage(lang);
  const translate = await getTranslations("Search");
  const page = await resolvePage(normalizedLang, "SearchResultPage");

  if (!page) {
    notFound();
  }

  const resolvedSearchParams = searchParams ? await searchParams : {};
  const query = resolvedSearchParams.q?.trim() ?? "";
  const results = query ? await searchProducts(query, normalizedLang) : emptyResults;

  return (
    <CmsPage page={page} lang={lang}>
      <section className="rounded border border-slate-200 p-6">
        <h2 className="text-lg font-semibold">{translate("title")}</h2>
        {query.length === 0 ? (
          <p className="mt-2 text-sm text-slate-500">{translate("emptyQuery")}</p>
        ) : null}
        <div className="mt-4 space-y-3">
          {(results?.content ?? []).map((product) => (
            <div key={product.uid} className="rounded border border-slate-100 p-3">
              <h3 className="font-medium">{product.name}</h3>
              <p className="text-xs text-slate-500">SKU: {product.sku}</p>
            </div>
          ))}
        </div>
      </section>
    </CmsPage>
  );
}
