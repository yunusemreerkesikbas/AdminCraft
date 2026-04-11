import type { Metadata } from "next";
import { Geist } from "next/font/google";
import localFont from "next/font/local";
import "./globals.css";

const geist = Geist({
  variable: "--font-geist",
  subsets: ["latin"],
  display: "swap",
});

const commune = localFont({
  src: "../commune/WOFF/GC commune.woff2",
  variable: "--font-commune",
  display: "swap",
});

export const metadata: Metadata = {
  title: "Craftive — Multi-Tenant Project Platform",
  description:
    "One infrastructure, many projects. From blog to HR portal, agency dashboards to e-commerce — Craftive adapts to each client without rebuilding your backend.",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en" suppressHydrationWarning>
      <body suppressHydrationWarning className={`${geist.variable} ${commune.variable} font-sans antialiased`}>
        {children}
      </body>
    </html>
  );
}
