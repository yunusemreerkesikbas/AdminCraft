import CmsPage from "@/components/cms/CmsPage";
import { loadCategoryPage } from "@/lib/core/cms/loaders";
import { buildPageMetadata } from "@/lib/core/seo/metadata";

export async function generateMetadata({
  params,
}: {
  params: Promise<{ lang: string; categoryUid: string }>;
}) {
  const { lang, categoryUid } = await params;
  const { page, site } = await loadCategoryPage(lang, categoryUid);

  return buildPageMetadata(page, site, `/${lang}/c/${categoryUid}`);
}

export default async function CategoryPage({
  params,
}: {
  params: Promise<{ lang: string; categoryUid: string }>;
}) {
  const { lang, categoryUid } = await params;
  const { page, products } = await loadCategoryPage(lang, categoryUid);

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
