import { Geist, Geist_Mono } from "next/font/google";
import { headers } from "next/headers";
import { fetchSiteConfig } from "@/lib/cms-client";
import { FALLBACK_LOCALE, isRtlByConfig } from "@/lib/i18n";
import "./globals.css";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export default async function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  const requestHeaders = await headers();
  const lang = requestHeaders.get("x-lang") ?? FALLBACK_LOCALE;

  // fetchSiteConfig uses React cache() — deduplicated with [lang]/layout.tsx
  const site = await fetchSiteConfig();
  const dir = site ? (isRtlByConfig(lang, site.enabledLanguages) ? "rtl" : "ltr") : "ltr";

  return (
    <html lang={lang} dir={dir}>
      <body suppressHydrationWarning className={`${geistSans.variable} ${geistMono.variable} antialiased`}>
        {children}
      </body>
    </html>
  );
}
