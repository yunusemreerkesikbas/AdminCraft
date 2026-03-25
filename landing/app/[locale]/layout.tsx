import type { Metadata } from "next";
import { notFound } from "next/navigation";
import { SetLang } from "@/components/SetLang";
import { LocaleShell } from "@/components/layout/LocaleShell";
import navTr from "@/i18n/nav.tr.json";
import navEn from "@/i18n/nav.en.json";
import homeTr from "@/content/home.tr.json";
import homeEn from "@/content/home.en.json";
import { OG_IMAGE_PATH, SITE_URL } from "@/lib/site";

const SUPPORTED_LOCALES = ["tr", "en"] as const;
type Locale = (typeof SUPPORTED_LOCALES)[number];

const navByLocale = {
  tr: navTr.nav,
  en: navEn.nav,
} as const;

const footerByLocale = {
  tr: homeTr.footer,
  en: homeEn.footer,
};

const demoRequestModalByLocale = {
  tr: homeTr.demoRequestModal,
  en: homeEn.demoRequestModal,
} as const;

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
  const canonical = `${SITE_URL}/${locale}`;

  const title = isEN
    ? "Craftive — Multi-Tenant Project Platform"
    : "Craftive — Çok Kiracılı Proje Platformu";
  const description = isEN
    ? "One infrastructure, many projects. From blog to HR portal, agency dashboards to e-commerce — Craftive adapts to each client without rebuilding your backend."
    : "Tek altyapı, birçok proje. Blog sitesinden HR portalına, ajans panellerinden e-ticarete — Craftive her müşteri için altyapıyı baştan kurmadan uyum sağlar.";

  return {
    title,
    description,
    alternates: {
      canonical,
      languages: {
        tr: `${SITE_URL}/tr`,
        en: `${SITE_URL}/en`,
        "x-default": `${SITE_URL}/en`,
      },
    },
    openGraph: {
      title,
      description,
      url: canonical,
      siteName: "Craftive",
      images: [{ url: `${SITE_URL}${OG_IMAGE_PATH}`, width: 1200, height: 630, alt: "Craftive" }],
      locale: isEN ? "en_US" : "tr_TR",
      type: "website",
    },
    twitter: {
      card: "summary_large_image",
      title,
      description,
      images: [`${SITE_URL}${OG_IMAGE_PATH}`],
    },
  };
}

const orgJsonLd = {
  "@context": "https://schema.org",
  "@type": "Organization",
  name: "Craftive",
  url: SITE_URL,
  logo: `${SITE_URL}${OG_IMAGE_PATH}`,
  contactPoint: { "@type": "ContactPoint", email: "hello@craftive.io" },
};

const softwareJsonLd = {
  "@context": "https://schema.org",
  "@type": "SoftwareApplication",
  name: "Craftive",
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
    mainEntity: content.faq.groups.flatMap((group) =>
      group.items.map((item) => ({
        "@type": "Question",
        name: item.question,
        acceptedAnswer: { "@type": "Answer", text: item.answer },
      }))
    ),
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
        demoRequestModalContent={demoRequestModalByLocale[locale]}
      >
        {children}
      </LocaleShell>
    </>
  );
}
