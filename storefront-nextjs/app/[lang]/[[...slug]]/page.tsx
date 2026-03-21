import CmsPage from "@/components/cms/CmsPage";
import { loadContentPage, loadHomepage } from "@/lib/core/cms/loaders";
import { buildOrganizationSchema } from "@/lib/core/seo/schema";
import { buildPageMetadata } from "@/lib/core/seo/metadata";
import { getTranslations } from "next-intl/server";

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
  const isHome = !slug || slug.length === 0;
  const slugPath = isHome ? undefined : `/${slug.join("/")}`;
  const { page, site } = isHome
    ? await loadHomepage(lang)
    : await loadContentPage(lang, slugPath);

  return buildPageMetadata(page, site, isHome ? `/${lang}` : `/${lang}${slugPath}`);
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
  const isHome = !slug || slug.length === 0;
  const slugPath = isHome ? undefined : `/${slug.join("/")}`;

  const { page, site } = isHome
    ? await loadHomepage(lang)
    : await loadContentPage(lang, slugPath);

  const orgSchema = isHome && site ? buildOrganizationSchema(site) : null;

  return (
    <>
      {orgSchema ? (
        <script
          type="application/ld+json"
          dangerouslySetInnerHTML={{ __html: JSON.stringify(orgSchema) }}
        />
      ) : null}
      <CmsPage page={page} lang={lang} />
    </>
  );
}
