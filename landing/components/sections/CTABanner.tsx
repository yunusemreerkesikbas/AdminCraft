"use client";

import { Button } from "@/components/ui/button";
import { AnimateInView } from "@/components/AnimateInView";
import { useDemoContext } from "@/components/layout/DemoContext";
import siteConfig from "@/config/site.json";

type CTAContent = {
  heading: string;
  subheading: string;
  primaryCta: string;
  secondaryCta: string;
};

export function CTABanner({ content }: { content: CTAContent }) {
  const { openContact } = useDemoContext();

  return (
    <section className="relative overflow-hidden px-4 py-24 sm:px-6 lg:px-20">
      <div
        className="pointer-events-none absolute inset-0"
        style={{
          background:
            "linear-gradient(135deg, var(--color-theme-7) 0%, var(--color-theme-6) 100%)",
        }}
        aria-hidden
      />
      <div className="relative mx-auto max-w-[1440px] flex flex-col items-center gap-8 text-center">
        <AnimateInView>
          <h2 className="font-heading max-w-2xl text-3xl font-semibold tracking-tight text-[var(--color-dark-neutral-1)] sm:text-4xl lg:text-5xl">
            {content.heading}
          </h2>
        </AnimateInView>
        <AnimateInView className="animate-in-view-delay-1">
          <p className="max-w-xl text-[var(--color-dark-neutral-2)]">{content.subheading}</p>
        </AnimateInView>
        <AnimateInView className="animate-in-view-delay-2 flex flex-wrap justify-center gap-4">
          <Button
            size="lg"
            onClick={openContact}
            className="bg-[var(--color-theme-3)] text-white hover:opacity-90 hover:scale-[1.02] active:scale-[0.98] transition-all shadow-lg"
          >
            {content.primaryCta}
          </Button>
          <Button
            asChild
            variant="outline"
            size="lg"
            className="border-[var(--color-shade)] bg-white hover:bg-[var(--color-light-neutral-2)]"
          >
            <a href={siteConfig.docsUrl} target="_blank" rel="noopener noreferrer">
              {content.secondaryCta}
            </a>
          </Button>
        </AnimateInView>
      </div>
    </section>
  );
}
