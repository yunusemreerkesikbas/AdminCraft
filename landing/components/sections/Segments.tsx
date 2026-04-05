"use client";

import { type ReactNode, useCallback, useEffect, useState } from "react";
import { AnimateInView } from "@/components/AnimateInView";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";
import useEmblaCarousel from "embla-carousel-react";
import Autoplay from "embla-carousel-autoplay";
import { motion, AnimatePresence, useReducedMotion } from "framer-motion";
import {
  ArrowLeft,
  ArrowRight,
  ArrowUpRight,
  Building2,
  FileText,
  Layout,
  ShoppingCart,
  Users2,
  type LucideIcon,
} from "lucide-react";

type SegmentStat = { label: string; value: string };
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
type UseCaseTheme = { color: string; surfaceGlow: string; Icon: LucideIcon };

const USE_CASE_THEMES: Record<string, UseCaseTheme> = {
  "corporate-site": { color: "#f97316", surfaceGlow: "rgba(249,115,22,0.16)", Icon: Building2 },
  "content-hub": { color: "#f59e0b", surfaceGlow: "rgba(245,158,11,0.16)", Icon: FileText },
  storefront: { color: "#10b981", surfaceGlow: "rgba(16,185,129,0.16)", Icon: ShoppingCart },
  "campaign-system": { color: "#0ea5e9", surfaceGlow: "rgba(14,165,233,0.16)", Icon: Layout },
  "agency-foundation": { color: "#8b8ef8", surfaceGlow: "rgba(139,142,248,0.18)", Icon: Users2 },
};

function getTheme(key: string): UseCaseTheme {
  return USE_CASE_THEMES[key] ?? USE_CASE_THEMES["corporate-site"];
}
function withAlpha(color: string, alpha: string) {
  return `${color}${alpha}`;
}
function getTintedStyles(
  theme: UseCaseTheme,
  { backgroundAlpha = "10", borderAlpha = "24", includeBorder = true }: { backgroundAlpha?: string; borderAlpha?: string; includeBorder?: boolean } = {},
) {
  return {
    background: withAlpha(theme.color, backgroundAlpha),
    ...(includeBorder ? { borderColor: withAlpha(theme.color, borderAlpha) } : {}),
    color: theme.color,
  };
}

function SegmentEyebrow({ children, className }: { children: ReactNode; className?: string }) {
  return <p className={cn("text-[11px] font-semibold uppercase tracking-[0.18em] text-neutral-400", className)}>{children}</p>;
}

function SegmentIconTile({ theme, className, iconClassName, backgroundAlpha, borderAlpha }: { theme: UseCaseTheme; className: string; iconClassName: string; backgroundAlpha?: string; borderAlpha?: string }) {
  const Icon = theme.Icon;
  return (
    <span className={cn("flex shrink-0 items-center justify-center border", className)} style={getTintedStyles(theme, { backgroundAlpha, borderAlpha })} aria-hidden="true">
      <Icon className={iconClassName} />
    </span>
  );
}


function SegmentModuleTag({ module, theme }: { module: string; theme: UseCaseTheme }) {
  return <span className="rounded-full border px-3 py-1.5 text-xs font-semibold" style={getTintedStyles(theme, { borderAlpha: "22" })}>{module}</span>;
}


function SegmentUseCaseTab({ useCase, isActive, onClick }: { useCase: SegmentUseCase; isActive: boolean; onClick: () => void }) {
  const theme = getTheme(useCase.key);
  return (
    <button
      type="button"
      onClick={onClick}
      className="group inline-flex min-w-fit touch-manipulation items-center gap-3 rounded-[22px] border bg-white px-4 py-3 text-left shadow-[0_18px_35px_-32px_rgba(15,23,42,0.2)] transition-[transform,box-shadow,border-color,background-color] duration-300 hover:-translate-y-0.5 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-neutral-300 motion-reduce:transform-none motion-reduce:transition-none"
      style={{
        borderColor: isActive ? withAlpha(theme.color, "55") : "rgba(229,229,229,0.9)",
        background: isActive ? `linear-gradient(180deg, ${withAlpha(theme.color, "10")} 0%, rgba(255,255,255,0.96) 100%)` : "#ffffff",
        boxShadow: isActive ? `0 28px 50px -38px ${theme.surfaceGlow}` : "0 18px 35px -32px rgba(15,23,42,0.2)",
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

function SurfacePreview({ useCase }: { useCase: SegmentUseCase }) {
  const theme = getTheme(useCase.key);

  return (
    <div
      className="relative overflow-hidden rounded-[28px] border border-white/80 bg-[linear-gradient(180deg,#fdfdff_0%,#f7f9fc_100%)] p-4 shadow-[0_32px_65px_-46px_rgba(15,23,42,0.3)]"
      style={{ boxShadow: `0 32px 65px -46px rgba(15,23,42,0.28), 0 0 0 1px ${theme.surfaceGlow} inset` }}
    >
      <div className="pointer-events-none absolute inset-0 opacity-[0.24]" style={{ background: `radial-gradient(circle at 18% 12%, ${theme.surfaceGlow} 0%, transparent 42%)` }} aria-hidden="true" />

      {/* Browser chrome */}
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

      {/* Screenshot placeholder */}
      <div className="relative mt-4 overflow-hidden rounded-2xl border border-neutral-200/80 bg-neutral-50">
        <div className="flex min-h-[240px] items-center justify-center text-xs text-neutral-400 select-none">
          [ görsel: {useCase.key} dashboard ekranı ]
        </div>
        <div
          className="pointer-events-none absolute inset-x-0 bottom-0 h-16"
          style={{ background: "linear-gradient(to bottom, rgba(250,250,250,0) 0%, rgba(250,250,250,0.95) 100%)" }}
          aria-hidden="true"
        />
      </div>
    </div>
  );
}

function SlideCard({ useCase, labels }: { useCase: SegmentUseCase; labels: SegmentsContent["labels"] }) {
  const theme = getTheme(useCase.key);
  return (
    <article className="relative overflow-hidden rounded-[34px] border border-neutral-200/80 bg-white p-5 shadow-[0_30px_80px_-50px_rgba(15,23,42,0.32)] sm:p-6 lg:p-8">
      <div className="pointer-events-none absolute inset-0 opacity-[0.18]" style={{ background: `radial-gradient(circle at 0% 0%, ${theme.surfaceGlow} 0%, transparent 42%)` }} aria-hidden="true" />
      <div className="relative grid gap-6 lg:grid-cols-[0.88fr_1.12fr] lg:items-center">
        <div className="flex flex-col gap-6">
          <div className="flex flex-wrap items-start justify-between gap-4">
            <div className="flex items-center gap-3">
              <SegmentIconTile theme={theme} className="h-12 w-12 rounded-[18px]" iconClassName="h-5 w-5" />
              <div>
                <SegmentEyebrow className="tracking-[0.22em]">{useCase.eyebrow}</SegmentEyebrow>
                <h3 className="mt-1 font-heading text-3xl font-bold tracking-tight text-neutral-950">{useCase.type}</h3>
              </div>
            </div>
            <span className="inline-flex items-center gap-1 rounded-full px-3 py-1.5 text-[11px] font-semibold" style={getTintedStyles(theme, { includeBorder: false })}>
              {useCase.name}
              <ArrowUpRight className="h-3.5 w-3.5" />
            </span>
          </div>
          <p className="max-w-[52ch] text-base leading-8 text-neutral-600">{useCase.summary}</p>
          <div className="flex flex-wrap gap-8">
            {useCase.stats.map((stat) => (
              <div key={stat.label}>
                <p className="font-heading text-2xl font-bold tracking-tight" style={{ color: theme.color }}>{stat.value}</p>
                <p className="mt-0.5 text-xs font-medium text-neutral-500">{stat.label}</p>
              </div>
            ))}
          </div>
          <div className="flex flex-wrap gap-2">
            {useCase.modules.map((module) => (
              <SegmentModuleTag key={module} module={module} theme={theme} />
            ))}
          </div>
        </div>
        <SurfacePreview useCase={useCase} />
      </div>
    </article>
  );
}

export function Segments({ content }: { content: SegmentsContent }) {
  const shouldReduce = useReducedMotion();
  const [activeIndex, setActiveIndex] = useState(0);
  const [progress, setProgress] = useState(0);

  const autoplayPlugin = Autoplay({ delay: 6000, stopOnInteraction: true, stopOnMouseEnter: true });

  const [emblaRef, emblaApi] = useEmblaCarousel(
    { loop: true, align: "start", skipSnaps: false },
    [autoplayPlugin],
  );

  const onSelect = useCallback(() => {
    if (!emblaApi) return;
    setActiveIndex(emblaApi.selectedScrollSnap());
    setProgress(0);
  }, [emblaApi]);

  useEffect(() => {
    if (!emblaApi) return;
    emblaApi.on("select", onSelect);
    onSelect();
    return () => { emblaApi.off("select", onSelect); };
  }, [emblaApi, onSelect]);

  // Progress bar animation
  useEffect(() => {
    if (shouldReduce) return;
    const interval = setInterval(() => {
      setProgress((p) => (p >= 100 ? 100 : p + 100 / 60));
    }, 100);
    return () => clearInterval(interval);
  }, [activeIndex, shouldReduce]);

  function goTo(index: number) {
    emblaApi?.scrollTo(index);
  }
  function goPrev() {
    emblaApi?.scrollPrev();
  }
  function goNext() {
    emblaApi?.scrollNext();
  }

  const activeUseCase = content.useCases[activeIndex];

  return (
    <section
      id="segments"
      className="relative overflow-hidden bg-[linear-gradient(180deg,#f6f7f9_0%,#f7f8fb_48%,#f4f6fa_100%)] px-4 py-24 sm:px-6 lg:px-20"
    >
      <div
        className="pointer-events-none absolute inset-0 opacity-[0.03]"
        style={{ backgroundImage: "radial-gradient(circle, #0f172a 1px, transparent 1px)", backgroundSize: "28px 28px" }}
        aria-hidden="true"
      />

      <div className="relative mx-auto max-w-[1440px]">
        <div className="flex flex-col items-center gap-4 text-center">
          <AnimateInView>
            <Badge variant="secondary" className="rounded-full border border-[var(--color-shade)] bg-white px-4 py-1 text-xs font-semibold text-[var(--color-dark-neutral-2)]">
              {content.sectionTag}
            </Badge>
          </AnimateInView>
          <AnimateInView delay={1}>
            <h2 className="font-heading max-w-2xl text-3xl font-bold tracking-tight text-[var(--color-dark-neutral-1)] sm:text-4xl">
              {content.heading}
            </h2>
          </AnimateInView>
          <AnimateInView delay={2}>
            <p className="max-w-2xl text-[var(--color-dark-neutral-2)]">{content.subheading}</p>
          </AnimateInView>
        </div>

        <AnimateInView delay={3} className="mt-12">
          <div className="flex flex-col gap-5">
            {/* Tabs + nav */}
            <div className="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
              <div className="flex gap-3 overflow-x-auto pb-1">
                {content.useCases.map((useCase, index) => (
                  <SegmentUseCaseTab key={useCase.key} useCase={useCase} isActive={index === activeIndex} onClick={() => goTo(index)} />
                ))}
              </div>
              <div className="flex items-center justify-between gap-3 sm:justify-end">
                <div className="text-sm text-neutral-500">
                  <span className="font-semibold text-neutral-900">{String(activeIndex + 1).padStart(2, "0")}</span>
                  <span className="mx-1 text-neutral-300">/</span>
                  <span>{String(content.useCases.length).padStart(2, "0")}</span>
                </div>
                <div className="flex items-center gap-2">
                  <Button variant="outline" size="icon-sm" className="rounded-full border-neutral-200 bg-white" onClick={goPrev} aria-label={content.labels.previousUseCase}>
                    <ArrowLeft className="h-4 w-4" />
                  </Button>
                  <Button variant="outline" size="icon-sm" className="rounded-full border-neutral-200 bg-white" onClick={goNext} aria-label={content.labels.nextUseCase}>
                    <ArrowRight className="h-4 w-4" />
                  </Button>
                </div>
              </div>
            </div>

            {/* Embla Carousel */}
            <div className="overflow-hidden rounded-[36px]" ref={emblaRef}>
              <div className="flex">
                {content.useCases.map((useCase) => (
                  <div key={useCase.key} className="w-full shrink-0 grow-0 basis-full">
                    <AnimatePresence mode="wait">
                      <motion.div
                        key={useCase.key}
                        initial={{ opacity: 0.8 }}
                        animate={{ opacity: 1 }}
                        transition={{ duration: 0.3 }}
                      >
                        <SlideCard useCase={useCase} labels={content.labels} />
                      </motion.div>
                    </AnimatePresence>
                  </div>
                ))}
              </div>
            </div>

            {/* Bottom bar */}
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
                      className="relative h-2.5 touch-manipulation overflow-hidden rounded-full transition-[width,background-color] duration-300 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-neutral-300 motion-reduce:transition-none"
                      style={{
                        width: isActive ? "2.5rem" : "0.625rem",
                        backgroundColor: isActive ? "rgba(212,212,212,0.4)" : "rgba(212,212,212,0.9)",
                      }}
                      aria-label={`${content.labels.goToUseCase} ${useCase.type}`}
                      aria-current={isActive}
                    >
                      {isActive && (
                        <motion.div
                          className="absolute inset-y-0 left-0 rounded-full"
                          style={{ backgroundColor: theme.color }}
                          initial={{ width: "0%" }}
                          animate={{ width: `${progress}%` }}
                          transition={{ duration: 0.1, ease: "linear" }}
                        />
                      )}
                    </button>
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
