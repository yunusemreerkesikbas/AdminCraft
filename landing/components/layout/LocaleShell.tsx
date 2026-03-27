"use client";

import type { DemoRequestModalContent } from "@/content/home";
import { Navbar } from "@/components/layout/Navbar";
import { Footer } from "@/components/layout/Footer";
import { DemoProvider, useDemoContext } from "@/components/layout/DemoContext";

type NavLabels = {
  home: string;
  features: string;
  segments: string;
  howItWorks: string;
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
    company?: { label: string; items: { label: string; href: string }[] };
    resources: { label: string; items: { label: string; href: string }[] };
  };
  copyright: string;
};

type LocaleShellProps = {
  locale: string;
  navLabels: NavLabels;
  footerContent: FooterContent;
  demoRequestModalContent: DemoRequestModalContent;
  children: React.ReactNode;
};

function ShellInner({
  locale,
  navLabels,
  footerContent,
  children,
}: Omit<LocaleShellProps, "demoRequestModalContent">) {
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

export function LocaleShell({
  locale,
  navLabels,
  footerContent,
  demoRequestModalContent,
  children,
}: LocaleShellProps) {
  return (
    <DemoProvider locale={locale} content={demoRequestModalContent}>
      <ShellInner locale={locale} navLabels={navLabels} footerContent={footerContent}>
        {children}
      </ShellInner>
    </DemoProvider>
  );
}
