import { setRequestLocale, getMessages } from "next-intl/server";
import { NextIntlClientProvider } from "next-intl";
import { notFound } from "next/navigation";
import { loadSiteConfig } from "@/lib/core/cms/loaders";
import { isLocaleEnabled, isValidLocaleFormat, requireMessageLocale } from "@/lib/core/i18n/locale";

export default async function LocaleLayout({
  children,
  params,
}: {
  children: React.ReactNode;
  params: Promise<{ lang: string }>;
}) {
  const { lang } = await params;

  if (!isValidLocaleFormat(lang)) {
    notFound();
  }

  const site = await loadSiteConfig(lang);

  if (!isLocaleEnabled(lang, site.enabledLanguages)) {
    notFound();
  }

  setRequestLocale(requireMessageLocale(lang));
  const messages = await getMessages();

  return (
    <NextIntlClientProvider messages={messages}>
      {children}
    </NextIntlClientProvider>
  );
}
