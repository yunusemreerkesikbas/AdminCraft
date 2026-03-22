import { redirect } from "next/navigation";
import { loadSiteConfig } from "@/lib/core/cms/loaders";
import { resolveSiteDefaultLocale } from "@/lib/core/i18n/locale";

export default async function RootPage() {
  const site = await loadSiteConfig();
  const locale = resolveSiteDefaultLocale(site);
  redirect(`/${locale}`);
}
