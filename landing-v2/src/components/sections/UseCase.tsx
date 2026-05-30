"use client";

import Image from "next/image";
import { useMemo, useState } from "react";
import { useTranslations } from "next-intl";
import { ASSETS } from "@/lib/assets";
import { Reveal } from "@/components/Reveal";
import { BriefcaseIcon, GradCapIcon, MonitorIcon, UsersIcon } from "@/components/icons";

type TabCopy = {
  label: string;
  description: string;
  stat: string;
  statLabel: string;
};

const TAB_ICONS = [BriefcaseIcon, GradCapIcon, MonitorIcon, UsersIcon];
const TAB_IMAGES = [
  ASSETS.useCase.agency,
  ASSETS.useCase.saas,
  ASSETS.useCase.corporate,
  ASSETS.useCase.ecommerce,
];

export function UseCase() {
  const t = useTranslations("useCase");
  const tabsCopy = useMemo(() => t.raw("tabs") as TabCopy[], [t]);
  const rhythmTags = useMemo(() => t.raw("rhythmTags") as string[], [t]);

  const [active, setActive] = useState(0);
  const cur = tabsCopy[active];

  return (
    <section id="use-case" className="bg-white py-20">
      <div className="max-w-6xl mx-auto px-6">
        <div className="rounded-[40px] bg-surface px-6 md:px-10 py-14 bg-[radial-gradient(#dcdcdc_1px,transparent_1px)] [background-size:18px_18px]">
          <div className="text-center mb-9">
            <Reveal>
              <div className="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-white border border-line mb-5">
                <span className="size-1.5 rounded-full bg-[#0E121B]" />
                <span className="text-[13px] font-medium text-ink-muted">{t("badge")}</span>
              </div>
            </Reveal>
            <Reveal delay={80}>
              <h2 className="font-heading text-[34px] md:text-[48px] leading-[1.1] font-medium tracking-[-0.02em] text-ink">
                {t("headline1")}<br />{t("headline2")}
              </h2>
            </Reveal>
          </div>

          {/* Tab row */}
          <Reveal delay={120}>
            <div className="flex flex-wrap items-center gap-2 mb-8 justify-center px-1">
              {tabsCopy.map((tab, i) => {
                const Icon = TAB_ICONS[i];
                const isActive = i === active;
                return (
                  <button
                    key={tab.label}
                    onClick={() => setActive(i)}
                    className={`flex items-center gap-2 px-5 py-3 rounded-full text-[14px] font-medium leading-none transition-colors ${
                      isActive ? "bg-white text-ink shadow-soft" : "text-ink-muted hover:bg-white/60"
                    }`}
                  >
                    <Icon size={17} className={isActive ? "text-ink" : "text-ink-muted"} />
                    {tab.label}
                  </button>
                );
              })}
            </div>
          </Reveal>

          {/* Image with combined info card */}
          <Reveal delay={160}>
            <div className="relative rounded-3xl overflow-hidden h-[300px] md:h-[520px] bg-ink">
              <Image
                key={active}
                src={TAB_IMAGES[active]}
                alt=""
                fill
                priority
                className="object-cover object-center transition-opacity duration-500"
                sizes="(min-width: 1200px) 1080px, 100vw"
              />
              <div className="absolute inset-0 bg-gradient-to-b from-black/10 via-transparent to-black/30" />

              <div
                key={cur.label}
                className="absolute left-5 right-5 bottom-5 md:left-auto md:right-7 md:bottom-7 md:w-[360px] rounded-2xl bg-black/35 backdrop-blur-md border border-white/15 p-6 text-white animate-[fadeIn_0.35s_ease-out]"
              >
                <p className="text-[17px] leading-snug font-medium">{cur.description}</p>
                <div className="mt-5 flex items-baseline gap-3">
                  <span className="font-heading text-[34px] md:text-[40px] font-medium leading-none">{cur.stat}</span>
                  <span className="text-[13px] text-white/80">{cur.statLabel}</span>
                </div>
              </div>
            </div>
          </Reveal>

          <div className="mt-10 text-center">
            <Reveal>
              <p className="text-[14px] font-medium text-ink-muted mb-4">{t("footerText")}</p>
            </Reveal>
            <Reveal delay={80}>
              <div className="flex flex-wrap items-center justify-center gap-x-6 gap-y-2">
                {rhythmTags.map((tag) => (
                  <span key={tag} className="text-ink-muted text-[14px] font-medium">
                    {tag}
                  </span>
                ))}
              </div>
            </Reveal>
          </div>
        </div>
      </div>
    </section>
  );
}
