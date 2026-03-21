import { getTranslations } from "next-intl/server";
import CmsPage from "@/components/cms/CmsPage";
import { loadProductPage } from "@/lib/core/cms/loaders";
import { buildPageMetadata } from "@/lib/core/seo/metadata";

export async function generateMetadata({
  params,
}: {
  params: Promise<{ lang: string; uid: string }>;
}) {
  const { lang, uid } = await params;
  const { page, site } = await loadProductPage(lang, uid);

  return buildPageMetadata(page, site, `/${lang}/products/${uid}`);
}

export default async function ProductPage({
  params,
}: {
  params: Promise<{ lang: string; uid: string }>;
}) {
  const { lang, uid } = await params;
  const [translate, { page, product }] = await Promise.all([
    getTranslations("Product"),
    loadProductPage(lang, uid),
  ]);

  return (
    <CmsPage page={page} lang={lang}>
      <section className="rounded border border-slate-200 p-6">
        <h1 className="text-2xl font-semibold">{product.name}</h1>
        <p className="text-sm text-slate-500">{translate("sku")}: {product.sku}</p>
        <p className="mt-4 text-slate-700">{product.shortDescription}</p>
        <p className="mt-4 text-lg font-semibold">{product.price?.formattedValue}</p>
      </section>
    </CmsPage>
  );
}
