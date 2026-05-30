"use client";
import Image from "next/image";
import { useTranslations } from "next-intl";
import { ASSETS } from "@/lib/assets";
import { HERO_LQIP } from "@/lib/hero-lqip";
import { Reveal } from "@/components/Reveal";
import { HeroCtaSection } from "@/components/HeroCtaSection";

export function Hero() {
  const t = useTranslations("hero");

  return (
    <section id="hero" className="relative w-full overflow-hidden min-h-screen flex items-center">
      {/* Full-bleed background */}
      <div className="absolute inset-0 -z-10">
        {/*
         * <picture> serves mobile WebP on narrow screens, desktop WebP elsewhere.
         * Next.js <Image> is bypassed here because unoptimized:true makes it a
         * pass-through anyway — using <picture> gives us the media-query srcset
         * without extra JS. fetchPriority="high" + rel="preload" (inserted by
         * the layout) ensure the browser fetches this before paint.
         */}
        <picture>
          <source
            srcSet={ASSETS.hero.bgMobile}
            media="(max-width: 768px)"
            type="image/webp"
          />
          <source
            srcSet={ASSETS.hero.bg}
            type="image/webp"
          />
          {/* eslint-disable-next-line @next/next/no-img-element */}
          <img
            src={ASSETS.hero.bgFallback}
            alt=""
            fetchPriority="high"
            decoding="async"
            style={{
              position: "absolute",
              inset: 0,
              width: "100%",
              height: "100%",
              objectFit: "cover",
              objectPosition: "center",
              backgroundImage: `url(${HERO_LQIP})`,
              backgroundSize: "cover",
            }}
          />
        </picture>
        {/* Subtle top fade for nav readability */}
        <div className="absolute inset-0 bg-gradient-to-b from-black/30 via-transparent to-transparent" />
        {/* Radial dark vignette — darkens center behind text */}
        <div className="absolute inset-0" style={{ background: "radial-gradient(ellipse 70% 60% at 50% 45%, rgba(0,0,0,0.72) 0%, rgba(0,0,0,0.35) 55%, transparent 80%)" }} />
      </div>

      {/* Foreground content */}
      <div className="relative w-full pt-24 pb-20 flex flex-col items-center text-center text-white" style={{ zIndex: 1 }}>
        <div className="px-6 w-full flex flex-col items-center">
          {/* Pill badge */}
          <Reveal>
            <div className="inline-flex items-center gap-2 pl-1 pr-3 py-1 bg-white/10 backdrop-blur-md rounded-full border border-white/15 mb-7">
              <span className="px-2.5 py-1 text-[12px] font-medium leading-none text-white bg-black rounded-full">{t("badgeNew")}</span>
              <span className="text-[14px] text-white">{t("badgeText")}</span>
            </div>
          </Reveal>

          {/* Headline */}
          <Reveal delay={50}>
            <h1 className="font-heading text-[40px] sm:text-[60px] md:text-[78px] leading-[1.05] font-medium tracking-[-0.03em] text-white max-w-[960px]">
              {t("headline")}
            </h1>
          </Reveal>

          {/* Subhead */}
          <Reveal delay={120}>
            <p className="mt-7 text-[18px] md:text-[22px] leading-[1.6] text-white/85 max-w-[560px]">
              {t("subhead")}
            </p>
          </Reveal>

          {/* CTAs */}
          <Reveal delay={180}>
            <HeroCtaSection primaryCta={t("primaryCta")} secondaryCta={t("secondaryCta")} />
          </Reveal>
        </div>

      </div>
    </section>
  );
}
