"use client";

import { useDemoContext } from "@/components/layout/DemoContext";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import Image from "next/image";
import Link from "next/link";

type HeroContent = {
  badge: string;
  headline: string;
  subheadline: string;
  primaryCta: string;
  secondaryCta: string;
  mockupAlt: string;
};

type HeroProps = { content: HeroContent; locale: string };

function DashboardMockup({ alt }: { alt: string }) {
  return (
    <div className="relative mx-auto w-full max-w-[1240px] px-1 pb-3 pt-6 sm:px-2 sm:pt-10 lg:px-0 lg:pb-8">
      <div
        className="pointer-events-none absolute inset-x-[8%] top-[10%] h-[72%] rounded-full blur-3xl"
        style={{
          background:
            "radial-gradient(circle, rgba(37,99,235,0.18) 0%, rgba(37,99,235,0.08) 34%, rgba(37,99,235,0) 74%)",
        }}
        aria-hidden
      />
      <div
        className="pointer-events-none absolute inset-x-[14%] bottom-[3%] h-[24%] rounded-full blur-3xl"
        style={{
          background:
            "radial-gradient(circle, rgba(15,23,42,0.15) 0%, rgba(15,23,42,0.05) 40%, rgba(15,23,42,0) 78%)",
        }}
        aria-hidden
      />
      <div
        className="pointer-events-none absolute left-[4%] top-[17%] hidden h-[56%] w-[11%] rounded-[30px] border border-white/28 bg-white/12 backdrop-blur-[18px] lg:block"
        style={{ transform: "rotate(-9deg)" }}
        aria-hidden
      />
      <div
        className="pointer-events-none absolute bottom-[11%] right-[2%] hidden h-[29%] w-[16%] rounded-[28px] border border-white/34 bg-[linear-gradient(180deg,rgba(255,255,255,0.34)_0%,rgba(255,255,255,0.08)_100%)] shadow-[0_22px_38px_-30px_rgba(37,99,235,0.4)] backdrop-blur-[16px] md:block"
        style={{ transform: "rotate(7deg)" }}
        aria-hidden
      />

      <div className="animate-float-subtle relative overflow-hidden rounded-[28px] border border-white/85 bg-white shadow-[0_56px_120px_-52px_rgba(15,23,42,0.48)]">
        <Image
          src="/images/hero.png"
          alt={alt}
          width={1920}
          height={1080}
          sizes="(min-width: 1536px) 760px, (min-width: 1280px) 60vw, (min-width: 768px) 70vw, 96vw"
          className="h-auto w-full object-cover contrast-[1.05] saturate-[1.06]"
          priority
        />
        <div
          className="pointer-events-none absolute inset-0"
          style={{
            boxShadow:
              "inset 0 1px 0 rgba(255,255,255,0.82), inset 0 0 0 1px rgba(148,163,184,0.15), inset 0 -28px 56px rgba(15,23,42,0.04)",
          }}
          aria-hidden
        />
        <div
          className="pointer-events-none absolute inset-y-0 left-0 w-[14%]"
          style={{
            background:
              "linear-gradient(to right, rgba(255,255,255,0.52) 0%, rgba(255,255,255,0.18) 48%, rgba(255,255,255,0) 100%)",
          }}
          aria-hidden
        />
        <div
          className="pointer-events-none absolute inset-y-0 right-0 w-[10%]"
          style={{
            background:
              "linear-gradient(to left, rgba(255,255,255,0.34) 0%, rgba(255,255,255,0.12) 42%, rgba(255,255,255,0) 100%)",
          }}
          aria-hidden
        />
        <div
          className="pointer-events-none absolute inset-x-0 top-0 h-[14%]"
          style={{
            background:
              "linear-gradient(to bottom, rgba(255,255,255,0.26) 0%, rgba(255,255,255,0) 100%)",
          }}
          aria-hidden
        />
      </div>
    </div>
  );
}

export function Hero({ content, locale }: HeroProps) {
  const { openContact } = useDemoContext();
  const base = `/${locale}`;

  return (
    <section className="relative overflow-hidden bg-[var(--color-light-neutral-1)]">
      <div
        className="pointer-events-none absolute inset-0"
        style={{
          background:
            "radial-gradient(ellipse 68% 54% at 78% 30%, rgba(37,99,235,0.08) 0%, rgba(37,99,235,0) 70%), radial-gradient(ellipse 42% 32% at 22% 22%, rgba(244,244,245,0.92) 0%, rgba(244,244,245,0) 100%)",
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

      <div className="relative mx-auto w-full max-w-[1440px] px-4 pb-8 pt-[112px] sm:px-10 lg:px-20 lg:pb-10 lg:pt-[138px]">
        <div className="grid items-center gap-14 lg:grid-cols-[minmax(0,470px)_minmax(0,1.1fr)] lg:gap-8 xl:grid-cols-[minmax(0,500px)_minmax(0,1.16fr)]">
          <div className="relative z-10 flex w-full max-w-[520px] min-w-0 flex-col items-center gap-6 text-center animate-fade-in-up lg:items-start lg:text-left">
            <Badge
              variant="secondary"
              className="rounded-full border border-neutral-200 bg-white px-4 py-1.5 text-[0.72rem] font-semibold tracking-[-0.01em] text-neutral-600 shadow-[0_1px_0_rgba(255,255,255,0.7),0_8px_20px_-18px_rgba(15,23,42,0.35)]"
            >
              {content.badge}
            </Badge>

            <div className="flex flex-col gap-4">
              <h1 className="heading-1 max-w-[9ch] text-balance text-[var(--color-dark-neutral-1)] lg:text-[4.45rem] lg:leading-[0.98]">
                {content.headline}
              </h1>
              <p className="max-w-[50ch] text-[1rem] leading-[1.7] text-neutral-600 sm:text-[1.04rem]">
                {content.subheadline}
              </p>
            </div>

            <div className="flex flex-wrap items-center justify-center gap-3 animate-fade-in-up animate-fade-in-up-delay-1 lg:justify-start">
              <Button
                onClick={openContact}
                size="lg"
                className="min-h-11 rounded-[14px] bg-neutral-950 px-7 text-white shadow-[0_18px_30px_-18px_rgba(15,23,42,0.72)] transition-[background-color,box-shadow,transform] duration-200 hover:bg-neutral-800 hover:shadow-[0_24px_40px_-22px_rgba(15,23,42,0.72)] hover:scale-[1.01] active:scale-[0.98]"
              >
                {content.primaryCta}
              </Button>
              <Button
                asChild
                variant="outline"
                size="lg"
                className="min-h-11 rounded-[14px] border-neutral-200 bg-white px-6 text-neutral-700 shadow-[0_10px_24px_-20px_rgba(15,23,42,0.3)] transition-[background-color,border-color,color,box-shadow] duration-200 hover:border-neutral-300 hover:bg-neutral-50 hover:text-neutral-950"
              >
                <Link href={`${base}#features`}>{content.secondaryCta}</Link>
              </Button>
            </div>
          </div>

          <div className="relative w-full max-w-[1240px] justify-self-end animate-fade-in-up animate-fade-in-up-delay-2 lg:-mr-16 xl:-mr-24 2xl:-mr-28">
            <div
              className="pointer-events-none absolute -inset-x-10 -inset-y-10 rounded-[42px]"
              style={{
                background:
                  "radial-gradient(74% 82% at 46% 50%, rgba(37,99,235,0.22) 0%, rgba(37,99,235,0.08) 40%, rgba(37,99,235,0) 76%)",
              }}
              aria-hidden
            />
            <div
              className="pointer-events-none absolute inset-y-[16%] -left-8 w-24 rounded-full blur-3xl"
              style={{ background: "rgba(255,255,255,0.42)" }}
              aria-hidden
            />
            <div
              className="pointer-events-none absolute right-[8%] top-[8%] h-28 w-28 rounded-full blur-3xl"
              style={{ background: "rgba(59,130,246,0.18)" }}
              aria-hidden
            />
            <DashboardMockup alt={content.mockupAlt} />
          </div>
        </div>
      </div>
    </section>
  );
}
