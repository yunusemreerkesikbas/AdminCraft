"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useCallback, useEffect, useState } from "react";
import { Button } from "@/components/ui/button";
import { Sheet, SheetContent, SheetTrigger } from "@/components/ui/sheet";
import { Menu } from "lucide-react";
import siteConfig from "@/config/site.json";

const PILL_TRANSITION_MS = 320;

type NavLabels = {
  home: string;
  features: string;
  segments: string;
  comparison: string;
  faq: string;
  docs: string;
  cta: string;
  closeMenu: string;
  openMenu: string;
};

type NavbarProps = {
  locale: string;
  labels: NavLabels;
  onDemoOpen: () => void;
};

const NAV_LINKS = (base: string, labels: NavLabels) => [
  { href: `${base}#features`, label: labels.features },
  { href: `${base}#segments`, label: labels.segments },
  { href: `${base}#howitworks`, label: labels.comparison },
  { href: `${base}#faq`, label: labels.faq },
  { href: siteConfig.docsUrl, label: labels.docs, external: true },
];

const navLinkClass =
  "cursor-pointer text-[15px] font-medium text-neutral-500 hover:text-neutral-900 transition-colors duration-200 focus-ring rounded-md px-3 py-2 -mx-1";

export function Navbar({ locale, labels, onDemoOpen }: NavbarProps) {
  const base = `/${locale}`;
  const links = NAV_LINKS(base, labels);
  const router = useRouter();
  const [displayLocale, setDisplayLocale] = useState(locale);

  useEffect(() => {
    setDisplayLocale(locale);
  }, [locale]);

  const handleLangClick = useCallback(
    (e: React.MouseEvent<HTMLAnchorElement>, targetLocale: "tr" | "en") => {
      if (targetLocale === locale) return;
      e.preventDefault();
      setDisplayLocale(targetLocale);
      setTimeout(() => router.push(`/${targetLocale}`), PILL_TRANSITION_MS);
    },
    [locale, router]
  );

  const pillActiveEn = displayLocale === "en";

  return (
    <header className="sticky top-0 z-50 border-b border-neutral-200/80 bg-white/80 backdrop-blur-md supports-[backdrop-filter]:bg-white/90">
      <nav className="mx-auto flex max-w-[1200px] items-center justify-between gap-6 px-4 py-3 sm:px-6 lg:px-12">
        <Link
          href={base}
          className="cursor-pointer font-heading text-[17px] font-semibold tracking-tight text-neutral-900 hover:opacity-80 focus-ring rounded-md shrink-0"
        >
          {siteConfig.brandName}
        </Link>

        <div className="hidden items-center gap-0.5 lg:flex">
          {links.map((l) =>
            l.external ? (
              <a key={l.href} href={l.href} target="_blank" rel="noopener noreferrer" className={navLinkClass}>
                {l.label}
              </a>
            ) : (
              <Link key={l.href} href={l.href} className={navLinkClass}>
                {l.label}
              </Link>
            )
          )}
        </div>

        <div className="flex items-center gap-3">
          <div className="relative grid grid-cols-2 rounded-full bg-neutral-100 p-0.5">
            <div
              className="absolute top-0.5 bottom-0.5 rounded-full bg-neutral-900 transition-transform ease-out"
              style={{
                left: "4px",
                width: "calc(50% - 6px)",
                transform: pillActiveEn ? "translateX(calc(100% + 4px))" : "translateX(0)",
                transitionDuration: `${PILL_TRANSITION_MS}ms`,
              }}
              aria-hidden
            />
            <Link
              href="/tr"
              onClick={(e) => handleLangClick(e, "tr")}
              className={`relative z-10 cursor-pointer rounded-full px-3 py-1.5 text-[13px] font-medium transition-colors duration-200 focus-ring ${
                displayLocale === "tr" ? "text-white" : "text-neutral-600 hover:text-neutral-900"
              }`}
            >
              TR
            </Link>
            <Link
              href="/en"
              onClick={(e) => handleLangClick(e, "en")}
              className={`relative z-10 cursor-pointer rounded-full px-3 py-1.5 text-[13px] font-medium transition-colors duration-200 focus-ring ${
                displayLocale === "en" ? "text-white" : "text-neutral-600 hover:text-neutral-900"
              }`}
            >
              EN
            </Link>
          </div>

          <Button
            onClick={onDemoOpen}
            className="cursor-pointer hidden rounded-full bg-neutral-900 px-5 py-2 text-[14px] font-medium text-white hover:bg-neutral-800 sm:inline-flex"
          >
            {labels.cta}
          </Button>

          <Sheet>
            <SheetTrigger asChild>
              <Button variant="ghost" size="icon" className="cursor-pointer lg:hidden text-neutral-600 hover:text-neutral-900" aria-label={labels.openMenu}>
                <Menu className="h-5 w-5" />
              </Button>
            </SheetTrigger>
            <SheetContent side="right" className="w-72 pt-10">
              <nav className="flex flex-col gap-0.5 px-2">
                {links.map((l) =>
                  l.external ? (
                    <a key={l.href} href={l.href} target="_blank" rel="noopener noreferrer" className={navLinkClass + " block py-2.5"}>
                      {l.label}
                    </a>
                  ) : (
                    <Link key={l.href} href={l.href} className={navLinkClass + " block py-2.5"}>
                      {l.label}
                    </Link>
                  )
                )}
                <div className="mt-6 border-t border-neutral-200 pt-4">
                  <Button
                    onClick={onDemoOpen}
                    className="cursor-pointer w-full rounded-full bg-neutral-900 text-white hover:bg-neutral-800"
                  >
                    {labels.cta}
                  </Button>
                </div>
              </nav>
            </SheetContent>
          </Sheet>
        </div>
      </nav>
    </header>
  );
}
