import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import { headers } from "next/headers";
import {
  FALLBACK_LOCALE,
  isBundledLocale,
} from "@/lib/core/i18n/locale";
import "./globals.css";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: {
    default: "Commerce Storefront",
    template: "%s | Commerce Storefront",
  },
  description: "Commerce storefront shell for Craftive tenants.",
  robots: {
    index: false,
    follow: false,
  },
};

export default async function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  const requestHeaders = await headers();
  const requestLocale = requestHeaders.get("x-next-intl-locale");
  const lang = requestLocale && isBundledLocale(requestLocale)
    ? requestLocale
    : FALLBACK_LOCALE;

  return (
    <html lang={lang}>
      <body className={`${geistSans.variable} ${geistMono.variable}`}>
        {children}
      </body>
    </html>
  );
}
