import { loadShellData } from "@/lib/core/cms/loaders";
import SiteHeader from "@/components/theme/layout/SiteHeader";

export default async function HeaderSlot({ lang }: { lang: string }) {
  const { site, shell } = await loadShellData(lang);
  const header = shell?.header;
  if (!header) return null;

  if (!header.mainNavigation && !header.primaryBlocks.length && !header.secondaryBlocks.length) {
    return null;
  }

  return (
    <SiteHeader
      lang={lang}
      brand={site.i18n?.siteName || site.siteName}
      mainNavigation={header.mainNavigation ?? null}
      primaryBlocks={header.primaryBlocks}
      secondaryBlocks={header.secondaryBlocks}
      enabledLanguages={site.enabledLanguages}
      logoUrl={site.logoUrl}
      logoDarkUrl={site.logoDarkUrl}
    />
  );
}
