import { notFound } from "next/navigation";
import { SetLang } from "@/components/SetLang";
import { Navbar } from "@/components/Navbar";
import navTr from "@/i18n/nav.tr.json";
import navEn from "@/i18n/nav.en.json";

const SUPPORTED_LOCALES = ["tr", "en"] as const;
type Locale = (typeof SUPPORTED_LOCALES)[number];

const navByLocale: Record<Locale, { nav: { home: string; features: string; faq: string; docs: string; cta: string } }> = {
  tr: navTr as { nav: { home: string; features: string; faq: string; docs: string; cta: string } },
  en: navEn as { nav: { home: string; features: string; faq: string; docs: string; cta: string } },
};

export function generateStaticParams() {
  return SUPPORTED_LOCALES.map((locale) => ({ locale }));
}

type LayoutProps = {
  children: React.ReactNode;
  params: Promise<{ locale: string }>;
};

export default async function LocaleLayout({ children, params }: LayoutProps) {
  const { locale: localeParam } = await params;
  const locale = localeParam as Locale;

  if (!SUPPORTED_LOCALES.includes(locale)) {
    notFound();
  }

  const labels = navByLocale[locale].nav;

  return (
    <>
      <SetLang locale={locale} />
      <Navbar locale={locale} labels={labels} />
      {children}
    </>
  );
}
