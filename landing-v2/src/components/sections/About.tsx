"use client";
import {
  ActivityIcon,
  ArrowRightIcon,
  BookIcon,
  DropletIcon,
  MoonIcon,
  PhoneOffIcon,
  SparklesIcon,
  TargetIcon,
} from "@/components/icons";
import { Reveal } from "@/components/Reveal";
import { ASSETS } from "@/lib/assets";
import { DemoButton } from "@/components/DemoButton";
import { useTranslations } from "next-intl";
import Image from "next/image";
import type { ComponentType } from "react";

type IconType = ComponentType<{ size?: number; className?: string }>;
type Tile = { label?: string; image?: string; color?: string; Icon?: IconType };

export function About() {
  const t = useTranslations("about");
  const tags = t.raw("tags") as string[];

  const TOP_ROW: Tile[] = [
    { label: t("tile1"), color: "#7a7d85", Icon: PhoneOffIcon },
    { image: ASSETS.about.tile01 },
    { label: t("tile2"), color: "#0E121B", Icon: ActivityIcon },
    { image: ASSETS.about.tile02 },
    { label: t("tile3"), color: "#0059ff", Icon: TargetIcon },
    { image: ASSETS.about.tile03 },
    { label: t("tile4"), color: "#0283a7", Icon: ActivityIcon },
    { image: ASSETS.about.tile04 },
  ];

  const BOTTOM_ROW: Tile[] = [
    { image: ASSETS.about.tile05 },
    { label: t("tile5"), color: "#ff1f3d", Icon: SparklesIcon },
    { image: ASSETS.about.tile04 },
    { label: t("tile6"), color: "#9000ff", Icon: MoonIcon },
    { image: ASSETS.about.tile03 },
    { label: t("tile7"), color: "#0059ff", Icon: DropletIcon },
    { image: ASSETS.about.tile01 },
    { label: t("tile8"), color: "#ff2d92", Icon: BookIcon },
  ];

  return (
    <section className="relative bg-white pt-6 pb-0">
      {/* Headline area */}
      <div className="relative max-w-5xl mx-auto px-6 text-center mb-14">
        <Reveal>
          <h2 className="font-heading text-[40px] sm:text-[54px] md:text-[68px] leading-[1.08] font-medium tracking-[-0.035em] text-ink">
            <span>{t("headline1")} </span>
            <Image
              src={ASSETS.about.weatherEmoji}
              alt=""
              width={56}
              height={44}
              className="inline-block align-[-10px] mx-1"
            />
            <span> {t("headline2")}</span>
          </h2>
        </Reveal>

        <Reveal delay={120}>
          <div className="mt-8 flex flex-wrap items-center justify-center gap-2">
            <span className="text-[14px] text-ink-muted mr-3">
              {t("tagsIntro")}
            </span>
            {tags.map((tag) => (
              <span
                key={tag}
                className="px-4 py-1.5 rounded-full bg-surface text-ink text-[13px] font-medium"
              >
                {tag}
              </span>
            ))}
          </div>
        </Reveal>
      </div>

      {/* Marquee rows + centered phone */}
      <div className="relative h-[280px] md:h-[420px]">
        <div className="absolute top-[10px] md:top-[60px] inset-x-0 overflow-x-hidden mask-fade-x" style={{ contain: "layout style" }}>
          <div
            className="flex gap-4 marquee-x"
            style={{ width: "max-content" }}
          >
            {[...TOP_ROW, ...TOP_ROW, ...TOP_ROW].map((tile, i) => (
              <HabitTile key={i} {...tile} />
            ))}
          </div>
        </div>

        <div className="absolute top-[146px] md:top-[196px] inset-x-0 overflow-x-hidden mask-fade-x" style={{ contain: "layout style" }}>
          <div
            className="flex gap-4 marquee-x-reverse"
            style={{ width: "max-content" }}
          >
            {[...BOTTOM_ROW, ...BOTTOM_ROW, ...BOTTOM_ROW].map((tile, i) => (
              <HabitTile key={i} {...tile} />
            ))}
          </div>
        </div>
      </div>

      {/* Rating + CTA */}
      <div className="relative z-20 max-w-4xl mx-auto px-6 text-center mt-16 pb-10">
        <Reveal delay={80}>
          <p className="mt-7 max-w-[600px] mx-auto text-[18px] md:text-[20px] text-ink/70 leading-relaxed">
            {t("description")}
          </p>
        </Reveal>

        <Reveal delay={140}>
          <div className="mt-7 flex items-center justify-center gap-3 flex-wrap">
            <DemoButton location="about" className="px-6 py-3.5 rounded-full bg-ink text-white text-[14px] font-medium leading-none flex items-center gap-2 hover:bg-ink/90 transition-colors">
              {t("primaryCta")}
              <ArrowRightIcon size={14} />
            </DemoButton>
            <a
              href="https://wa.me/905394204255?text=Merhaba%2C%20Craftive%20hakk%C4%B1nda%20bilgi%20almak%20istiyorum."
              target="_blank"
              rel="noreferrer"
              className="px-6 py-3.5 rounded-full border border-line text-ink text-[14px] font-medium leading-none flex items-center gap-2 hover:bg-surface transition-colors"
            >
              {t("secondaryCta")}
            </a>
          </div>
        </Reveal>
      </div>

      {/* Cloud transition zone — rises up behind CTA */}
      <div
        className="relative overflow-hidden pointer-events-none -mt-[260px] md:-mt-[350px] h-[380px] md:h-[480px]"
      >
        <div className="absolute inset-0 bg-[#f7f7f7]" />
        {/* Full-width top fade — blends clouds into white above */}
        <div
          className="absolute top-0 inset-x-0 z-10 pointer-events-none"
          style={{
            height: "160px",
            background: "linear-gradient(to bottom, #ffffff, transparent)",
          }}
        />
        {/* Fade to white below */}
        <div
          className="absolute bottom-0 left-0 right-0 h-20 pointer-events-none z-10"
          style={{
            background: "linear-gradient(to top, #ffffff, transparent)",
          }}
        />
        {/* Big cloud — full-width atmospheric backdrop */}
        <div className="absolute inset-0">
          <Image
            src={ASSETS.clouds.big}
            alt=""
            fill
            sizes="100vw"
            className="object-cover"
          />
        </div>
        {/* Medium cloud — left anchor */}
        <Image
          src={ASSETS.clouds.medium}
          alt=""
          width={891}
          height={296}
          sizes="(max-width: 768px) 55vw, 491px"
          className="absolute left-0 bottom-0 w-[55%] h-auto"
        />
        {/* Small cloud — right side */}
        <Image
          src={ASSETS.clouds.small}
          alt=""
          width={502}
          height={300}
          sizes="(max-width: 768px) 36vw, 181px"
          className="absolute right-0 bottom-4 w-[36%] h-auto"
        />
      </div>
    </section>
  );
}

function HabitTile({ label, image, color, Icon }: Tile) {
  if (image) {
    return (
      <div className="w-[210px] h-[120px] rounded-3xl overflow-hidden flex-shrink-0">
        <Image
          src={image}
          alt=""
          width={210}
          height={120}
          sizes="210px"
          className="object-cover w-full h-full"
        />
      </div>
    );
  }
  return (
    <div className="w-[210px] h-[120px] rounded-3xl border border-line bg-white flex items-center gap-3 px-5 flex-shrink-0">
      {Icon && color && (
        <span
          className="size-10 rounded-full flex items-center justify-center flex-shrink-0"
          style={{ backgroundColor: color }}
        >
          <Icon size={18} className="text-white" />
        </span>
      )}
      <span className="text-ink text-[15px] font-medium leading-tight">
        {label}
      </span>
    </div>
  );
}
