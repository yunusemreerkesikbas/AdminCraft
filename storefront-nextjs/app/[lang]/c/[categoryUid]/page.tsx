import CmsPage from "@/components/cms/CmsPage";
import { fetchProductsByCategory, fetchSiteConfig, resolvePage } from "@/lib/cms-client";
import { normalizeLanguage } from "@/lib/i18n";
import { buildPageMetadata } from "@/lib/seo";
import { notFound } from "next/navigation";

export async function generateMetadata({
  params,
}: {
  params: Promise<{ lang: string; categoryUid: string }>;
}) {
  const { lang, categoryUid } = await params;
  const normalizedLang = normalizeLanguage(lang);
  const [page, site] = await Promise.all([
    resolvePage(normalizedLang, "CategoryPage", undefined, categoryUid),
    fetchSiteConfig(),
  ]);

  if (!page || !site) {
    return {};
  }

  return buildPageMetadata(page, site);
}

export default async function CategoryPage({
  params,
}: {
  params: Promise<{ lang: string; categoryUid: string }>;
}) {
  const { lang, categoryUid } = await params;
  const normalizedLang = normalizeLanguage(lang);
  const [page, products] = await Promise.all([
    resolvePage(normalizedLang, "CategoryPage", undefined, categoryUid),
    fetchProductsByCategory(categoryUid, normalizedLang),
  ]);

  if (!page || !products) {
    notFound();
  }

  return (
    <CmsPage page={page} lang={lang}>
      <section className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {products.content.map((product) => (
          <div key={product.uid} className="rounded border border-slate-200 p-4">
            <h2 className="font-medium">{product.name}</h2>
            <p className="text-xs text-slate-500">SKU: {product.sku}</p>
          </div>
        ))}
      </section>
    </CmsPage>
  );
}
