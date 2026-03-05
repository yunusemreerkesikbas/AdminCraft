import { redirect } from "next/navigation";
import { fetchTenantConfig } from "@/lib/cms-client";
import { FALLBACK_LOCALE, toUrlLocale } from "@/lib/i18n";

export default async function RootPage() {
  const tenant = await fetchTenantConfig();
  const locale = tenant ? toUrlLocale(tenant.defaultLanguage) : FALLBACK_LOCALE;
  redirect(`/${locale}`);
}
