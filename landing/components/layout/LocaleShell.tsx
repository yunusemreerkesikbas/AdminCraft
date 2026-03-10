"use client";

import { Navbar } from "@/components/layout/Navbar";
import { Footer } from "@/components/layout/Footer";
import { DemoProvider, useDemoContext } from "@/components/layout/DemoContext";

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

type FooterContent = {
  tagline: string;
  links: {
    product: { label: string; items: { label: string; href: string }[] };
    company: { label: string; items: { label: string; href: string }[] };
    resources: { label: string; items: { label: string; href: string }[] };
  };
  copyright: string;
};

type LocaleShellProps = {
  locale: string;
  navLabels: NavLabels;
  footerContent: FooterContent;
  children: React.ReactNode;
};

function ShellInner({ locale, navLabels, footerContent, children }: LocaleShellProps) {
  const { openContact } = useDemoContext();
  return (
    <>
      <Navbar locale={locale} labels={navLabels} onDemoOpen={openContact} />
      <main className="min-h-screen bg-[var(--color-light-neutral-1)] text-[var(--color-dark-neutral-1)]">
        {children}
      </main>
      <Footer content={footerContent} />
    </>
  );
}

export function LocaleShell({ locale, navLabels, footerContent, children }: LocaleShellProps) {
  return (
    <DemoProvider locale={locale}>
      <ShellInner locale={locale} navLabels={navLabels} footerContent={footerContent}>
        {children}
      </ShellInner>
    </DemoProvider>
  );
}
