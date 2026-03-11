import type { Metadata } from "next";
import { notFound } from "next/navigation";
import { SetLang } from "@/components/SetLang";
import { LocaleShell } from "@/components/layout/LocaleShell";
import navTr from "@/i18n/nav.tr.json";
import navEn from "@/i18n/nav.en.json";
import homeTr from "@/content/home.tr.json";
import homeEn from "@/content/home.en.json";

const SUPPORTED_LOCALES = ["tr", "en"] as const;
type Locale = (typeof SUPPORTED_LOCALES)[number];

const BASE_URL = "https://www.craftive.io";

const navByLocale = {
  tr: navTr.nav,
  en: navEn.nav,
} as const;

const footerByLocale = {
  tr: homeTr.footer,
  en: homeEn.footer,
};

export function generateStaticParams() {
  return SUPPORTED_LOCALES.map((locale) => ({ locale }));
}

type LayoutProps = {
  children: React.ReactNode;
  params: Promise<{ locale: string }>;
};

export async function generateMetadata({ params }: { params: Promise<{ locale: string }> }): Promise<Metadata> {
  const { locale } = await params;
  const isEN = locale === "en";
  const canonical = `${BASE_URL}/${locale}`;

  const title = isEN
    ? "AdminCraft — Multi-Tenant SaaS Platform"
    : "AdminCraft — Çok Kiracılı SaaS Platformu";
  const description = isEN
    ? "One infrastructure, unlimited configurations. From blog to HR portal, agency to e-commerce — AdminCraft configures to any business."
    : "Tek altyapı, sınırsız yapılandırma. Blog sitesinden HR portalına, ajans yönetiminden e-ticarete — her işletme için AdminCraft.";

  return {
    title,
    description,
    alternates: {
      canonical,
      languages: {
        tr: `${BASE_URL}/tr`,
        en: `${BASE_URL}/en`,
        "x-default": `${BASE_URL}/en`,
      },
    },
    openGraph: {
      title,
      description,
      url: canonical,
      siteName: "AdminCraft",
      images: [{ url: `${BASE_URL}/images/og-image.png`, width: 1200, height: 630, alt: "AdminCraft" }],
      locale: isEN ? "en_US" : "tr_TR",
      type: "website",
    },
    twitter: {
      card: "summary_large_image",
      title,
      description,
      images: [`${BASE_URL}/images/og-image.png`],
    },
  };
}

const orgJsonLd = {
  "@context": "https://schema.org",
  "@type": "Organization",
  name: "AdminCraft",
  url: BASE_URL,
  logo: `${BASE_URL}/images/og-image.png`,
  contactPoint: { "@type": "ContactPoint", email: "hello@craftive.io" },
};

const softwareJsonLd = {
  "@context": "https://schema.org",
  "@type": "SoftwareApplication",
  name: "AdminCraft",
  applicationCategory: "BusinessApplication",
  operatingSystem: "Web",
  offers: { "@type": "Offer", price: "1500", priceCurrency: "TRY" },
};

function serializeJsonLd(obj: unknown): string {
  return JSON.stringify(obj).replace(/</g, "\\u003c");
}

function buildFaqJsonLd(locale: Locale) {
  const content = locale === "tr" ? homeTr : homeEn;
  return {
    "@context": "https://schema.org",
    "@type": "FAQPage",
    mainEntity: content.faq.items.map((item) => ({
      "@type": "Question",
      name: item.question,
      acceptedAnswer: { "@type": "Answer", text: item.answer },
    })),
  };
}

export default async function LocaleLayout({ children, params }: LayoutProps) {
  const { locale: localeParam } = await params;
  const locale = localeParam as Locale;

  if (!SUPPORTED_LOCALES.includes(locale)) {
    notFound();
  }

  const faqJsonLd = buildFaqJsonLd(locale);

  return (
    <>
      <SetLang locale={locale} />
      <script
        type="application/ld+json"
        dangerouslySetInnerHTML={{ __html: serializeJsonLd(orgJsonLd) }}
      />
      <script
        type="application/ld+json"
        dangerouslySetInnerHTML={{ __html: serializeJsonLd(softwareJsonLd) }}
      />
      <script
        type="application/ld+json"
        dangerouslySetInnerHTML={{ __html: serializeJsonLd(faqJsonLd) }}
      />
      <LocaleShell
        locale={locale}
        navLabels={navByLocale[locale]}
        footerContent={footerByLocale[locale]}
      >
        {children}
      </LocaleShell>
    </>
  );
}
