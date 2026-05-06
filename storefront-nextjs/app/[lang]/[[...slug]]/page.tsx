import CmsPage from "@/components/cms/CmsPage";
import { loadContentPage, loadHomepage } from "@/lib/core/cms/loaders";
import { buildOrganizationSchema } from "@/lib/core/seo/schema";
import { safeJsonLd } from "@/lib/core/seo/json-ld";
import { buildPageMetadata } from "@/lib/core/seo/metadata";
import { getTranslations } from "next-intl/server";

const readPreviewTicket = (
  raw: string | string[] | undefined,
): string | undefined => {
  if (!raw) return undefined;
  const value = Array.isArray(raw) ? raw[0] : raw;
  return typeof value === "string" && value.length > 0 ? value : undefined;
};

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
  searchParams,
}: {
  params: Promise<{ lang: string; slug?: string[] }>;
  searchParams?: Promise<Record<string, string | string[] | undefined>>;
}) {
  const { lang, slug } = await params;
  const resolvedSearchParams = (await searchParams) ?? {};
  const previewTicket = readPreviewTicket(resolvedSearchParams.preview);

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
    ? await loadHomepage(lang, previewTicket)
    : await loadContentPage(lang, slugPath, previewTicket);

  const orgSchema = isHome && site ? buildOrganizationSchema(site) : null;

  return (
    <>
      {orgSchema ? (
        <script
          type="application/ld+json"
          dangerouslySetInnerHTML={{ __html: safeJsonLd(orgSchema) }}
        />
      ) : null}
      <CmsPage page={page} lang={lang} />
    </>
  );
}
