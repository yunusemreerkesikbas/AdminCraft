import { Geist, Geist_Mono, Marcellus } from "next/font/google";
import { headers } from "next/headers";
import Script from "next/script";
import { getSiteConfig } from "@/lib/core/cms/client";
import {
  isRtlByConfig,
  isValidLocaleFormat,
  resolveSiteDefaultLocale,
} from "@/lib/core/i18n/locale";
import { buildWebSiteSchema } from "@/lib/core/seo/schema";
import { getGoogleAnalyticsId, getGtmId } from "@/lib/core/config/runtime-env";
import "./globals.css";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

const marcellus = Marcellus({
  variable: "--font-marcellus",
  subsets: ["latin"],
  weight: "400",
});

export default async function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  const requestHeaders = await headers();
  const requestLang = requestHeaders.get("x-lang");
  const preferredLang = requestLang && isValidLocaleFormat(requestLang) ? requestLang : undefined;

  let site = null;

  try {
    site = await getSiteConfig(preferredLang);
  } catch (error) {
    console.warn("[layout] Failed to load site config.", error);
  }

  if (!site) {
    console.warn("[layout] Site config unavailable.");

    if (process.env.NODE_ENV !== "production") {
      return (
        <html lang={preferredLang ?? "en"}>
          <body>
            <div className="sticky top-0 z-[1000] bg-amber-300/95 px-4 py-2 text-center text-xs font-semibold uppercase tracking-[0.18em] text-amber-950">
              CMS unavailable. Check NEXT_PUBLIC_CMS_API_URL and tenant configuration.
            </div>
            {children}
          </body>
        </html>
      );
    }

    return (
      <html lang={preferredLang ?? "en"}>
        <body>
          <p>Service temporarily unavailable.</p>
        </body>
      </html>
    );
  }

  const lang = preferredLang ?? resolveSiteDefaultLocale(site);
  const dir = isRtlByConfig(lang, site.enabledLanguages) ? "rtl" : "ltr";

  const gaId = getGoogleAnalyticsId();
  const gtmId = getGtmId();
  const webSiteSchema = buildWebSiteSchema(site, lang);

  return (
    <html lang={lang} dir={dir}>
      <head>
        {webSiteSchema ? (
          <script
            type="application/ld+json"
            dangerouslySetInnerHTML={{ __html: JSON.stringify(webSiteSchema) }}
          />
        ) : null}
        {gtmId ? (
          <Script id="google-tag-manager" strategy="afterInteractive">{`
            (function(w,d,s,l,i){w[l]=w[l]||[];w[l].push({'gtm.start':
            new Date().getTime(),event:'gtm.js'});var f=d.getElementsByTagName(s)[0],
            j=d.createElement(s),dl=l!='dataLayer'?'&l='+l:'';j.async=true;j.src=
            'https://www.googletagmanager.com/gtm.js?id='+i+dl;f.parentNode.insertBefore(j,f);
            })(window,document,'script','dataLayer','${gtmId}');
          `}</Script>
        ) : null}
        {gaId ? (
          <>
            <Script
              src={`https://www.googletagmanager.com/gtag/js?id=${encodeURIComponent(gaId)}`}
              strategy="afterInteractive"
            />
            <Script id="google-analytics" strategy="afterInteractive">{`
              window.dataLayer = window.dataLayer || [];
              function gtag(){dataLayer.push(arguments);}
              gtag('js', new Date());
              gtag('config', '${gaId}');
            `}</Script>
          </>
        ) : null}
      </head>
      <body suppressHydrationWarning className={`${geistSans.variable} ${geistMono.variable} ${marcellus.variable} antialiased`}>
        {gtmId ? (
          <noscript>
            <iframe
              src={`https://www.googletagmanager.com/ns.html?id=${encodeURIComponent(gtmId)}`}
              height="0"
              width="0"
              style={{ display: "none", visibility: "hidden" }}
            />
          </noscript>
        ) : null}
        {children}
      </body>
    </html>
  );
}
