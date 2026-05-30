"use client";
import {
  ArrowRightIcon,
  BellIcon,
  BulbIcon,
  EyeIcon,
  ListIcon,
} from "@/components/icons";
import { Reveal } from "@/components/Reveal";
import { ASSETS } from "@/lib/assets";
import { useTranslations } from "next-intl";
import Image from "next/image";

type Chip = { title: string; action: string; solid: boolean };
type Tile = { title: string; body: string };

const TILE_COLORS = ["#0059ff", "#ff1f3d", "#9000ff", "#12a70a"];
const TILE_ICONS = [BellIcon, ListIcon, EyeIcon, BulbIcon];

export function AISuggestions() {
  const t = useTranslations("managedProcess");
  const chips = t.raw("chips") as Chip[];
  const tiles = t.raw("tiles") as Tile[];

  return (
    <section id="smart-assist" className="bg-white py-14 md:py-24">
      <div className="max-w-6xl mx-auto px-6">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-12 items-center">
          <div>
            <Reveal>
              <div className="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-surface mb-5">
                <span className="size-1.5 rounded-full bg-[#0E121B]" />
                <span className="text-[13px] font-medium text-ink-muted">
                  {t("badge")}
                </span>
              </div>
            </Reveal>
            <Reveal delay={80}>
              <h2 className="font-heading text-[36px] md:text-[52px] leading-[1.05] font-medium tracking-[-0.02em] text-ink">
                {t("headline")}
              </h2>
            </Reveal>
            <Reveal delay={150}>
              <p className="mt-5 text-[17px] text-ink-muted leading-relaxed max-w-md">
                {t("subhead")}
              </p>
              <a
                href="#cta-footer"
                className="mt-8 inline-flex items-center gap-2 px-6 py-3.5 rounded-full bg-ink text-white text-[14px] font-medium hover:bg-ink/90 transition-colors"
              >
                {t("cta")}
                <ArrowRightIcon size={14} />
              </a>
            </Reveal>
          </div>

          <Reveal delay={120}>
            <div className="relative rounded-[32px] overflow-hidden aspect-square lg:aspect-[4/3] bg-[linear-gradient(150deg,#f6d9c4_0%,#f3cdd6_40%,#e9c7f0_70%,#d9c0ee_100%)]">
              {/* Tilted phone */}
              <div className="absolute right-[-6%] top-1/2 -translate-y-1/2 w-[58%] rotate-[14deg]">
                <Image
                  src={ASSETS.aiSuggest.phone}
                  alt="Craftive süreç önizlemesi"
                  width={350}
                  height={720}
                  className="w-full h-auto drop-shadow-2xl rounded-[34px]"
                />
              </div>
              {/* Floating step chips */}
              <div className="absolute left-5 top-7 flex flex-col gap-3 w-[58%]">
                {chips.map((c) => (
                  <div
                    key={c.title}
                    className="rounded-2xl bg-white/85 backdrop-blur-md shadow-card p-3.5"
                  >
                    <div className="text-[13px] font-medium text-ink leading-snug">
                      {c.title}
                    </div>
                    <span
                      className={`mt-2 inline-block px-3 py-1.5 rounded-full text-[12px] font-medium ${
                        c.solid
                          ? "bg-[#12a70a] text-white"
                          : "bg-surface text-ink-muted"
                      }`}
                    >
                      {c.action}
                    </span>
                  </div>
                ))}
              </div>
            </div>
          </Reveal>
        </div>

        <Reveal delay={120}>
          <div className="mt-16 grid grid-cols-2 md:grid-cols-4 gap-4 md:gap-5">
            {tiles.map((tile, i) => {
              const Icon = TILE_ICONS[i];
              return (
                <div
                  key={tile.title}
                  className="rounded-3xl border border-line bg-white p-5 md:p-6 flex flex-col gap-4 md:gap-5"
                >
                  <div
                    className="size-10 md:size-11 rounded-full flex items-center justify-center flex-shrink-0"
                    style={{ backgroundColor: TILE_COLORS[i] }}
                  >
                    <Icon size={18} className="text-white" />
                  </div>
                  <div>
                    <div className="font-heading text-[17px] md:text-[20px] font-medium text-ink leading-tight">
                      {tile.title}
                    </div>
                    <div className="mt-1.5 md:mt-2 text-ink-muted text-[13px] md:text-[14px] leading-snug">
                      {tile.body}
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        </Reveal>
      </div>
    </section>
  );
}
