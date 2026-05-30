import { CTABanner } from "@/components/sections/CTABanner";
import { FAQ } from "@/components/sections/FAQ";
import { Features } from "@/components/sections/Features";
import { Hero } from "@/components/sections/Hero";
import { HowItWorks } from "@/components/sections/HowItWorks";
import { NewsletterSection } from "@/components/sections/NewsletterSection";
import { TechStack } from "@/components/sections/TechStack";
import enHome from "@/content/home.en.json";
import trHome from "@/content/home.tr.json";
import { notFound } from "next/navigation";

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
      {/* <Segments content={c.segments} /> */}
      <HowItWorks content={c.howItWorks} />
      <FAQ content={c.faq} />
      <NewsletterSection content={c.newsletter} locale={locale} />
      <CTABanner content={c.cta} />
    </div>
  );
}
