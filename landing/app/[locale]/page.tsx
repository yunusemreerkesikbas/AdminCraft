import { notFound } from "next/navigation";
import trHome from "@/content/home.tr.json";
import enHome from "@/content/home.en.json";
import { Hero } from "@/components/Hero";
import { TrustedBy } from "@/components/TrustedBy";
import { Benefits } from "@/components/Benefits";
import { Features } from "@/components/Features";
import { Testimonials } from "@/components/Testimonials";

const SUPPORTED_LOCALES = ["tr", "en"] as const;

type Locale = (typeof SUPPORTED_LOCALES)[number];

type HeroContent = {
  headline: string;
  subheadline: string;
  primaryCta: string;
  secondaryCta: string;
};

type TrustedByContent = { title: string };

type BenefitCard = { title: string; description: string };
type BenefitsContent = {
  sectionTag: string;
  heading: string;
  subheading: string;
  cards: BenefitCard[];
};

type FeatureCard = { title: string; description: string; bgClass: string; ctaLabel?: string; ctaUrl?: string };
type FeaturesContent = {
  sectionTag: string;
  heading: string;
  subheading: string;
  cards: FeatureCard[];
};

type TestimonialItem = { name: string; role: string; quote: string };
type TestimonialsContent = {
  sectionTag: string;
  heading: string;
  subheading: string;
  items: TestimonialItem[];
};

type HomeContent = {
  hero: HeroContent;
  trustedBy: TrustedByContent;
  benefits: BenefitsContent;
  features: FeaturesContent;
  testimonials: TestimonialsContent;
};

const contentByLocale: Record<Locale, HomeContent> = {
  tr: trHome as HomeContent,
  en: enHome as HomeContent,
};

type PageProps = {
  params: Promise<{ locale: string }>;
};

export default async function LocaleHomePage({ params }: PageProps) {
  const { locale: localeParam } = await params;
  const locale = localeParam as Locale;

  if (!SUPPORTED_LOCALES.includes(locale)) {
    notFound();
  }

  const content = contentByLocale[locale];

  return (
    <main className="min-h-screen bg-[var(--color-light-neutral-1)] text-[var(--color-dark-neutral-1)]">
      <div className="flex flex-col gap-[140px]">
        <Hero content={content.hero} locale={locale} />
        <TrustedBy title={content.trustedBy.title} />
        <Benefits
        sectionTag={content.benefits.sectionTag}
        heading={content.benefits.heading}
        subheading={content.benefits.subheading}
        cards={content.benefits.cards}
      />
      <Features
        sectionTag={content.features.sectionTag}
        heading={content.features.heading}
        subheading={content.features.subheading}
        cards={content.features.cards}
      />
      <Testimonials
        sectionTag={content.testimonials.sectionTag}
        heading={content.testimonials.heading}
        subheading={content.testimonials.subheading}
        items={content.testimonials.items}
        />
        <section id="faq" className="border-t border-[var(--color-shade)] bg-white px-4 py-16 sm:px-6 lg:px-20">
          <div className="mx-auto max-w-[1440px]">
            <h2 className="font-heading text-2xl font-semibold text-[var(--color-dark-neutral-1)]">
              {locale === "tr" ? "SSS" : "FAQ"}
            </h2>
            <p className="mt-2 text-[var(--color-dark-neutral-2)]">
              {locale === "tr" ? "İçerik yakında eklenecek." : "Content coming soon."}
            </p>
          </div>
        </section>
      </div>
    </main>
  );
}

