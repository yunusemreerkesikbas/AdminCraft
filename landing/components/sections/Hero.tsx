"use client";

import { useDemoContext } from "@/components/layout/DemoContext";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import Link from "next/link";

type HeroContent = {
  badge: string;
  headline: string;
  subheadline: string;
  primaryCta: string;
  secondaryCta: string;
  mockupAlt: string;
  visual?: unknown;
};

type HeroProps = { content: HeroContent; locale: string };

export function Hero({ content, locale }: HeroProps) {
  const { openContact } = useDemoContext();
  const base = `/${locale}`;

  return (
    <section className="relative overflow-hidden bg-[var(--color-light-neutral-1)]">
      <div
        className="pointer-events-none absolute inset-0"
        style={{
          background:
            "radial-gradient(ellipse 56% 42% at 50% 24%, rgba(37,99,235,0.08) 0%, rgba(37,99,235,0) 72%), radial-gradient(ellipse 38% 28% at 18% 18%, rgba(244,244,245,0.92) 0%, rgba(244,244,245,0) 100%)",
        }}
        aria-hidden
      />
      <div
        className="pointer-events-none absolute inset-0 opacity-[0.03]"
        style={{
          backgroundImage: "radial-gradient(circle, #000 1px, transparent 1px)",
          backgroundSize: "26px 26px",
        }}
        aria-hidden
      />
      <div
        className="pointer-events-none absolute inset-x-0 bottom-0 h-24"
        style={{
          background:
            "linear-gradient(to bottom, rgba(255,255,255,0) 0%, rgba(247,248,250,0.88) 100%)",
        }}
        aria-hidden
      />

      <div className="relative mx-auto flex w-full max-w-[1440px] items-center justify-center px-4 pb-10 pt-[112px] sm:px-10 sm:pb-12 lg:px-20 lg:pb-14 lg:pt-[138px]">
        <div className="relative z-10 flex w-full max-w-[760px] min-w-0 animate-fade-in-up flex-col items-center gap-6 text-center">
          <Badge
            variant="secondary"
            className="rounded-full border border-neutral-200 bg-white px-4 py-1.5 text-[0.72rem] font-semibold tracking-[-0.01em] text-neutral-600 shadow-[0_1px_0_rgba(255,255,255,0.7),0_8px_20px_-18px_rgba(15,23,42,0.35)]"
          >
            {content.badge}
          </Badge>

          <div className="flex flex-col items-center gap-4">
            <h1 className="heading-1 max-w-[10ch] text-balance text-[var(--color-dark-neutral-1)] lg:text-[4.65rem] lg:leading-[0.98]">
              {content.headline}
            </h1>
            <p className="max-w-[58ch] text-[1rem] leading-[1.75] text-neutral-600 sm:text-[1.05rem]">
              {content.subheadline}
            </p>
          </div>

          <div className="flex flex-wrap items-center justify-center gap-3 animate-fade-in-up animate-fade-in-up-delay-1">
            <Button
              onClick={openContact}
              size="lg"
              className="min-h-11 rounded-[14px] bg-neutral-950 px-7 text-white shadow-[0_18px_30px_-18px_rgba(15,23,42,0.72)] transition-[background-color,box-shadow,transform] duration-200 hover:scale-[1.01] hover:bg-neutral-800 hover:shadow-[0_24px_40px_-22px_rgba(15,23,42,0.72)] active:scale-[0.98] motion-reduce:transform-none motion-reduce:transition-none"
            >
              {content.primaryCta}
            </Button>
            <Button
              asChild
              variant="outline"
              size="lg"
              className="min-h-11 rounded-[14px] border-neutral-200 bg-white px-6 text-neutral-700 shadow-[0_10px_24px_-20px_rgba(15,23,42,0.3)] transition-[background-color,border-color,color,box-shadow] duration-200 hover:border-neutral-300 hover:bg-neutral-50 hover:text-neutral-950 motion-reduce:transition-none"
            >
              <Link href={`${base}#features`}>{content.secondaryCta}</Link>
            </Button>
          </div>
        </div>
      </div>
    </section>
  );
}
