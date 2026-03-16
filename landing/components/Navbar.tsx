"use client";

import Link from "next/link";
import { useState } from "react";
import siteConfig from "@/config/site.json";

type NavLabels = {
  home: string;
  features: string;
  faq: string;
  docs: string;
  cta: string;
};

type NavbarProps = {
  locale: string;
  labels: NavLabels;
};

const navLinkClass =
  "text-sm font-medium text-[var(--color-dark-neutral-2)] hover:text-[var(--color-dark-neutral-1)] transition-colors focus-ring rounded px-2 py-1";

export function Navbar({ locale, labels }: NavbarProps) {
  const base = `/${locale}`;
  const [mobileOpen, setMobileOpen] = useState(false);

  return (
    <header className="sticky top-0 z-50 border-b border-[var(--color-shade)] bg-[var(--color-light-neutral-1)]/95 backdrop-blur supports-[backdrop-filter]:bg-white/80">
      <nav className="mx-auto flex max-w-[1440px] items-center justify-between gap-4 px-4 py-4 sm:px-6 lg:px-20">
        <Link
          href={base}
          className="font-heading text-xl font-semibold text-[var(--color-dark-neutral-1)] hover:opacity-80 focus-ring rounded"
        >
          {siteConfig.brandName}
        </Link>

        <div className="flex items-center gap-6">
          <Link href={base} className={`hidden ${navLinkClass} sm:block`}>
            {labels.home}
          </Link>
          <Link href={`${base}#features`} className={`hidden ${navLinkClass} sm:block`} onClick={() => setMobileOpen(false)}>
            {labels.features}
          </Link>
          <Link href={`${base}#faq`} className={`hidden ${navLinkClass} sm:block`} onClick={() => setMobileOpen(false)}>
            {labels.faq}
          </Link>
          <a
            href={siteConfig.docsUrl}
            target="_blank"
            rel="noopener noreferrer"
            className={`hidden ${navLinkClass} sm:block`}
          >
            {labels.docs}
          </a>

          <div className="flex items-center gap-2">
            <Link
              href="/tr"
              className={`rounded px-2 py-1 text-sm font-medium transition-colors focus-ring ${locale === "tr" ? "bg-[var(--color-shade)] text-[var(--color-dark-neutral-1)]" : "text-[var(--color-dark-neutral-2)] hover:text-[var(--color-dark-neutral-1)]"}`}
            >
              TR
            </Link>
            <Link
              href="/en"
              className={`rounded px-2 py-1 text-sm font-medium transition-colors focus-ring ${locale === "en" ? "bg-[var(--color-shade)] text-[var(--color-dark-neutral-1)]" : "text-[var(--color-dark-neutral-2)] hover:text-[var(--color-dark-neutral-1)]"}`}
            >
              EN
            </Link>
          </div>

          <a
            href={`mailto:${siteConfig.contactEmail}?subject=Demo%20Talebi`}
            className="btn-primary hidden sm:inline-flex"
          >
            {labels.cta}
          </a>

          <button
            type="button"
            aria-expanded={mobileOpen}
            aria-controls="mobile-nav"
            aria-label={mobileOpen ? (locale === "tr" ? "Menüyü kapat" : "Close menu") : (locale === "tr" ? "Menüyü aç" : "Open menu")}
            className="flex flex-col gap-1.5 p-2 sm:hidden focus-ring rounded-lg"
            onClick={() => setMobileOpen((o) => !o)}
          >
            <span className={`h-0.5 w-6 bg-[var(--color-dark-neutral-1)] transition-transform ${mobileOpen ? "translate-y-2 rotate-45" : ""}`} />
            <span className={`h-0.5 w-6 bg-[var(--color-dark-neutral-1)] transition-opacity ${mobileOpen ? "opacity-0" : ""}`} />
            <span className={`h-0.5 w-6 bg-[var(--color-dark-neutral-1)] transition-transform ${mobileOpen ? "-translate-y-2 -rotate-45" : ""}`} />
          </button>
        </div>
      </nav>

      <div
        id="mobile-nav"
        className={`overflow-hidden border-t border-[var(--color-shade)] bg-[var(--color-light-neutral-1)] transition-[max-height] duration-300 ease-out sm:hidden ${mobileOpen ? "max-h-[280px]" : "max-h-0"}`}
      >
        <div className="flex flex-col gap-1 px-4 py-4">
          <Link href={base} className={navLinkClass} onClick={() => setMobileOpen(false)}>
            {labels.home}
          </Link>
          <Link href={`${base}#features`} className={navLinkClass} onClick={() => setMobileOpen(false)}>
            {labels.features}
          </Link>
          <Link href={`${base}#faq`} className={navLinkClass} onClick={() => setMobileOpen(false)}>
            {labels.faq}
          </Link>
          <a href={siteConfig.docsUrl} target="_blank" rel="noopener noreferrer" className={navLinkClass}>
            {labels.docs}
          </a>
          <a
            href={`mailto:${siteConfig.contactEmail}?subject=Demo%20Talebi`}
            className="btn-primary mt-2 sm:hidden"
            onClick={() => setMobileOpen(false)}
          >
            {labels.cta}
          </a>
        </div>
      </div>
    </header>
  );
}
