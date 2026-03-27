import type { ComponentType } from "react";
import { AnimateInView } from "@/components/AnimateInView";
import { Badge } from "@/components/ui/badge";
import {
  Boxes,
  Database,
  Globe,
  Layers,
  LayoutTemplate,
  PanelTop,
  Zap,
} from "lucide-react";

const TRIO_ICONS: Record<string, ComponentType<{ className?: string }>> = {
  template: LayoutTemplate,
  slot: PanelTop,
  component: Boxes,
};

const ICONS: Record<string, ComponentType<{ className?: string }>> = {
  Layers,
  Database,
  Zap,
  Globe,
};

const TRIO_ACCENTS = [
  {
    glow: "rgba(96,165,250,0.16)",
    border: "rgba(96,165,250,0.24)",
    iconBg: "bg-[#60a5fa]/12",
    icon: "text-[#93c5fd]",
    chip: "bg-[#60a5fa]/14 text-[#bfdbfe]",
    number: "text-[#60a5fa]/12",
  },
  {
    glow: "rgba(129,140,248,0.16)",
    border: "rgba(129,140,248,0.24)",
    iconBg: "bg-[#818cf8]/12",
    icon: "text-[#c7d2fe]",
    chip: "bg-[#818cf8]/14 text-[#c7d2fe]",
    number: "text-[#818cf8]/12",
  },
  {
    glow: "rgba(56,189,248,0.14)",
    border: "rgba(56,189,248,0.24)",
    iconBg: "bg-[#38bdf8]/12",
    icon: "text-[#bae6fd]",
    chip: "bg-[#38bdf8]/14 text-[#bae6fd]",
    number: "text-[#38bdf8]/12",
  },
];

const CARD_ACCENTS = [
  {
    glow: "rgba(96,165,250,0.14)",
    border: "rgba(96,165,250,0.24)",
    iconBg: "bg-[#60a5fa]/12",
    iconColor: "text-[#93c5fd]",
    number: "text-[#60a5fa]/16",
  },
  {
    glow: "rgba(129,140,248,0.14)",
    border: "rgba(129,140,248,0.24)",
    iconBg: "bg-[#818cf8]/12",
    iconColor: "text-[#c7d2fe]",
    number: "text-[#818cf8]/16",
  },
  {
    glow: "rgba(74,222,128,0.12)",
    border: "rgba(74,222,128,0.24)",
    iconBg: "bg-[#4ade80]/10",
    iconColor: "text-[#86efac]",
    number: "text-[#4ade80]/14",
  },
  {
    glow: "rgba(244,244,245,0.08)",
    border: "rgba(244,244,245,0.18)",
    iconBg: "bg-white/10",
    iconColor: "text-white/82",
    number: "text-white/[0.1]",
  },
];

type TrioItem = { key: string; name: string; label: string; description: string };
type PowerTrio = { label: string; heading: string; subheading: string; items: TrioItem[] };
type FeatureCard = { icon: string; title: string; description: string };
type FeaturesContent = {
  sectionTag: string;
  heading: string;
  subheading: string;
  powerTrio: PowerTrio;
  cards: FeatureCard[];
};

function TrioStage({
  item,
  index,
}: {
  item: TrioItem;
  index: number;
}) {
  const Icon = TRIO_ICONS[item.key] ?? Layers;
  const accent = TRIO_ACCENTS[index % TRIO_ACCENTS.length];
  const stepNum = String(index + 1).padStart(2, "0");

  return (
    <div
      className="group relative h-full overflow-hidden rounded-[28px] border bg-[linear-gradient(180deg,rgba(10,15,30,0.7)_0%,rgba(6,10,24,0.86)_100%)] p-6 transition-[border-color,box-shadow,background-color] duration-300"
      style={{
        borderColor: accent.border,
        boxShadow: `0 24px 60px -44px ${accent.glow}`,
      }}
    >
      <div
        className="pointer-events-none absolute inset-0 opacity-[0.14]"
        style={{
          background: `radial-gradient(circle at 12% 8%, ${accent.glow} 0%, transparent 42%)`,
        }}
        aria-hidden="true"
      />
      <div className="relative flex h-full flex-col gap-5">
        <div className="flex items-start justify-between gap-4">
          <div
            className={`flex h-12 w-12 items-center justify-center rounded-[18px] border border-white/10 ${accent.iconBg}`}
            aria-hidden="true"
          >
            <Icon className={`h-5 w-5 ${accent.icon}`} />
          </div>
          <span className={`font-heading text-5xl font-black leading-none ${accent.number}`}>
            {stepNum}
          </span>
        </div>

        <div className="space-y-3">
          <span className={`inline-flex rounded-full px-3 py-1 text-[11px] font-semibold uppercase tracking-[0.18em] ${accent.chip}`}>
            {item.label}
          </span>
          <h4 className="font-heading text-2xl font-bold tracking-tight text-white">
            {item.name}
          </h4>
          <p className="text-sm leading-7 text-white/58">
            {item.description}
          </p>
        </div>
      </div>
    </div>
  );
}

function FeatureTile({
  card,
  index,
}: {
  card: FeatureCard;
  index: number;
}) {
  const Icon = ICONS[card.icon] ?? Layers;
  const accent = CARD_ACCENTS[index % CARD_ACCENTS.length];
  const stepNum = String(index + 1).padStart(2, "0");
  const delayClass = index < 3 ? `animate-in-view-delay-${index + 1}` : "animate-in-view-delay-4";

  return (
    <AnimateInView className={`h-full ${delayClass}`}>
      <article
        className="group relative h-full overflow-hidden rounded-[28px] border bg-[linear-gradient(180deg,rgba(12,18,35,0.9)_0%,rgba(7,11,24,0.98)_100%)] p-6 transition-[border-color,box-shadow,background-color] duration-300"
        style={{
          borderColor: accent.border,
          boxShadow: `0 28px 70px -52px ${accent.glow}`,
        }}
      >
        <div
          className="pointer-events-none absolute inset-0 opacity-[0.12] transition-opacity duration-300 group-hover:opacity-[0.22]"
          style={{
            background: `radial-gradient(circle at 0% 0%, ${accent.glow} 0%, transparent 44%)`,
          }}
          aria-hidden="true"
        />
        <div
          className="pointer-events-none absolute inset-x-6 bottom-0 h-px opacity-0 transition-opacity duration-300 group-hover:opacity-100"
          style={{
            background: `linear-gradient(90deg, transparent 0%, ${accent.border} 50%, transparent 100%)`,
          }}
          aria-hidden="true"
        />

        <div className="relative flex h-full flex-col gap-5">
          <div className="flex items-start justify-between gap-4">
            <div className={`flex h-12 w-12 items-center justify-center rounded-[18px] border border-white/10 ${accent.iconBg}`}>
              <Icon className={`h-5 w-5 ${accent.iconColor}`} />
            </div>
            <span className={`font-heading text-5xl font-black leading-none ${accent.number}`}>
              {stepNum}
            </span>
          </div>

          <div className="space-y-3">
            <h3 className="font-heading text-xl font-bold tracking-tight text-white">
              {card.title}
            </h3>
            <p className="text-sm leading-7 text-white/58">
              {card.description}
            </p>
          </div>
        </div>
      </article>
    </AnimateInView>
  );
}

export function Features({ content }: { content: FeaturesContent }) {
  return (
    <section
      id="features"
      className="relative overflow-hidden px-4 py-24 sm:px-6 lg:px-20"
      style={{
        background:
          "linear-gradient(180deg, #f8fafc 0%, #f2f6fb 12%, #e7edf8 21%, #cad6e8 29%, #0b1325 42%, #081327 72%, #07101f 100%)",
      }}
    >
      <div
        className="pointer-events-none absolute inset-x-0 top-0 h-[34%]"
        style={{
          background:
            "radial-gradient(ellipse 56% 52% at 50% 0%, rgba(255,255,255,0.92) 0%, rgba(248,250,252,0.64) 28%, rgba(226,236,249,0.22) 52%, rgba(255,255,255,0) 82%)",
        }}
        aria-hidden="true"
      />
      <div
        className="pointer-events-none absolute inset-x-0 top-[18%] h-[16rem]"
        style={{
          background:
            "linear-gradient(180deg, rgba(255,255,255,0) 0%, rgba(211,223,239,0.26) 38%, rgba(12,19,36,0.22) 82%, rgba(8,19,39,0) 100%)",
        }}
        aria-hidden="true"
      />
      <div
        className="pointer-events-none absolute inset-x-0 bottom-0 top-[32%] opacity-[0.04]"
        style={{
          backgroundImage: "radial-gradient(circle, #ffffff 1px, transparent 1px)",
          backgroundSize: "28px 28px",
        }}
        aria-hidden="true"
      />
      <div
        className="pointer-events-none absolute inset-x-0 top-[30%] h-[34rem] bg-[radial-gradient(circle_at_center,rgba(59,130,246,0.16)_0%,rgba(59,130,246,0.06)_34%,transparent_72%)]"
        aria-hidden="true"
      />

      <div className="relative mx-auto max-w-[1440px]">
        <div className="mx-auto max-w-3xl text-center">
          <AnimateInView>
            <Badge
              variant="secondary"
              className="rounded-full border border-[var(--color-shade)] bg-white/92 px-4 py-1 text-xs font-semibold text-[var(--color-dark-neutral-2)] shadow-[0_10px_24px_-18px_rgba(15,23,42,0.24)]"
            >
              {content.sectionTag}
            </Badge>
          </AnimateInView>
          <AnimateInView className="animate-in-view-delay-1 mt-5">
            <h2 className="font-heading text-3xl font-bold tracking-[-0.04em] text-[var(--color-dark-neutral-1)] sm:text-4xl lg:text-5xl">
              {content.heading}
            </h2>
          </AnimateInView>
          <AnimateInView className="animate-in-view-delay-2 mt-4">
            <p className="mx-auto max-w-2xl text-[var(--color-dark-neutral-2)]">
              {content.subheading}
            </p>
          </AnimateInView>
        </div>

        <AnimateInView className="mt-16 animate-scale-in">
          <div className="relative overflow-hidden rounded-[36px] border border-white/10 bg-[linear-gradient(180deg,rgba(6,11,24,0.9)_0%,rgba(4,8,19,0.98)_100%)] p-6 shadow-[0_40px_100px_-56px_rgba(2,6,23,0.78)] sm:p-8 lg:p-10">
            <div
              className="pointer-events-none absolute -left-20 top-10 h-40 w-40 rounded-full bg-[#60a5fa]/12 blur-3xl animate-float-subtle"
              aria-hidden="true"
            />
            <div
              className="pointer-events-none absolute right-0 top-20 h-48 w-48 rounded-full bg-[#818cf8]/10 blur-3xl animate-glow-pulse"
              aria-hidden="true"
            />
            <div
              className="pointer-events-none absolute inset-x-10 top-0 h-px bg-[linear-gradient(90deg,transparent_0%,rgba(96,165,250,0.06)_18%,rgba(96,165,250,0.58)_50%,rgba(56,189,248,0.12)_82%,transparent_100%)] animate-shimmer-slow"
              aria-hidden="true"
            />
            <div className="grid gap-8 lg:grid-cols-[0.9fr_1.1fr] lg:items-start">
              <div className="relative space-y-5">
                <span className="inline-flex items-center gap-2 rounded-full border border-white/10 bg-white/[0.06] px-3 py-1 text-xs font-semibold uppercase tracking-[0.18em] text-white/70">
                  <span className="h-1.5 w-1.5 rounded-full bg-[#93c5fd]" />
                  {content.powerTrio.label}
                </span>
                <div className="space-y-4">
                  <h3 className="font-heading max-w-[12ch] text-3xl font-bold tracking-tight text-white sm:text-[2.5rem]">
                    {content.powerTrio.heading}
                  </h3>
                  <p className="max-w-[34rem] text-sm leading-7 text-white/56 sm:text-base">
                    {content.powerTrio.subheading}
                  </p>
                </div>
              </div>

              <div className="relative">
                <div
                  className="pointer-events-none absolute left-[12%] right-[12%] top-[3.25rem] hidden h-px bg-[linear-gradient(90deg,rgba(96,165,250,0.12)_0%,rgba(129,140,248,0.55)_46%,rgba(56,189,248,0.18)_100%)] animate-shimmer-slow lg:block"
                  aria-hidden="true"
                />
                <div className="grid gap-4 lg:auto-rows-fr lg:grid-cols-3 lg:items-stretch">
                  {content.powerTrio.items.map((item, index) => (
                    <AnimateInView
                      key={item.key}
                      className={`h-full ${index < 3 ? `animate-in-view-delay-${index + 1}` : "animate-in-view-delay-4"} animate-scale-in`}
                    >
                      <TrioStage item={item} index={index} />
                    </AnimateInView>
                  ))}
                </div>
              </div>
            </div>
          </div>
        </AnimateInView>

        <div className="mt-6 grid gap-5 sm:grid-cols-2 xl:grid-cols-4">
          {content.cards.map((card, index) => (
            <FeatureTile key={card.title} card={card} index={index} />
          ))}
        </div>
      </div>
    </section>
  );
}
