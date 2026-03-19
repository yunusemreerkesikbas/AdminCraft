import { Badge } from "@/components/ui/badge";
import { Layers, Database, Zap, Globe, LayoutTemplate, PanelTop, Boxes } from "lucide-react";
import { AnimateInView } from "@/components/AnimateInView";

const TRIO_ICONS: Record<string, React.ComponentType<{ className?: string }>> = {
  template: LayoutTemplate,
  slot: PanelTop,
  component: Boxes,
};

const ICONS: Record<string, React.ComponentType<{ className?: string }>> = {
  Layers,
  Database,
  Zap,
  Globe,
};

const CARD_STYLES = [
  {
    gradient: "from-[var(--color-theme-3)] to-[var(--color-theme-4)]",
    iconBg: "bg-[var(--color-theme-7)]",
    iconColor: "text-[var(--color-theme-3)]",
    numColor: "text-[var(--color-theme-3)]",
  },
  {
    gradient: "from-[var(--color-theme-4)] to-[var(--color-theme-6)]",
    iconBg: "bg-[var(--color-theme-6)]",
    iconColor: "text-[#8b8ef8]",
    numColor: "text-[#8b8ef8]",
  },
  {
    gradient: "from-[#a3e635] to-[var(--color-theme-3)]",
    iconBg: "bg-[#f0fdf4]",
    iconColor: "text-[#4ade80]",
    numColor: "text-[#4ade80]",
  },
  {
    gradient: "from-[var(--color-dark-neutral-1)] to-[var(--color-dark-neutral-2)]",
    iconBg: "bg-[var(--color-light-neutral-2)]",
    iconColor: "text-[var(--color-dark-neutral-1)]",
    numColor: "text-[var(--color-dark-neutral-2)]",
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

export function Features({ content }: { content: FeaturesContent }) {
  return (
    <section id="features" className="bg-[var(--color-light-neutral-1)] px-4 py-24 sm:px-6 lg:px-20">
      <div className="mx-auto max-w-[1440px]">
        {/* Header */}
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
            <h2 className="font-heading max-w-xl text-3xl font-bold tracking-tight text-[var(--color-dark-neutral-1)] sm:text-4xl">
              {content.heading}
            </h2>
          </AnimateInView>
          <AnimateInView className="animate-in-view-delay-2">
            <p className="max-w-xl text-[var(--color-dark-neutral-2)]">{content.subheading}</p>
          </AnimateInView>
        </div>

        {/* Power Trio — Featured Card */}
        <AnimateInView className="mt-14 animate-in-view-delay-1">
          <div
            className="relative overflow-hidden rounded-3xl p-px"
            style={{
              background: "linear-gradient(135deg, var(--color-theme-3) 0%, #818cf8 50%, #1D4ED8 100%)",
            }}
          >
            <div
              className="relative rounded-3xl px-8 py-10 sm:px-12 sm:py-12"
              style={{
                background: "linear-gradient(160deg, #040d1c 0%, #060f22 50%, #0a1530 100%)",
              }}
            >
              {/* Radial glow */}
              <div
                className="pointer-events-none absolute inset-0"
                style={{
                  background:
                    "radial-gradient(ellipse 60% 40% at 50% 0%, rgba(37,99,235,0.15) 0%, transparent 70%)",
                }}
                aria-hidden
              />
              {/* Dot pattern */}
              <div
                className="pointer-events-none absolute inset-0 opacity-[0.03]"
                style={{
                  backgroundImage: "radial-gradient(circle, #fff 1px, transparent 1px)",
                  backgroundSize: "28px 28px",
                }}
                aria-hidden
              />

              <div className="relative">
                {/* Trio header */}
                <div className="flex flex-col items-start gap-3">
                  <span className="inline-flex items-center gap-2 rounded-full border border-white/10 bg-white/10 px-3 py-1 text-xs font-bold uppercase tracking-wider text-white/70 backdrop-blur-sm">
                    <span className="h-1.5 w-1.5 rounded-full bg-[#93c5fd]" />
                    {content.powerTrio.label}
                  </span>
                  <h3 className="font-heading text-2xl font-bold text-white sm:text-3xl">
                    {content.powerTrio.heading}
                  </h3>
                  <p className="max-w-2xl text-white/50">{content.powerTrio.subheading}</p>
                </div>

                {/* Trio columns */}
                <div className="mt-8 overflow-hidden rounded-2xl ring-1 ring-white/10 grid grid-cols-1 divide-y divide-white/[0.06] sm:grid-cols-3 sm:divide-x sm:divide-y-0">
                  {content.powerTrio.items.map((item, i) => {
                    const Icon = TRIO_ICONS[item.key] ?? Layers;
                    return (
                      <div
                        key={item.key}
                        className="relative flex flex-col gap-4 bg-white/[0.04] p-6 transition-colors duration-200 hover:bg-white/[0.07]"
                      >
                        {/* Accent top line */}
                        <div
                          className="absolute inset-x-0 top-0 h-px"
                          style={{
                            background:
                              i === 0
                                ? "linear-gradient(90deg, var(--color-theme-3) 0%, #818cf8 100%)"
                                : i === 1
                                ? "linear-gradient(90deg, #818cf8 0%, #6366f1 100%)"
                                : "linear-gradient(90deg, #6366f1 0%, #1D4ED8 100%)",
                            opacity: 0.75,
                          }}
                        />

                        {/* Ghost number */}
                        <span className="pointer-events-none absolute right-4 top-3 select-none font-heading text-5xl font-black leading-none text-white/[0.04]">
                          0{i + 1}
                        </span>

                        {/* Icon */}
                        <div
                          className="w-fit rounded-xl p-3"
                          style={{
                            background:
                              "linear-gradient(135deg, rgba(37,99,235,0.25) 0%, rgba(29,78,216,0.15) 100%)",
                            boxShadow: "0 0 0 1px rgba(37,99,235,0.25)",
                          }}
                        >
                          <Icon className="h-5 w-5 text-[#93c5fd]" />
                        </div>

                        {/* Text */}
                        <div className="flex flex-col gap-1">
                          <p className="text-[10px] font-semibold uppercase tracking-widest text-white/35">
                            {item.label}
                          </p>
                          <h4 className="font-heading text-base font-bold text-white">{item.name}</h4>
                          <p className="mt-1 text-sm leading-relaxed text-white/45">{item.description}</p>
                        </div>
                      </div>
                    );
                  })}
                </div>
              </div>
            </div>
          </div>
        </AnimateInView>

        {/* Feature Cards */}
        <div className="mt-5 grid gap-5 sm:grid-cols-2 lg:grid-cols-4 items-stretch">
          {content.cards.map((card, i) => {
            const Icon = ICONS[card.icon] ?? Layers;
            const style = CARD_STYLES[i % CARD_STYLES.length];
            const delayClass = i < 3 ? `animate-in-view-delay-${i + 1}` : "animate-in-view-delay-4";
            return (
              <AnimateInView
                key={card.title}
                className={`h-full animate-feature-card-in ${delayClass}`}
              >
                <div
                  className={`group relative h-full rounded-2xl bg-gradient-to-br p-px ${style.gradient} transition-all duration-300 ease-out hover:shadow-[0_20px_40px_-12px_rgba(0,0,0,0.18),0_0_0_1px_rgba(0,0,0,0.04)] hover:-translate-y-2 hover:scale-[1.02] active:scale-[0.99]`}
                >
                  <div className="relative h-full rounded-2xl bg-white p-6 flex flex-col gap-4 transition-shadow duration-300 group-hover:shadow-sm">
                    <span
                      className={`absolute right-5 top-5 font-heading text-4xl font-extrabold leading-none opacity-[0.07] transition-transform duration-300 group-hover:scale-110 ${style.numColor}`}
                    >
                      0{i + 1}
                    </span>
                    <div className={`w-fit rounded-xl p-3 shrink-0 transition-transform duration-300 ease-out group-hover:scale-105 ${style.iconBg}`}>
                      <Icon className={`h-5 w-5 ${style.iconColor}`} />
                    </div>
                    <div className="flex flex-col gap-2 flex-1 min-h-0">
                      <h3 className="font-heading text-base font-bold text-[var(--color-dark-neutral-1)] shrink-0">
                        {card.title}
                      </h3>
                      <p className="text-sm leading-relaxed text-[var(--color-dark-neutral-2)]">
                        {card.description}
                      </p>
                    </div>
                  </div>
                </div>
              </AnimateInView>
            );
          })}
        </div>
      </div>
    </section>
  );
}
