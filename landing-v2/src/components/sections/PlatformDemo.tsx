"use client";

import { useState } from "react";
import { useTranslations, useLocale } from "next-intl";
import { ASSETS } from "@/lib/assets";
import { Reveal } from "@/components/Reveal";
import { ArrowRightIcon, CheckIcon } from "@/components/icons";
import { DemoRequestModal } from "@/components/modals/DemoRequestModal";

/* ── Tab icons (inline SVG) ──────────────────────────────────────── */

function SmartEditIcon({ active }: { active: boolean }) {
  return (
    <svg width="20" height="20" viewBox="0 0 20 20" fill="none" aria-hidden>
      <rect x="2" y="3" width="12" height="10" rx="1.5" stroke={active ? "#0E121B" : "currentColor"} strokeWidth="1.5" />
      <path d="M5 7h6M5 10h4" stroke={active ? "#0E121B" : "currentColor"} strokeWidth="1.5" strokeLinecap="round" />
      <path d="M13.5 11l4-4-1.5-1.5-4 4V11h1.5Z" fill={active ? "#0E121B" : "currentColor"} />
    </svg>
  );
}

function MediaIcon({ active }: { active: boolean }) {
  return (
    <svg width="20" height="20" viewBox="0 0 20 20" fill="none" aria-hidden>
      <rect x="2" y="4" width="16" height="12" rx="1.5" stroke={active ? "#0E121B" : "currentColor"} strokeWidth="1.5" />
      <circle cx="7" cy="8.5" r="1.5" fill={active ? "#0E121B" : "currentColor"} />
      <path d="M2 13l4-3.5 3 2.5 3-3.5 4 4.5" stroke={active ? "#0E121B" : "currentColor"} strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  );
}

function PageBuilderIcon({ active }: { active: boolean }) {
  return (
    <svg width="20" height="20" viewBox="0 0 20 20" fill="none" aria-hidden>
      <rect x="2" y="3" width="16" height="14" rx="1.5" stroke={active ? "#0E121B" : "currentColor"} strokeWidth="1.5" />
      <path d="M2 7h16" stroke={active ? "#0E121B" : "currentColor"} strokeWidth="1.5" />
      <rect x="5" y="10" width="4" height="4" rx="0.75" fill={active ? "#0E121B" : "currentColor"} opacity={active ? 1 : 0.4} />
      <rect x="11" y="10" width="4" height="1.5" rx="0.75" fill={active ? "#0E121B" : "currentColor"} opacity={active ? 1 : 0.4} />
      <rect x="11" y="12.5" width="2.5" height="1.5" rx="0.75" fill={active ? "#0E121B" : "currentColor"} opacity={active ? 1 : 0.4} />
    </svg>
  );
}

/* ── Data ────────────────────────────────────────────────────────── */

const TAB_ICONS = [SmartEditIcon, MediaIcon, PageBuilderIcon] as const;

const TAB_VIDEOS = [
  ASSETS.videos.review4,
  ASSETS.videos.review1,
  ASSETS.videos.review2,
];

type TabCopy = {
  id: string;
  label: string;
  heading: string;
  description: string;
  features: string[];
  cta: string;
};

/* ── Component ───────────────────────────────────────────────────── */

export function PlatformDemo() {
  const t = useTranslations("platformDemo");
  const locale = useLocale();
  const tabs = t.raw("tabs") as TabCopy[];
  const [activeIdx, setActiveIdx] = useState(0);
  const [modalOpen, setModalOpen] = useState(false);

  return (
    <>
    <DemoRequestModal open={modalOpen} onClose={() => setModalOpen(false)} locale={locale} />
    <section id="platform-demo" className="bg-white py-20 md:py-28 overflow-hidden">
      <div className="max-w-6xl mx-auto px-6">

        {/* Badge + headline */}
        <Reveal>
          <div className="text-center mb-10">
            <div className="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-surface border border-line mb-5">
              <span className="size-1.5 rounded-full bg-[#0E121B]" />
              <span className="text-[13px] font-medium text-ink-muted">{t("badge")}</span>
            </div>
            <h2 className="font-heading text-[32px] md:text-[48px] leading-[1.1] font-medium tracking-[-0.03em] text-ink">
              {t("headline")}
            </h2>
            <p className="mt-4 text-[16px] md:text-[18px] text-ink-muted max-w-[520px] mx-auto leading-relaxed">
              {t("subhead")}
            </p>
          </div>
        </Reveal>

        {/* Tab row */}
        <Reveal delay={80}>
          <div className="flex justify-center mb-10">
            <div className="inline-flex border-b border-line w-full max-w-[600px]">
              {tabs.map((tab, i) => {
                const Icon = TAB_ICONS[i];
                const isActive = activeIdx === i;
                return (
                  <button
                    key={tab.id}
                    onClick={() => setActiveIdx(i)}
                    className={`flex flex-1 flex-col items-center gap-2 px-4 py-4 relative transition-colors duration-200 outline-none ${
                      isActive ? "text-ink" : "text-ink-muted hover:text-ink"
                    }`}
                  >
                    <Icon active={isActive} />
                    <span className={`text-[13px] md:text-[14px] font-medium whitespace-nowrap transition-colors ${isActive ? "text-ink" : "text-ink-muted"}`}>
                      {tab.label}
                    </span>
                    {/* Active indicator */}
                    <span
                      className={`absolute bottom-0 left-0 right-0 h-[2px] rounded-t-full transition-opacity duration-200 ${
                        isActive ? "opacity-100 bg-[#0E121B]" : "opacity-0 bg-[#0E121B]"
                      }`}
                    />
                  </button>
                );
              })}
            </div>
          </div>
        </Reveal>

        {/* Content area */}
        <div className="grid grid-cols-1 lg:grid-cols-[1fr_2fr] gap-10 lg:gap-14 items-center">

          {/* Left — text: stacked, opacity crossfade only */}
          <div style={{ display: "grid" }}>
            {tabs.map((tab, i) => {
              const isActive = activeIdx === i;
              return (
                <div
                  key={tab.id}
                  aria-hidden={!isActive}
                  style={{
                    gridArea: "1 / 1",
                    opacity: isActive ? 1 : 0,
                    transition: "opacity 0.35s ease",
                    pointerEvents: isActive ? "auto" : "none",
                  }}
                >
                  <h3 className="font-heading text-[26px] md:text-[32px] leading-[1.2] font-medium tracking-[-0.025em] text-ink mb-4">
                    {tab.heading}
                  </h3>
                  <p className="text-[15px] md:text-[16px] text-ink-muted leading-[1.7] mb-7">
                    {tab.description}
                  </p>
                  <ul className="space-y-3 mb-9">
                    {tab.features.map((f) => (
                      <li key={f} className="flex items-start gap-3 text-[14px] text-ink leading-snug">
                        <span className="mt-0.5 size-5 rounded-full bg-[#0E121B]/10 flex items-center justify-center flex-shrink-0">
                          <CheckIcon size={11} className="text-[#0E121B]" />
                        </span>
                        {f}
                      </li>
                    ))}
                  </ul>
                  <button
                    type="button"
                    onClick={() => setModalOpen(true)}
                    className="inline-flex items-center gap-2 px-6 py-3.5 rounded-full bg-ink text-white text-[14px] font-medium hover:bg-ink/85 transition-colors"
                  >
                    {tab.cta}
                    <ArrowRightIcon size={14} />
                  </button>
                </div>
              );
            })}
          </div>

          {/* Right — video: sliding carousel */}
          <div className="overflow-hidden rounded-2xl shadow-2xl">
            <div
              className="flex"
              style={{
                transform: `translateX(-${activeIdx * 100}%)`,
                transition: "transform 0.4s cubic-bezier(0.4, 0, 0.2, 1)",
              }}
            >
              {tabs.map((tab, i) => (
                <div key={tab.id} className="w-full flex-shrink-0">
                  <video
                    src={TAB_VIDEOS[i]}
                    autoPlay
                    loop
                    muted
                    playsInline
                    preload="none"
                    className="w-full aspect-video object-cover block"
                  />
                </div>
              ))}
            </div>
          </div>

        </div>
      </div>
    </section>
    </>
  );
}
