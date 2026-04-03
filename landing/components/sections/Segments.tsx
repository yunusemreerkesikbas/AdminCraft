"use client";

import { type ReactNode, useState } from "react";
import { AnimateInView } from "@/components/AnimateInView";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";
import {
  ArrowLeft,
  ArrowRight,
  ArrowUpRight,
  Building2,
  FileText,
  Layout,
  Layers3,
  ShoppingCart,
  Users2,
  type LucideIcon,
} from "lucide-react";

type SegmentStat = {
  label: string;
  value: string;
};

type SegmentUseCase = {
  key: string;
  name: string;
  type: string;
  tabLabel?: string;
  domain: string;
  eyebrow: string;
  summary: string;
  modules: string[];
  stats: SegmentStat[];
};

type SegmentsContent = {
  sectionTag: string;
  heading: string;
  subheading: string;
  labels: {
    domain: string;
    keyMetrics: string;
    enabledModules: string;
    deliveryLayer: string;
    platformModel: string;
    tenantDomain: string;
    isolation: string;
    dedicatedDataSurface: string;
    previousUseCase: string;
    nextUseCase: string;
    goToUseCase: string;
    sharedOperationalCore: string;
    tenantIsolation: string;
    activeModulesSuffix: string;
  };
  useCases: SegmentUseCase[];
};

type UseCaseTheme = {
  color: string;
  surfaceGlow: string;
  Icon: LucideIcon;
};

const USE_CASE_THEMES: Record<string, UseCaseTheme> = {
  "corporate-site": {
    color: "#f97316",
    surfaceGlow: "rgba(249,115,22,0.16)",
    Icon: Building2,
  },
  "content-hub": {
    color: "#f59e0b",
    surfaceGlow: "rgba(245,158,11,0.16)",
    Icon: FileText,
  },
  storefront: {
    color: "#10b981",
    surfaceGlow: "rgba(16,185,129,0.16)",
    Icon: ShoppingCart,
  },
  "campaign-system": {
    color: "#0ea5e9",
    surfaceGlow: "rgba(14,165,233,0.16)",
    Icon: Layout,
  },
  "agency-foundation": {
    color: "#8b8ef8",
    surfaceGlow: "rgba(139,142,248,0.18)",
    Icon: Users2,
  },
};

function getTheme(key: string): UseCaseTheme {
  return USE_CASE_THEMES[key] ?? USE_CASE_THEMES["corporate-site"];
}

function withAlpha(color: string, alpha: string) {
  return `${color}${alpha}`;
}

function getTintedStyles(
  theme: UseCaseTheme,
  {
    backgroundAlpha = "10",
    borderAlpha = "24",
    includeBorder = true,
  }: {
    backgroundAlpha?: string;
    borderAlpha?: string;
    includeBorder?: boolean;
  } = {},
) {
  return {
    background: withAlpha(theme.color, backgroundAlpha),
    ...(includeBorder ? { borderColor: withAlpha(theme.color, borderAlpha) } : {}),
    color: theme.color,
  };
}

function SegmentEyebrow({
  children,
  className,
}: {
  children: ReactNode;
  className?: string;
}) {
  return (
    <p className={cn("text-[11px] font-semibold uppercase tracking-[0.18em] text-neutral-400", className)}>
      {children}
    </p>
  );
}

function SegmentIconTile({
  theme,
  className,
  iconClassName,
  backgroundAlpha,
  borderAlpha,
}: {
  theme: UseCaseTheme;
  className: string;
  iconClassName: string;
  backgroundAlpha?: string;
  borderAlpha?: string;
}) {
  const Icon = theme.Icon;

  return (
    <span
      className={cn("flex shrink-0 items-center justify-center border", className)}
      style={getTintedStyles(theme, { backgroundAlpha, borderAlpha })}
      aria-hidden="true"
    >
      <Icon className={iconClassName} />
    </span>
  );
}

function SegmentStatCard({
  stat,
  theme,
  compact = false,
}: {
  stat: SegmentStat;
  theme: UseCaseTheme;
  compact?: boolean;
}) {
  return (
    <div
      className={cn(
        "rounded-2xl bg-[linear-gradient(180deg,#ffffff_0%,#fafafa_100%)]",
        compact ? "px-3 py-3" : "border border-neutral-200/80 px-4 py-4",
      )}
    >
      <p
        className={cn(
          "font-heading font-bold tracking-tight",
          compact ? "text-xl" : "text-3xl",
        )}
        style={{ color: theme.color }}
      >
        {stat.value}
      </p>
      <p className={cn("mt-1 font-medium text-neutral-500", compact ? "text-[11px]" : "text-xs")}>{stat.label}</p>
    </div>
  );
}

function SegmentModuleTag({ module, theme }: { module: string; theme: UseCaseTheme }) {
  return (
    <span
      className="rounded-full border px-3 py-1.5 text-xs font-semibold"
      style={getTintedStyles(theme, { borderAlpha: "22" })}
    >
      {module}
    </span>
  );
}

function SegmentInfoRow({
  label,
  value,
  monospace = false,
}: {
  label: string;
  value: string;
  monospace?: boolean;
}) {
  return (
    <div className="flex items-center justify-between rounded-2xl border border-neutral-200/80 px-3 py-3">
      <span className="text-sm font-medium text-neutral-600">{label}</span>
      <span className={cn(monospace ? "font-mono text-xs text-neutral-700" : "text-sm font-semibold text-neutral-900")}>
        {value}
      </span>
    </div>
  );
}

function SegmentUseCaseTab({
  useCase,
  isActive,
  onClick,
}: {
  useCase: SegmentUseCase;
  isActive: boolean;
  onClick: () => void;
}) {
  const theme = getTheme(useCase.key);

  return (
    <button
      type="button"
      onClick={onClick}
      className="group inline-flex min-w-fit touch-manipulation items-center gap-3 rounded-[22px] border bg-white px-4 py-3 text-left shadow-[0_18px_35px_-32px_rgba(15,23,42,0.2)] transition-[transform,box-shadow,border-color,background-color] duration-300 hover:-translate-y-0.5 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-neutral-300 motion-reduce:transform-none motion-reduce:transition-none"
      style={{
        borderColor: isActive ? withAlpha(theme.color, "55") : "rgba(229,229,229,0.9)",
        background: isActive
          ? `linear-gradient(180deg, ${withAlpha(theme.color, "10")} 0%, rgba(255,255,255,0.96) 100%)`
          : "#ffffff",
        boxShadow: isActive
          ? `0 28px 50px -38px ${theme.surfaceGlow}`
          : "0 18px 35px -32px rgba(15,23,42,0.2)",
      }}
      aria-pressed={isActive}
    >
      <SegmentIconTile theme={theme} className="h-10 w-10 rounded-[16px]" iconClassName="h-4.5 w-4.5" />
      <span className="min-w-0">
        <span className="block text-sm font-semibold text-neutral-950">{useCase.tabLabel ?? useCase.type}</span>
        <span className="block text-xs text-neutral-500">{useCase.domain}</span>
      </span>
    </button>
  );
}

function SurfacePreview({
  useCase,
  labels,
}: {
  useCase: SegmentUseCase;
  labels: SegmentsContent["labels"];
}) {
  const theme = getTheme(useCase.key);
  const deliveryRows = [
    { label: labels.tenantDomain, value: useCase.domain, monospace: true },
    { label: labels.isolation, value: labels.dedicatedDataSurface },
  ];

  return (
    <div
      className="relative overflow-hidden rounded-[28px] border border-white/80 bg-[linear-gradient(180deg,#fdfdff_0%,#f7f9fc_100%)] p-4 shadow-[0_32px_65px_-46px_rgba(15,23,42,0.3)]"
      style={{
        boxShadow: `0 32px 65px -46px rgba(15,23,42,0.28), 0 0 0 1px ${theme.surfaceGlow} inset`,
      }}
    >
      <div
        className="pointer-events-none absolute inset-0 opacity-[0.24]"
        style={{
          background: `radial-gradient(circle at 18% 12%, ${theme.surfaceGlow} 0%, transparent 42%)`,
        }}
        aria-hidden="true"
      />

      <div className="flex items-center gap-2 border-b border-neutral-200/80 pb-3">
        <div className="flex gap-1.5" aria-hidden="true">
          <div className="h-3 w-3 rounded-full bg-[#ff5f57]" />
          <div className="h-3 w-3 rounded-full bg-[#febb2e]" />
          <div className="h-3 w-3 rounded-full bg-[#28c840]" />
        </div>
        <div className="flex min-w-0 flex-1 items-center gap-2 rounded-full border border-neutral-200 bg-white px-3 py-1.5 text-[11px] text-neutral-500">
          <theme.Icon className="h-3.5 w-3.5 shrink-0" style={{ color: theme.color }} />
          <span className="truncate">{useCase.domain}</span>
        </div>
      </div>

      <div className="mt-4 grid gap-3">
        <div className="grid gap-3 sm:grid-cols-[1.35fr_0.95fr]">
          <div className="rounded-2xl border border-neutral-200/80 bg-white px-4 py-4">
            <div className="flex items-start justify-between gap-3">
              <div>
                <SegmentEyebrow>{useCase.eyebrow}</SegmentEyebrow>
                <h3 className="mt-2 font-heading text-xl font-bold tracking-tight text-neutral-950">
                  {useCase.type}
                </h3>
              </div>
              <SegmentIconTile
                theme={theme}
                className="h-10 w-10 rounded-2xl"
                iconClassName="h-4.5 w-4.5"
                backgroundAlpha="12"
              />
            </div>

            <div className="mt-4 space-y-2.5">
              <div className="h-2.5 w-[74%] rounded-full bg-neutral-900/85" />
              <div className="h-2.5 w-full rounded-full bg-neutral-200" />
              <div className="h-2.5 w-[82%] rounded-full bg-neutral-200" />
            </div>
          </div>

          <div className="rounded-2xl border border-neutral-200/80 bg-white p-4">
            <SegmentEyebrow>{labels.keyMetrics}</SegmentEyebrow>
            <div className="mt-4 grid gap-3">
              {useCase.stats.slice(0, 2).map((stat) => (
                <SegmentStatCard key={stat.label} stat={stat} theme={theme} compact />
              ))}
            </div>
          </div>
        </div>

        <div className="grid gap-3 sm:grid-cols-[1.1fr_1fr]">
          <div className="rounded-2xl border border-neutral-200/80 bg-white px-4 py-4">
            <div className="flex items-center gap-2">
              <Layers3 className="h-3.5 w-3.5" style={{ color: theme.color }} />
              <SegmentEyebrow>{labels.enabledModules}</SegmentEyebrow>
            </div>
            <div className="mt-4 flex flex-wrap gap-2">
              {useCase.modules.map((module) => (
                <SegmentModuleTag key={module} module={module} theme={theme} />
              ))}
            </div>
          </div>

          <div className="rounded-2xl border border-neutral-200/80 bg-white px-4 py-4">
            <div className="flex items-center justify-between gap-3">
              <div>
                <SegmentEyebrow>{labels.deliveryLayer}</SegmentEyebrow>
                <p className="mt-2 text-sm font-semibold text-neutral-900">{useCase.name}</p>
              </div>
              <span
                className="rounded-full px-2.5 py-1 text-[11px] font-semibold"
                style={getTintedStyles(theme, { includeBorder: false })}
              >
                {labels.platformModel}
              </span>
            </div>

            <div className="mt-4 space-y-3">
              {deliveryRows.map((row) => (
                <SegmentInfoRow
                  key={row.label}
                  label={row.label}
                  value={row.value}
                  monospace={row.monospace}
                />
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

function SlideCard({
  useCase,
  labels,
}: {
  useCase: SegmentUseCase;
  labels: SegmentsContent["labels"];
}) {
  const theme = getTheme(useCase.key);

  return (
    <article className="relative overflow-hidden rounded-[34px] border border-neutral-200/80 bg-white p-5 shadow-[0_30px_80px_-50px_rgba(15,23,42,0.32)] sm:p-6 lg:p-8">
      <div
        className="pointer-events-none absolute inset-0 opacity-[0.18]"
        style={{
          background: `radial-gradient(circle at 0% 0%, ${theme.surfaceGlow} 0%, transparent 42%)`,
        }}
        aria-hidden="true"
      />

      <div className="relative grid gap-6 lg:grid-cols-[0.88fr_1.12fr] lg:items-center">
        <div className="flex flex-col gap-6">
          <div className="flex flex-wrap items-start justify-between gap-4">
            <div className="flex items-center gap-3">
              <SegmentIconTile theme={theme} className="h-12 w-12 rounded-[18px]" iconClassName="h-5 w-5" />
              <div>
                <SegmentEyebrow className="tracking-[0.22em]">{useCase.eyebrow}</SegmentEyebrow>
                <h3 className="mt-1 font-heading text-3xl font-bold tracking-tight text-neutral-950">
                  {useCase.type}
                </h3>
              </div>
            </div>

            <span
              className="inline-flex items-center gap-1 rounded-full px-3 py-1.5 text-[11px] font-semibold"
              style={getTintedStyles(theme, { includeBorder: false })}
            >
              {useCase.name}
              <ArrowUpRight className="h-3.5 w-3.5" />
            </span>
          </div>

          <div className="rounded-[24px] border border-neutral-200/80 bg-[linear-gradient(180deg,#fff_0%,#fbfbfd_100%)] p-4">
            <SegmentEyebrow>{labels.domain}</SegmentEyebrow>
            <p className="mt-3 font-mono text-sm text-neutral-700">{useCase.domain}</p>
          </div>

          <p className="max-w-[52ch] text-base leading-8 text-neutral-600">{useCase.summary}</p>

          <div className="grid gap-3 sm:grid-cols-3">
            {useCase.stats.map((stat) => (
              <SegmentStatCard key={stat.label} stat={stat} theme={theme} />
            ))}
          </div>

          <div className="flex flex-wrap gap-2">
            {useCase.modules.map((module) => (
              <SegmentModuleTag key={module} module={module} theme={theme} />
            ))}
          </div>
        </div>

        <SurfacePreview useCase={useCase} labels={labels} />
      </div>
    </article>
  );
}

export function Segments({ content }: { content: SegmentsContent }) {
  const [activeIndex, setActiveIndex] = useState(0);
  const activeUseCase = content.useCases[activeIndex];

  function goTo(index: number) {
    setActiveIndex(index);
  }

  function goPrev() {
    setActiveIndex((current) => (current === 0 ? content.useCases.length - 1 : current - 1));
  }

  function goNext() {
    setActiveIndex((current) => (current === content.useCases.length - 1 ? 0 : current + 1));
  }

  return (
    <section
      id="segments"
      className="relative overflow-hidden bg-[linear-gradient(180deg,#f6f7f9_0%,#f7f8fb_48%,#f4f6fa_100%)] px-4 py-24 sm:px-6 lg:px-20"
    >
      <div
        className="pointer-events-none absolute inset-0 opacity-[0.03]"
        style={{
          backgroundImage: "radial-gradient(circle, #0f172a 1px, transparent 1px)",
          backgroundSize: "28px 28px",
        }}
        aria-hidden="true"
      />

      <div className="relative mx-auto max-w-[1440px]">
        <div className="flex flex-col items-center gap-4 text-center">
          <AnimateInView>
            <Badge
              variant="secondary"
              className="rounded-full border border-[var(--color-shade)] bg-white px-4 py-1 text-xs font-semibold text-[var(--color-dark-neutral-2)]"
            >
              {content.sectionTag}
            </Badge>
          </AnimateInView>
          <AnimateInView className="animate-in-view-delay-1">
            <h2 className="font-heading max-w-2xl text-3xl font-bold tracking-tight text-[var(--color-dark-neutral-1)] sm:text-4xl">
              {content.heading}
            </h2>
          </AnimateInView>
          <AnimateInView className="animate-in-view-delay-2">
            <p className="max-w-2xl text-[var(--color-dark-neutral-2)]">{content.subheading}</p>
          </AnimateInView>
        </div>

        <AnimateInView className="mt-12 animate-in-view-delay-3">
          <div className="flex flex-col gap-5">
            <div className="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
              <div className="flex gap-3 overflow-x-auto pb-1">
                {content.useCases.map((useCase, index) => {
                  return (
                    <SegmentUseCaseTab
                      key={useCase.key}
                      useCase={useCase}
                      isActive={index === activeIndex}
                      onClick={() => goTo(index)}
                    />
                  );
                })}
              </div>

              <div className="flex items-center justify-between gap-3 sm:justify-end">
                <div className="text-sm text-neutral-500">
                  <span className="font-semibold text-neutral-900">
                    {String(activeIndex + 1).padStart(2, "0")}
                  </span>
                  <span className="mx-1 text-neutral-300">/</span>
                  <span>{String(content.useCases.length).padStart(2, "0")}</span>
                </div>
                <div className="flex items-center gap-2">
                  <Button
                    variant="outline"
                    size="icon-sm"
                    className="rounded-full border-neutral-200 bg-white"
                    onClick={goPrev}
                    aria-label={content.labels.previousUseCase}
                  >
                    <ArrowLeft className="h-4 w-4" />
                  </Button>
                  <Button
                    variant="outline"
                    size="icon-sm"
                    className="rounded-full border-neutral-200 bg-white"
                    onClick={goNext}
                    aria-label={content.labels.nextUseCase}
                  >
                    <ArrowRight className="h-4 w-4" />
                  </Button>
                </div>
              </div>
            </div>

            <div className="overflow-hidden rounded-[36px]">
              <div
                className="flex transition-transform duration-500 ease-out motion-reduce:transition-none"
                style={{ transform: `translateX(-${activeIndex * 100}%)` }}
              >
                {content.useCases.map((useCase) => (
                  <div key={useCase.key} className="w-full shrink-0">
                    <SlideCard useCase={useCase} labels={content.labels} />
                  </div>
                ))}
              </div>
            </div>

            <div className="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
              <div className="flex flex-wrap items-center gap-3 rounded-full border border-neutral-200/80 bg-white/80 px-4 py-3 text-sm text-neutral-600 shadow-[0_18px_35px_-30px_rgba(15,23,42,0.2)] backdrop-blur-sm">
                <span className="font-semibold text-neutral-900">{activeUseCase.type}</span>
                <span>{content.labels.sharedOperationalCore}</span>
                <span className="h-1 w-1 rounded-full bg-neutral-300" aria-hidden="true" />
                <span>{content.labels.tenantIsolation}</span>
                <span className="h-1 w-1 rounded-full bg-neutral-300" aria-hidden="true" />
                <span>{`${activeUseCase.modules.length} ${content.labels.activeModulesSuffix}`}</span>
              </div>

              <div className="flex items-center gap-2">
                {content.useCases.map((useCase, index) => {
                  const isActive = index === activeIndex;
                  const theme = getTheme(useCase.key);

                  return (
                    <button
                      key={useCase.key}
                      type="button"
                      onClick={() => goTo(index)}
                      className="h-2.5 touch-manipulation rounded-full transition-[width,background-color] duration-300 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-neutral-300 motion-reduce:transition-none"
                      style={{
                        width: isActive ? "2.5rem" : "0.625rem",
                        backgroundColor: isActive ? theme.color : "rgba(212,212,212,0.9)",
                      }}
                      aria-label={`${content.labels.goToUseCase} ${useCase.type}`}
                      aria-current={isActive}
                    />
                  );
                })}
              </div>
            </div>
          </div>
        </AnimateInView>
      </div>
    </section>
  );
}
