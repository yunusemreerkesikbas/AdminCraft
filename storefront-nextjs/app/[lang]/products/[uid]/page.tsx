import { getTranslations } from "next-intl/server";
import CmsPage from "@/components/cms/CmsPage";
import { fetchProduct, fetchSiteConfig, resolvePage } from "@/lib/cms-client";
import { normalizeLanguage } from "@/lib/i18n";
import { buildPageMetadata } from "@/lib/seo";
import { notFound } from "next/navigation";

export async function generateMetadata({
  params,
}: {
  params: Promise<{ lang: string; uid: string }>;
}) {
  const { lang, uid } = await params;
  const normalizedLang = normalizeLanguage(lang);
  const [page, site] = await Promise.all([
    resolvePage(normalizedLang, "ProductPage", undefined, uid),
    fetchSiteConfig(),
  ]);

  if (!page || !site) {
    return {};
  }

  return buildPageMetadata(page, site);
}

export default async function ProductPage({
  params,
}: {
  params: Promise<{ lang: string; uid: string }>;
}) {
  const { lang, uid } = await params;
  const normalizedLang = normalizeLanguage(lang);
  const translate = await getTranslations("Product");
  const [page, product] = await Promise.all([
    resolvePage(normalizedLang, "ProductPage", undefined, uid),
    fetchProduct(uid, normalizedLang),
  ]);

  if (!page || !product) {
    notFound();
  }

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
