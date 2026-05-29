import localFont from "next/font/local";
import "./globals.css";

const stackSans = localFont({
  src: [
    { path: "../../public/fonts/StackSansHeadline-400.woff2", weight: "400", style: "normal" },
    { path: "../../public/fonts/StackSansHeadline-500.woff2", weight: "500", style: "normal" },
  ],
  variable: "--font-heading",
  display: "swap",
});

const googleSansFlex = localFont({
  src: [
    { path: "../../public/fonts/GoogleSansFlex-Variable.woff2", weight: "100 900", style: "normal" },
  ],
  variable: "--font-sans",
  display: "swap",
});

export default function RootLayout({
  children,
}: Readonly<{ children: React.ReactNode }>) {
  return (
    <html
      lang="tr"
      className={`${stackSans.variable} ${googleSansFlex.variable} antialiased`}
    >
      <body className="min-h-screen bg-background text-foreground" suppressHydrationWarning>
        {children}
      </body>
    </html>
  );
}
