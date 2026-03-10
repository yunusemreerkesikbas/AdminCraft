"use client";

import Link from "next/link";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { useDemoContext } from "@/components/layout/DemoContext";

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
    <div aria-label={alt} role="img" className="animate-float relative w-full overflow-hidden rounded-2xl border border-[var(--color-shade)] bg-white shadow-[0_24px_80px_-12px_rgba(0,0,0,0.18)]">
      {/* Browser chrome */}
      <div className="flex items-center gap-2 border-b border-[var(--color-shade)] bg-[var(--color-light-neutral-2)] px-4 py-3">
        <div className="flex gap-1.5">
          <div className="h-3 w-3 rounded-full bg-red-400" />
          <div className="h-3 w-3 rounded-full bg-yellow-400" />
          <div className="h-3 w-3 rounded-full bg-green-400" />
        </div>
        <div className="mx-auto flex h-6 w-56 items-center rounded-md bg-white border border-[var(--color-shade)] px-3">
          <div className="h-1.5 w-32 rounded-full bg-[var(--color-shade)]" />
        </div>
      </div>

      {/* App layout */}
      <div className="flex" style={{ minHeight: "340px" }}>
        {/* Sidebar */}
        <div className="flex w-[52px] flex-col gap-3 border-r border-[var(--color-shade)] bg-[var(--color-light-neutral-2)] px-3 py-4 sm:w-44 sm:px-4">
          <div className="mb-2 hidden h-6 w-24 rounded-md bg-[var(--color-dark-neutral-1)] sm:block" />
          {[
            { w: "w-full", active: true },
            { w: "w-4/5", active: false },
            { w: "w-full", active: false },
            { w: "w-3/4", active: false },
            { w: "w-full", active: false },
          ].map((item, i) => (
            <div
              key={i}
              className={`flex items-center gap-2 rounded-lg px-2 py-1.5 ${item.active ? "bg-[var(--color-dark-neutral-1)]" : ""}`}
            >
              <div className={`h-3 w-3 shrink-0 rounded-sm ${item.active ? "bg-white opacity-70" : "bg-[var(--color-shade)]"}`} />
              <div className={`hidden h-2 rounded-full sm:block ${item.active ? "bg-white opacity-50" : "bg-[var(--color-shade)]"} ${item.w}`} />
            </div>
          ))}
        </div>

        {/* Main content */}
        <div className="flex flex-1 flex-col gap-4 p-4 sm:p-5">
          {/* Top bar */}
          <div className="flex items-center justify-between">
            <div className="h-5 w-32 rounded-md bg-[var(--color-shade)]" />
            <div className="flex gap-2">
              <div className="h-7 w-7 rounded-lg bg-[var(--color-light-neutral-2)] border border-[var(--color-shade)]" />
              <div className="h-7 w-16 rounded-lg bg-[var(--color-theme-3)] opacity-80" />
            </div>
          </div>

          {/* Stat cards */}
          <div className="grid grid-cols-3 gap-3">
            {[
              { val: "500+", label: "Tenants", color: "bg-[var(--color-theme-7)]", accent: "bg-[var(--color-theme-3)]" },
              { val: "99.9%", label: "Uptime", color: "bg-[var(--color-theme-6)]", accent: "bg-[var(--color-theme-4)]" },
              { val: "12", label: "Modules", color: "bg-[var(--color-light-neutral-2)]", accent: "bg-[var(--color-dark-neutral-1)]" },
            ].map((s) => (
              <div key={s.label} className={`rounded-xl border border-[var(--color-shade)] ${s.color} p-3`}>
                <div className={`mb-1.5 h-1.5 w-6 rounded-full ${s.accent} opacity-70`} />
                <div className="h-5 w-10 rounded font-bold text-[var(--color-dark-neutral-1)] text-xs flex items-center">
                  <div className={`h-4 w-8 rounded ${s.accent} opacity-80`} />
                </div>
                <div className="mt-1 h-1.5 w-12 rounded bg-[var(--color-shade)]" />
              </div>
            ))}
          </div>

          {/* Table area */}
          <div className="flex-1 overflow-hidden rounded-xl border border-[var(--color-shade)] bg-white">
            <div className="border-b border-[var(--color-shade)] bg-[var(--color-light-neutral-2)] px-4 py-2.5 flex items-center justify-between">
              <div className="h-2.5 w-24 rounded-full bg-[var(--color-shade)]" />
              <div className="flex gap-2">
                <div className="h-5 w-14 rounded-md bg-white border border-[var(--color-shade)]" />
                <div className="h-5 w-14 rounded-md bg-[var(--color-theme-3)] opacity-70" />
              </div>
            </div>
            <div className="divide-y divide-[var(--color-shade)]">
              {[
                { w1: "w-20", w2: "w-24", badge: "bg-green-100", dot: "bg-green-500" },
                { w1: "w-28", w2: "w-16", badge: "bg-[var(--color-theme-6)]", dot: "bg-[var(--color-theme-4)]" },
                { w1: "w-16", w2: "w-20", badge: "bg-[var(--color-theme-7)]", dot: "bg-[var(--color-theme-3)]" },
                { w1: "w-24", w2: "w-18", badge: "bg-green-100", dot: "bg-green-500" },
              ].map((row, i) => (
                <div key={i} className="flex items-center gap-4 px-4 py-2.5">
                  <div className={`h-2 rounded-full bg-[var(--color-shade)] ${row.w1}`} />
                  <div className={`h-2 rounded-full bg-[var(--color-shade)] ${row.w2} flex-1`} />
                  <div className={`flex items-center gap-1 rounded-full px-2 py-0.5 ${row.badge}`}>
                    <div className={`h-1.5 w-1.5 rounded-full ${row.dot}`} />
                    <div className="h-1.5 w-8 rounded-full bg-current opacity-30" />
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export function Hero({ content, locale }: HeroProps) {
  const { openContact } = useDemoContext();
  const base = `/${locale}`;

  return (
    <section className="relative overflow-hidden bg-[var(--color-light-neutral-1)]">
      {/* Gradient bg */}
      <div
        className="pointer-events-none absolute inset-0"
        style={{
          background:
            "radial-gradient(ellipse 90% 60% at 50% -10%, var(--color-theme-6) 0%, transparent 55%)",
        }}
        aria-hidden
      />
      {/* Subtle dot pattern */}
      <div
        className="pointer-events-none absolute inset-0 opacity-[0.025]"
        style={{
          backgroundImage: "radial-gradient(circle, #000 1px, transparent 1px)",
          backgroundSize: "28px 28px",
        }}
        aria-hidden
      />

      <div className="relative mx-auto flex w-full max-w-[1440px] flex-col items-center gap-14 px-4 pb-12 pt-[130px] sm:px-10 lg:px-20">
        {/* Text block */}
        <div className="flex w-full max-w-[680px] flex-col items-center gap-7 text-center animate-fade-in-up">
          <Badge
            variant="secondary"
            className="rounded-full border border-[var(--color-shade)] bg-white/80 px-4 py-1.5 text-xs font-semibold text-[var(--color-dark-neutral-2)] shadow-sm backdrop-blur-sm"
          >
            {content.badge}
          </Badge>

          <div className="flex flex-col gap-5">
            <h1 className="heading-1 text-balance text-[var(--color-dark-neutral-1)]">
              {content.headline}
            </h1>
            <p className="text-base leading-relaxed text-[var(--color-dark-neutral-2)] sm:text-[1.05rem]">
              {content.subheadline}
            </p>
          </div>

          <div className="flex flex-wrap justify-center gap-3 animate-fade-in-up animate-fade-in-up-delay-1">
            <Button
              onClick={openContact}
              size="lg"
              className="rounded-xl bg-[var(--color-theme-3)] px-7 text-white shadow-[0_4px_20px_rgba(37,99,235,0.4)] hover:opacity-90 hover:scale-[1.02] active:scale-[0.98] transition-all duration-200"
            >
              {content.primaryCta}
            </Button>
            <Button
              asChild
              variant="outline"
              size="lg"
              className="rounded-xl border-[var(--color-shade)] bg-white/80 hover:bg-[var(--color-light-neutral-2)]"
            >
              <Link href={`${base}#features`}>{content.secondaryCta}</Link>
            </Button>
          </div>
        </div>

        {/* Dashboard mockup */}
        <div className="w-full max-w-5xl animate-fade-in-up animate-fade-in-up-delay-2">
          <DashboardMockup alt={content.mockupAlt} />
        </div>
      </div>
    </section>
  );
}
