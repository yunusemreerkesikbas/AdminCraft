"use client";

import { Button } from "@/components/ui/button";
import { AnimateInView } from "@/components/AnimateInView";
import { FloatingOrbs } from "@/components/visuals/FloatingOrbs";
import { WaveDivider } from "@/components/visuals/WaveDivider";
import { useDemoContext } from "@/components/layout/DemoContext";
import siteConfig from "@/config/site.json";

type CTAContent = {
  heading: string;
  subheading: string;
  primaryCta: string;
  secondaryCta: string;
};

const CTA_ORBS = [
  {
    className: "absolute -top-10 left-[20%] h-48 w-48 rounded-full bg-blue-400/[0.08] blur-3xl",
    animate: { x: [0, 30, -20, 0], y: [0, -20, 15, 0] },
    duration: 12,
  },
  {
    className: "absolute bottom-0 right-[15%] h-40 w-40 rounded-full bg-indigo-300/[0.07] blur-3xl",
    animate: { x: [0, -25, 15, 0], y: [0, 15, -10, 0] },
    duration: 16,
  },
];

export function CTABanner({ content }: { content: CTAContent }) {
  const { openContact } = useDemoContext();

  return (
    <div className="relative">
      {/* Wave divider at top */}
      <WaveDivider fill="var(--color-theme-7)" className="relative z-10 -mb-px" />

      <section className="relative overflow-hidden px-4 py-24 sm:px-6 lg:px-20">
        {/* Animated gradient background */}
        <div
          className="pointer-events-none absolute inset-0 animate-gradient-shift"
          style={{
            background:
              "linear-gradient(135deg, var(--color-theme-7) 0%, var(--color-theme-6) 50%, var(--color-theme-7) 100%)",
            backgroundSize: "200% 200%",
          }}
          aria-hidden
        />

        <FloatingOrbs orbs={CTA_ORBS} />

        {/* Dot pattern */}
        <div
          className="pointer-events-none absolute inset-0 opacity-[0.02]"
          style={{
            backgroundImage: "radial-gradient(circle, #0f172a 1px, transparent 1px)",
            backgroundSize: "24px 24px",
          }}
          aria-hidden
        />

        <div className="relative mx-auto max-w-[1440px] flex flex-col items-center gap-8 text-center">
          <AnimateInView>
            <h2 className="font-heading max-w-2xl text-3xl font-semibold tracking-tight text-[var(--color-dark-neutral-1)] sm:text-4xl lg:text-5xl">
              {content.heading}
            </h2>
          </AnimateInView>
          <AnimateInView delay={1}>
            <p className="max-w-xl text-[var(--color-dark-neutral-2)]">{content.subheading}</p>
          </AnimateInView>
          <AnimateInView delay={2} className="flex flex-wrap justify-center gap-4">
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
    </div>
  );
}
