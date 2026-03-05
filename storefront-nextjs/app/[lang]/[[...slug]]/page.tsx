import CmsPage from "@/components/cms/CmsPage";
import { fetchSiteConfig, resolvePage } from "@/lib/cms-client";
import { normalizeLanguage } from "@/lib/i18n";
import { buildPageMetadata } from "@/lib/seo";
import { getTranslations } from "next-intl/server";
import { notFound } from "next/navigation";

export async function generateMetadata({
  params,
}: {
  params: Promise<{ lang: string; slug?: string[] }>;
}) {
  const { lang, slug } = await params;
  if (slug?.[0] === "maintenance") {
    const translate = await getTranslations("Maintenance");
    return {
      title: translate("title"),
      description: translate("description"),
    };
  }
  const normalizedLang = normalizeLanguage(lang);
  const isHome = !slug || slug.length === 0;
  const slugPath = isHome ? undefined : `/${slug.join("/")}`;
  const [page, site] = await Promise.all([
    isHome
      ? resolvePage(normalizedLang)
      : resolvePage(normalizedLang, "ContentPage", slugPath),
    fetchSiteConfig(),
  ]);

  if (!page || !site) {
    return {};
  }

  return buildPageMetadata(page, site);
}

export default async function ContentPage({
  params,
}: {
  params: Promise<{ lang: string; slug?: string[] }>;
}) {
  const { lang, slug } = await params;
  if (slug?.[0] === "maintenance") {
    const translate = await getTranslations("Maintenance");
    return (
      <div className="py-24 text-center">
        <h1 className="text-2xl font-semibold">{translate("title")}</h1>
        <p className="mt-2 text-sm text-slate-500">{translate("description")}</p>
      </div>
    );
  }
  const normalizedLang = normalizeLanguage(lang);
  const isHome = !slug || slug.length === 0;
  const slugPath = isHome ? undefined : `/${slug.join("/")}`;

  const page = await (isHome
    ? resolvePage(normalizedLang)
    : resolvePage(normalizedLang, "ContentPage", slugPath));

  if (!page) {
    notFound();
  }

  return <CmsPage page={page} lang={lang} />;
}
