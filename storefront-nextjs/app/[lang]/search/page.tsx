import { getTranslations } from "next-intl/server";
import CmsPage from "@/components/cms/CmsPage";
import { loadSearchPage } from "@/lib/core/cms/loaders";
import { buildPageMetadata } from "@/lib/core/seo/metadata";

export async function generateMetadata({
  params,
}: {
  params: Promise<{ lang: string }>;
}) {
  const { lang } = await params;
  const { page, site } = await loadSearchPage(lang);

  return buildPageMetadata(page, site, `/${lang}/search`);
}

export default async function SearchPage({
  params,
  searchParams,
}: {
  params: Promise<{ lang: string }>;
  searchParams?: Promise<{ q?: string }>;
}) {
  const { lang } = await params;
  const resolvedSearchParams = searchParams ? await searchParams : {};
  const [translate, { page, query, results }] = await Promise.all([
    getTranslations("Search"),
    loadSearchPage(lang, resolvedSearchParams.q),
  ]);

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
