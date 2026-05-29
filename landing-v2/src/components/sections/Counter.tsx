"use client";

import { useEffect, useMemo, useRef, useState } from "react";
import { useTranslations } from "next-intl";
import { ASSETS } from "@/lib/assets";
import { Reveal } from "@/components/Reveal";

function useInView<T extends HTMLElement>(threshold = 0.3) {
  const ref = useRef<T | null>(null);
  const [inView, setInView] = useState(false);
  useEffect(() => {
    const el = ref.current;
    if (!el) return;
    const io = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          setInView(true);
          io.disconnect();
        }
      },
      { threshold }
    );
    io.observe(el);
    return () => io.disconnect();
  }, [threshold]);
  return [ref, inView] as const;
}

function CountUp({ target, suffix = "", durationMs = 1800 }: { target: number; suffix?: string; durationMs?: number }) {
  const [ref, inView] = useInView<HTMLSpanElement>();
  const [value, setValue] = useState(0);
  useEffect(() => {
    if (!inView) return;
    const start = performance.now();
    let raf = 0;
    const tick = (t: number) => {
      const p = Math.min(1, (t - start) / durationMs);
      const eased = 1 - Math.pow(1 - p, 3);
      setValue(Math.round(target * eased));
      if (p < 1) raf = requestAnimationFrame(tick);
    };
    raf = requestAnimationFrame(tick);
    return () => cancelAnimationFrame(raf);
  }, [inView, target, durationMs]);
  return (
    <span ref={ref}>
      {value.toLocaleString("tr-TR")}
      {suffix}
    </span>
  );
}

type Stat = { value: string; suffix?: string; label: string };

export function Counter() {
  const t = useTranslations("counter");
  const stats = useMemo(() => t.raw("stats") as Stat[], [t]);
  const bigNumberTarget = useMemo(() => {
    const parsed = parseInt(t("bigNumberValue"), 10);
    return Number.isFinite(parsed) ? parsed : 0;
  }, [t]);

  return (
    <section id="metrics" className="relative overflow-hidden bg-surface pt-[80px] md:pt-[160px] pb-20">
      <div className="relative max-w-5xl mx-auto px-6 text-center z-10">
        <Reveal>
          <div className="inline-flex items-center gap-2 px-5 py-2 rounded-full bg-white border border-line shadow-soft mb-5">
            <span className="text-[14px] text-ink">{t("badge")}</span>
          </div>
        </Reveal>

        <Reveal delay={80}>
          <h2 className="font-heading text-[40px] md:text-[64px] leading-[1.1] font-medium tracking-[-0.03em] text-ink mb-8">
            {t("headline1")}<br />{t("headline2")}
          </h2>
        </Reveal>

        <Reveal delay={150}>
          <div className="relative inline-block">
            <div
              className="font-heading text-[72px] sm:text-[96px] md:text-[120px] leading-none font-medium tracking-[-0.04em] text-[#ff4c00]"
              style={{ textShadow: "0 0 60px rgba(255,76,0,0.35)" }}
            >
              <CountUp target={bigNumberTarget} suffix={t("bigNumberSuffix")} />
            </div>
            <span className="absolute -bottom-2 right-0 md:translate-x-1/3 -rotate-6 rounded-full bg-white shadow-card px-3 md:px-4 py-1.5 md:py-2 text-[12px] md:text-[14px] text-ink whitespace-nowrap">
              {t("bigNumberLabel")}
            </span>
          </div>
        </Reveal>
      </div>

      {/* Globe + clouds */}
      <Reveal delay={150}>
        <div
          className="relative mt-[60px] mx-auto w-full max-w-[1080px] aspect-[1080/440]"
          style={{
            WebkitMaskImage: "linear-gradient(to bottom, black 55%, transparent 90%)",
            maskImage: "linear-gradient(to bottom, black 55%, transparent 90%)",
          }}
        >
          <div className="absolute inset-0 overflow-hidden bg-surface">
            <div className="absolute inset-x-0 top-0 aspect-[1080/800]" style={{ mixBlendMode: "darken" }}>
              <video
                src={ASSETS.videos.earth}
                autoPlay
                loop
                muted
                playsInline
                preload="metadata"
                className="w-full h-full object-cover"
                style={{ objectPosition: "50% 50%" }}
              />
            </div>
          </div>
          <img src={ASSETS.clouds.counter} alt="" aria-hidden className="pointer-events-none select-none absolute" style={{ width: "49%", left: "-21.3%", top: "50%" }} />
          <img src={ASSETS.clouds.small} alt="" aria-hidden className="pointer-events-none select-none absolute" style={{ width: "69.6%", left: "-2.8%", top: "65.9%" }} />
          <img src={ASSETS.clouds.small} alt="" aria-hidden className="pointer-events-none select-none absolute" style={{ width: "69.6%", left: "53.5%", top: "11.4%" }} />
        </div>
      </Reveal>

      {/* Stats row below globe */}
      <Reveal>
        <div className="relative max-w-4xl mx-auto px-6 grid grid-cols-3 gap-6 mt-4">
          {stats.map((s) => (
            <div key={s.label} className="text-center">
              <div className="font-heading text-[36px] sm:text-[48px] md:text-[60px] leading-[0.95] font-medium tracking-[-0.03em] text-ink">
                {s.value}
                {s.suffix && <span className="text-ink-muted">{s.suffix}</span>}
              </div>
              <div className="mt-3 text-[14px] md:text-[16px] text-ink-muted max-w-[220px] mx-auto leading-snug">{s.label}</div>
            </div>
          ))}
        </div>
        <p className="mt-8 text-center text-[12px] text-ink-faint max-w-md mx-auto px-6">{t("statsNote")}</p>
      </Reveal>
    </section>
  );
}
