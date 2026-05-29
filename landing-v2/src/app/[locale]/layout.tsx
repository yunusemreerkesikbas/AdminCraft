import type { Metadata } from "next";
import localFont from "next/font/local";
import { NextIntlClientProvider } from "next-intl";
import { getMessages, getTranslations, setRequestLocale } from "next-intl/server";
import { notFound } from "next/navigation";
import { routing } from "@/i18n/routing";
import {
  CONTACT_EMAIL,
  LOGO_PATH,
  SITE_NAME,
  SITE_URL,
  SOCIAL_PROFILES,
} from "@/lib/site";
import { GoogleAnalytics } from "@/components/analytics/GoogleAnalytics";
import { CookieConsent } from "@/components/analytics/CookieConsent";
import "../globals.css";

type Props = { children: React.ReactNode; params: Promise<{ locale: string }> };

const OG_LOCALE: Record<string, string> = { tr: "tr_TR", en: "en_US" };

const stackSans = localFont({
  src: [
    { path: "../../../public/fonts/StackSansHeadline-400.woff2", weight: "400", style: "normal" },
    { path: "../../../public/fonts/StackSansHeadline-500.woff2", weight: "500", style: "normal" },
  ],
  variable: "--font-heading",
  display: "swap",
});

const googleSansFlex = localFont({
  src: [
    { path: "../../../public/fonts/GoogleSansFlex-Variable.woff2", weight: "100 900", style: "normal" },
  ],
  variable: "--font-sans",
  display: "swap",
});

export function generateStaticParams() {
  return routing.locales.map((locale) => ({ locale }));
}

export async function generateMetadata({ params }: Omit<Props, "children">): Promise<Metadata> {
  const { locale } = await params;
  setRequestLocale(locale);
  const t = await getTranslations({ locale, namespace: "metadata" });
  const title = t("title");
  const description = t("description");
  const canonical = `/${locale}`;

  return {
    metadataBase: new URL(SITE_URL),
    title,
    description,
    icons: {
      icon: [{ url: "/brand/favicon-dark.svg", type: "image/svg+xml" }],
      apple: "/brand/favicon-dark.svg",
    },
    alternates: {
      canonical,
      languages: {
        tr: "/tr",
        en: "/en",
        "x-default": `/${routing.defaultLocale}`,
      },
    },
    openGraph: {
      type: "website",
      siteName: SITE_NAME,
      title,
      description,
      url: canonical,
      locale: OG_LOCALE[locale] ?? OG_LOCALE[routing.defaultLocale],
      // og:image is supplied by app/[locale]/opengraph-image.tsx
    },
    twitter: {
      card: "summary_large_image",
      title,
      description,
    },
  };
}

function serializeJsonLd(obj: unknown): string {
  return JSON.stringify(obj).replace(/</g, "\\u003c");
}

type FaqItem = { q: string; a: string };

async function buildJsonLd(locale: string) {
  const t = await getTranslations({ locale, namespace: "faq" });
  const faqItems = t.raw("items") as FaqItem[];

  const organization = {
    "@context": "https://schema.org",
    "@type": "Organization",
    name: SITE_NAME,
    url: SITE_URL,
    logo: `${SITE_URL}${LOGO_PATH}`,
    contactPoint: { "@type": "ContactPoint", email: CONTACT_EMAIL, contactType: "sales" },
    sameAs: [...SOCIAL_PROFILES],
  };

  const website = {
    "@context": "https://schema.org",
    "@type": "WebSite",
    name: SITE_NAME,
    url: `${SITE_URL}/${locale}/`,
    inLanguage: locale,
  };

  const software = {
    "@context": "https://schema.org",
    "@type": "SoftwareApplication",
    name: SITE_NAME,
    applicationCategory: "BusinessApplication",
    operatingSystem: "Web",
  };

  const faq = {
    "@context": "https://schema.org",
    "@type": "FAQPage",
    mainEntity: faqItems.map((item) => ({
      "@type": "Question",
      name: item.q,
      acceptedAnswer: { "@type": "Answer", text: item.a },
    })),
  };

  return [organization, website, software, faq];
}

export default async function LocaleLayout({ children, params }: Props) {
  const { locale } = await params;

  if (!(routing.locales as readonly string[]).includes(locale)) {
    notFound();
  }

  setRequestLocale(locale);
  const messages = await getMessages();
  const jsonLd = await buildJsonLd(locale);

  return (
    <html lang={locale} className={`${stackSans.variable} ${googleSansFlex.variable} antialiased`}>
      <body className="min-h-screen bg-background text-foreground" suppressHydrationWarning>
        <NextIntlClientProvider messages={messages}>
          {jsonLd.map((schema, i) => (
            <script
              key={i}
              type="application/ld+json"
              dangerouslySetInnerHTML={{ __html: serializeJsonLd(schema) }}
            />
          ))}
          {children}
          <CookieConsent />
          <GoogleAnalytics />
        </NextIntlClientProvider>
      </body>
    </html>
  );
}
