import { notFound } from "next/navigation";
import trHome from "@/content/home.tr.json";
import enHome from "@/content/home.en.json";
import { Hero } from "@/components/sections/Hero";
import { TechStack } from "@/components/sections/TechStack";
import { Features } from "@/components/sections/Features";
import { Segments } from "@/components/sections/Segments";
import { HowItWorks } from "@/components/sections/HowItWorks";
import { FAQ } from "@/components/sections/FAQ";
import { CTABanner } from "@/components/sections/CTABanner";

const SUPPORTED_LOCALES = ["tr", "en"] as const;
type Locale = (typeof SUPPORTED_LOCALES)[number];

const contentByLocale = {
  tr: trHome,
  en: enHome,
} as const;

type PageProps = { params: Promise<{ locale: string }> };

export default async function LocaleHomePage({ params }: PageProps) {
  const { locale: localeParam } = await params;
  const locale = localeParam as Locale;

  if (!SUPPORTED_LOCALES.includes(locale)) {
    notFound();
  }

  const c = contentByLocale[locale];

  return (
    <div className="flex flex-col">
      <Hero content={c.hero} locale={locale} />
      <TechStack label={c.techStack.label} />
      <Features content={c.features} />
      <Segments content={c.segments} />
      <HowItWorks content={c.howItWorks} />
      <FAQ content={c.faq} />
      <CTABanner content={c.cta} />
    </div>
  );
}

