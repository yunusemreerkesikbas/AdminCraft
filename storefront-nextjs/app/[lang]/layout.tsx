import { redirect } from "next/navigation";
import { setRequestLocale, getMessages } from "next-intl/server";
import { NextIntlClientProvider } from "next-intl";
import { fetchSiteConfig, fetchTenantConfig } from "@/lib/cms-client";
import { toUrlLocale } from "@/lib/i18n";

export default async function LocaleLayout({
  children,
  params,
}: {
  children: React.ReactNode;
  params: Promise<{ lang: string }>;
}) {
  const { lang } = await params;
  const [site, tenant] = await Promise.all([fetchSiteConfig(), fetchTenantConfig()]);

  if (!site) {
    throw new Error("Site config not found");
  }

  // Runtime validasyon: tenant config varsa supportedLanguages'a göre kontrol
  // Tenant config yoksa (401/erişilemiyor) middleware'in format kontrolüne güven — redirect etme
  if (tenant) {
    const supportedCodes = tenant.supportedLanguages.map((l) => toUrlLocale(l.code));
    if (!supportedCodes.includes(lang)) {
      redirect(`/${toUrlLocale(tenant.defaultLanguage)}`);
    }
  }

  setRequestLocale(lang);
  const messages = await getMessages();

  return (
    <NextIntlClientProvider messages={messages}>
      {children}
    </NextIntlClientProvider>
  );
}
