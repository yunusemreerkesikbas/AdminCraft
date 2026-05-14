import { loadShellData } from "@/lib/core/cms/loaders";
import SiteFooter from "@/components/theme/layout/SiteFooter";

export default async function FooterSlot({
  lang,
  previewTicket,
  lifted = false,
}: {
  lang: string;
  previewTicket?: string;
  lifted?: boolean;
}) {
  const { site, shell } = await loadShellData(lang, previewTicket);
  const footer = shell?.footer;
  if (!footer) return null;

  if (!footer.primaryBlocks.length && !footer.bottomBlocks.length) {
    return null;
  }

  return (
    <SiteFooter
      lang={lang}
      brand={site.i18n?.siteName || site.siteName}
      primaryBlocks={footer.primaryBlocks}
      bottomBlocks={footer.bottomBlocks}
      lifted={lifted}
      logoUrl={site.logoUrl}
    />
  );
}
