"use client";
import Image from "next/image";
import { useTranslations } from "next-intl";
import { Reveal } from "@/components/Reveal";
import { AnimatedDonut } from "@/components/AnimatedDonut";
import { CheckIcon, ActivityIcon } from "@/components/icons";
import { ASSETS } from "@/lib/assets";

type SpeedCopy = {
  title: string;
  description: string;
  kpiTitle: string;
  kpiNote: string;
  pills: string[];
};

type DeliveryCopy = {
  title: string;
  description: string;
  header: string;
  subheader: string;
  progressLabel: string;
  progressValue: string;
  items: { time: string; title: string; meta: string }[];
};

type SecurityCopy = {
  title: string;
  description: string;
  features: { icon: string; title: string; body: string }[];
};

type SeoAnalyticsCopy = {
  title: string;
  description: string;
  summary: string;
  stats: { label: string; value: string; delta: string }[];
  rings: { label: string; value: number }[];
};

type PortabilityCopy = {
  title: string;
  description: string;
  notification: {
    label: string;
    title: string;
    body: string;
    primary: string;
    secondary: string;
  };
  bgItems: { method: string; path: string; label: string }[];
};

const STREAK_DAYS = [
  { day: "Pzt", date: null as string | null, active: true, faded: false },
  { day: "Sal", date: "13", active: false, faded: false },
  { day: "Çar", date: "14", active: false, faded: false },
  { day: "Per", date: "15", active: false, faded: false },
  { day: "Cum", date: "16", active: false, faded: false },
  { day: "Cmt", date: "17", active: false, faded: true },
];

const PILL_COLORS = ["#9000ff", "#0059ff", "#12a70a", "#0E121B", "#0283a7"];
const RING_COLORS = ["#0E121B", "#9000ff", "#0059ff"];

export function Features() {
  const t = useTranslations("features");
  const speed = t.raw("speed") as SpeedCopy;
  const delivery = t.raw("managedDelivery") as DeliveryCopy;
  const security = t.raw("security") as SecurityCopy;
  const seoAnalytics = t.raw("seoAnalytics") as SeoAnalyticsCopy;
  const portability = t.raw("portability") as PortabilityCopy;

  return (
    <section id="features" className="relative bg-white py-24">
      <div className="max-w-6xl mx-auto px-6">
        <SectionHeader badge={t("badge")} headline={t("headline")} subhead={t("subhead")} />
        <div className="grid grid-cols-1 md:grid-cols-2 gap-5 items-stretch">
          <Reveal className="h-full">
            <SpeedCard copy={speed} />
          </Reveal>
          <Reveal delay={100} className="h-full">
            <DeliveryCard copy={delivery} />
          </Reveal>
          <Reveal delay={180} className="md:col-span-2">
            <SecurityCard copy={security} />
          </Reveal>
          <Reveal delay={80} className="h-full">
            <SeoAnalyticsCard copy={seoAnalytics} />
          </Reveal>
          <Reveal delay={160} className="h-full">
            <PortabilityCard copy={portability} />
          </Reveal>
        </div>
      </div>
    </section>
  );
}

function SectionHeader({ badge, headline, subhead }: { badge: string; headline: string; subhead: string }) {
  return (
    <div className="grid grid-cols-1 md:grid-cols-[1.1fr_1fr] gap-8 items-start mb-12">
      <div>
        <Reveal>
          <div className="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-white border border-line shadow-soft mb-5">
            <span className="text-[14px] text-ink">{badge}</span>
          </div>
        </Reveal>
        <Reveal delay={80}>
          <h2 className="font-heading text-[40px] md:text-[64px] leading-[1.02] font-medium tracking-[-0.025em] text-ink">
            {headline}
          </h2>
        </Reveal>
      </div>
      <Reveal delay={150}>
        <p className="md:pt-4 text-[17px] text-ink-muted leading-relaxed">
          {subhead}
        </p>
      </Reveal>
    </div>
  );
}

function CardHead({ title, description, dark = false }: { title: string; description: string; dark?: boolean }) {
  return (
    <div className="p-8 pb-5">
      <h3 className={`font-heading text-[26px] font-medium ${dark ? "text-white" : "text-ink"}`}>{title}</h3>
      <p className={`mt-2 text-[15px] leading-relaxed max-w-md ${dark ? "text-white/70" : "text-ink-muted"}`}>
        {description}
      </p>
    </div>
  );
}

function SpeedCard({ copy }: { copy: SpeedCopy }) {
  return (
    <div className="relative overflow-hidden rounded-3xl bg-[#e8e8e8] flex flex-col h-full">
      <CardHead title={copy.title} description={copy.description} />
      <div className="px-8 pb-8 mt-auto">
        <div className="rounded-2xl bg-white border border-line p-6 shadow-soft">
          <div className="text-center text-[48px] leading-none mb-2">⚡</div>
          <div className="text-center font-heading text-[22px] font-medium text-ink leading-tight whitespace-pre-line">
            {copy.kpiTitle}
          </div>
          <div className="text-center text-[13px] text-ink-muted mt-1">{copy.kpiNote}</div>
          <div className="flex items-end justify-center gap-2 mt-5">
            {STREAK_DAYS.map((d) => (
              <div key={d.day} className="flex flex-col items-center gap-1.5">
                <div
                  className={`relative size-11 rounded-full flex items-center justify-center text-[15px] font-medium ${
                    d.active
                      ? "bg-[#0E121B] text-white"
                      : d.faded
                        ? "bg-surface text-ink-faint"
                        : "bg-ink text-white"
                  }`}
                >
                  {d.active ? <ActivityIcon size={18} className="text-white" /> : (d.date ?? "")}
                  {!d.active && !d.faded && (
                    <span className="absolute -top-1 -right-1 size-4 rounded-full bg-[#12a70a] flex items-center justify-center ring-2 ring-white">
                      <CheckIcon size={9} className="text-white" />
                    </span>
                  )}
                </div>
                <span className={`text-[12px] ${d.faded ? "text-ink-faint" : "text-ink-muted"}`}>{d.day}</span>
              </div>
            ))}
          </div>
        </div>
        <div className="overflow-hidden mask-fade-x mt-4">
          <div className="flex gap-3 marquee-x" style={{ width: "max-content" }}>
            {[...copy.pills, ...copy.pills].map((label, i) => (
              <div
                key={i}
                className="flex items-center gap-2 px-4 py-2.5 rounded-full bg-white border border-line whitespace-nowrap flex-shrink-0"
              >
                <span
                  className="size-4 rounded-full flex-shrink-0"
                  style={{ backgroundColor: PILL_COLORS[i % PILL_COLORS.length] }}
                />
                <span className="text-[13px] font-medium text-ink">{label}</span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}

function DeliveryCard({ copy }: { copy: DeliveryCopy }) {
  return (
    <div className="relative overflow-hidden rounded-3xl border border-white/10 flex flex-col h-full">
      <Image src={ASSETS.features.plannerTexture} alt="" fill sizes="(max-width:768px) 100vw, 585px" className="object-cover" />
      <div className="absolute inset-0 bg-black/15" />
      <div className="relative z-10 flex flex-col h-full">
        <CardHead title={copy.title} description={copy.description} dark />
        <div className="px-6 pb-6 mt-auto">
          <div className="rounded-2xl bg-[#0a140c]/75 backdrop-blur-md border border-white/10 p-5">
            <div className="flex items-center justify-between mb-4">
              <div>
                <div className="text-[16px] font-medium text-white">{copy.header}</div>
                <div className="text-[12px] text-white/55">{copy.subheader}</div>
              </div>
              <div className="flex items-center gap-2">
                <span className="text-[18px] font-medium text-white">{copy.progressValue}</span>
                <span className="text-[12px] text-white/55">{copy.progressLabel}</span>
              </div>
            </div>
            <div className="overflow-hidden h-[196px] mask-fade-y">
              <div className="flex flex-col gap-2.5 marquee-y-slow">
                {[...copy.items, ...copy.items].map((h, i) => (
                  <div key={i} className="flex items-center gap-3 flex-shrink-0">
                    <div className="text-[12px] text-white w-12 leading-tight text-center font-medium">
                      <div>{h.time}</div>
                    </div>
                    <div className="flex-1 flex items-center justify-between rounded-xl px-3.5 py-3 bg-[#1a8029]">
                      <div className="flex items-center gap-2">
                        <span className="size-4 rounded-full bg-[#34c759] flex items-center justify-center flex-shrink-0">
                          <CheckIcon size={9} className="text-[#0a140c]" />
                        </span>
                        <span className="text-[14px] text-white font-medium">{h.title}</span>
                      </div>
                      <span className="text-[12px] text-white/70 flex-shrink-0">{h.meta}</span>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

const SECURITY_ICONS: Record<string, React.ReactNode> = {
  database: (
    <svg width="20" height="20" viewBox="0 0 20 20" fill="none" aria-hidden>
      <ellipse cx="10" cy="5" rx="7" ry="2.5" stroke="currentColor" strokeWidth="1.4" />
      <path d="M3 5v5c0 1.38 3.134 2.5 7 2.5S17 11.38 17 10V5" stroke="currentColor" strokeWidth="1.4" />
      <path d="M3 10v5c0 1.38 3.134 2.5 7 2.5S17 16.38 17 15v-5" stroke="currentColor" strokeWidth="1.4" />
    </svg>
  ),
  lock: (
    <svg width="20" height="20" viewBox="0 0 20 20" fill="none" aria-hidden>
      <rect x="4" y="9" width="12" height="9" rx="2" stroke="currentColor" strokeWidth="1.4" />
      <path d="M7 9V6.5a3 3 0 1 1 6 0V9" stroke="currentColor" strokeWidth="1.4" strokeLinecap="round" />
      <circle cx="10" cy="13.5" r="1.5" fill="currentColor" />
    </svg>
  ),
  bot: (
    <svg width="20" height="20" viewBox="0 0 20 20" fill="none" aria-hidden>
      <rect x="3" y="7" width="14" height="10" rx="2" stroke="currentColor" strokeWidth="1.4" />
      <path d="M7 11h.01M13 11h.01" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
      <path d="M10 3v4M7.5 7h5" stroke="currentColor" strokeWidth="1.4" strokeLinecap="round" />
      <path d="M3 13H1m16 0h2" stroke="currentColor" strokeWidth="1.4" strokeLinecap="round" />
    </svg>
  ),
  rate: (
    <svg width="20" height="20" viewBox="0 0 20 20" fill="none" aria-hidden>
      <circle cx="10" cy="10" r="7.5" stroke="currentColor" strokeWidth="1.4" />
      <path d="M10 6v4l2.5 2.5" stroke="currentColor" strokeWidth="1.4" strokeLinecap="round" strokeLinejoin="round" />
      <path d="M6 3.5L4 2M14 3.5l2-1.5" stroke="currentColor" strokeWidth="1.4" strokeLinecap="round" />
    </svg>
  ),
  gdpr: (
    <svg width="20" height="20" viewBox="0 0 20 20" fill="none" aria-hidden>
      <path d="M10 2L3.5 5v5c0 3.75 2.8 7.25 6.5 8 3.7-.75 6.5-4.25 6.5-8V5L10 2Z" stroke="currentColor" strokeWidth="1.4" strokeLinejoin="round" />
      <path d="M7 10l2 2 4-4" stroke="currentColor" strokeWidth="1.4" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  ),
  https: (
    <svg width="20" height="20" viewBox="0 0 20 20" fill="none" aria-hidden>
      <path d="M10 2C7.5 2 5 4 5 7v1H4a1 1 0 0 0-1 1v7a1 1 0 0 0 1 1h12a1 1 0 0 0 1-1V9a1 1 0 0 0-1-1h-1V7c0-3-2.5-5-5-5Z" stroke="currentColor" strokeWidth="1.4" />
      <circle cx="10" cy="13" r="1.5" fill="currentColor" />
      <path d="M10 14.5v2" stroke="currentColor" strokeWidth="1.4" strokeLinecap="round" />
    </svg>
  ),
};

function SecurityCard({ copy }: { copy: SecurityCopy }) {
  return (
    <div className="relative overflow-hidden rounded-3xl bg-[#0b0d12] border border-white/[0.07] md:col-span-2 p-8 md:p-10">
      {/* Subtle dot grid */}
      <div
        className="absolute inset-0 pointer-events-none opacity-[0.03]"
        style={{ backgroundImage: "radial-gradient(circle, #fff 1px, transparent 1px)", backgroundSize: "28px 28px" }}
      />
      {/* Accent glow */}
      <div className="absolute top-0 left-1/2 -translate-x-1/2 w-[600px] h-[200px] pointer-events-none"
        style={{ background: "radial-gradient(ellipse, rgba(255,76,0,0.1) 0%, transparent 70%)" }} />

      <div className="relative z-10">
        {/* Header */}
        <div className="mb-8 md:mb-10 max-w-xl">
          <h3 className="font-heading text-[26px] md:text-[30px] font-medium text-white">{copy.title}</h3>
          <p className="mt-2 text-[15px] text-white/60 leading-relaxed">{copy.description}</p>
        </div>

        {/* Feature grid */}
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4">
          {copy.features.map((f) => (
            <div
              key={f.icon}
              className="rounded-2xl border border-white/[0.07] bg-white/[0.03] p-5 flex flex-col gap-3 hover:bg-white/[0.06] transition-colors"
            >
              <div className="size-9 rounded-xl bg-white/[0.07] flex items-center justify-center text-white/70 flex-shrink-0">
                {SECURITY_ICONS[f.icon]}
              </div>
              <div>
                <div className="text-[14px] font-semibold text-white leading-snug">{f.title}</div>
                <div className="mt-1.5 text-[13px] text-white/50 leading-relaxed">{f.body}</div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

function SeoAnalyticsCard({ copy }: { copy: SeoAnalyticsCopy }) {
  const [first, second, third] = copy.rings;
  return (
    <div className="relative overflow-hidden rounded-3xl border border-white/10 bg-[radial-gradient(120%_120%_at_70%_0%,#23262b_0%,#131515_55%)] flex flex-col h-full">
      <CardHead title={copy.title} description={copy.description} dark />
      <div className="px-8 pb-8 mt-auto flex flex-col gap-3">
        <div className="rounded-2xl bg-black/30 border border-white/10 p-6">
          <div className="text-center text-[15px] font-medium text-white mb-5">{copy.summary}</div>

          {/* Mobile: 2 small on top row, 1 large centered below */}
          <div className="md:hidden">
            <div className="grid grid-cols-2 justify-items-center mb-4">
              {[first, third].map((d, i) => (
                <div key={d.label} className="flex flex-col items-center gap-2">
                  <AnimatedDonut value={d.value} color={RING_COLORS[i === 0 ? 0 : 2]} size={84} inset={7} />
                  <span className="text-[12px] text-white/60">{d.label}</span>
                </div>
              ))}
            </div>
            <div className="flex justify-center">
              <div className="flex flex-col items-center gap-2">
                <AnimatedDonut value={second.value} color={RING_COLORS[1]} size={130} inset={10} />
                <span className="text-[12px] text-white/60">{second.label}</span>
              </div>
            </div>
          </div>

          {/* Desktop: small | LARGE | small in a single row */}
          <div className="hidden md:flex items-end justify-center gap-6">
            <div className="flex flex-col items-center gap-2">
              <AnimatedDonut value={first.value} color={RING_COLORS[0]} size={84} inset={7} />
              <span className="text-[12px] text-white/60">{first.label}</span>
            </div>
            <div className="flex flex-col items-center gap-2">
              <AnimatedDonut value={second.value} color={RING_COLORS[1]} size={130} inset={10} />
              <span className="text-[12px] text-white/60">{second.label}</span>
            </div>
            <div className="flex flex-col items-center gap-2">
              <AnimatedDonut value={third.value} color={RING_COLORS[2]} size={84} inset={7} />
              <span className="text-[12px] text-white/60">{third.label}</span>
            </div>
          </div>
        </div>

        {/* Stats: stacked on mobile, 2-column grid on desktop */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
          {copy.stats.map((s, i) => (
            <div key={i} className="rounded-xl bg-white/[0.06] p-4">
              <div className="text-[13px] font-medium text-white">{s.label}</div>
              <div className="text-white text-[32px] font-medium leading-tight mt-1">{s.value}</div>
              <div className="text-[13px] mt-1 text-[#3ad24f]">{s.delta}</div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

function PortabilityCard({ copy }: { copy: PortabilityCopy }) {
  return (
    <div className="relative overflow-hidden rounded-3xl border border-white/10 bg-[#131515] flex flex-col h-full">
      <CardHead title={copy.title} description={copy.description} dark />
      <div className="relative md:flex-1 md:min-h-[460px] mt-auto overflow-hidden">
        {/* Scrolling API log background */}
        <div className="absolute inset-0 overflow-hidden pointer-events-none" style={{ maskImage: "linear-gradient(to bottom, transparent 0%, black 25%, black 75%, transparent 100%)" }}>
          <div className="flex flex-col gap-2 marquee-y-slow px-8 pt-4">
            {[...copy.bgItems, ...copy.bgItems].map((item, i) => (
              <div key={i} className="flex items-center gap-3 rounded-xl px-3.5 py-2.5 bg-white/[0.04] border border-white/[0.07] flex-shrink-0 opacity-60">
                <span
                  className="text-[10px] font-mono font-bold px-1.5 py-0.5 rounded flex-shrink-0"
                  style={{ color: item.method === "GET" ? "#3ad24f" : item.method === "POST" ? "#0059ff" : "#ff9800", background: item.method === "GET" ? "rgba(58,210,79,0.12)" : item.method === "POST" ? "rgba(0,89,255,0.12)" : "rgba(255,152,0,0.12)" }}
                >
                  {item.method}
                </span>
                <span className="text-[12px] text-white/40 font-mono truncate flex-1">{item.path}</span>
                <span className="text-[11px] text-[#3ad24f]/70 flex-shrink-0">{item.label}</span>
              </div>
            ))}
          </div>
        </div>

        {/* Notification card foreground */}
        <div className="relative z-10 flex justify-center items-center md:h-full px-8 pb-8 pt-4 md:pt-0">
          <div className="relative w-[360px] max-w-full rounded-[20px] bg-white/[0.06] backdrop-blur-[50px] border border-white/10 p-6 shadow-2xl">
            <Image
              src={ASSETS.features.bell}
              alt=""
              width={94}
              height={80}
              className="absolute -left-[26px] -top-[49px] w-[94px] h-auto drop-shadow-xl"
            />
            <div className="text-[12px] text-white/45">{copy.notification.label}</div>
            <div className="text-[20px] font-medium text-white mt-1 leading-tight">{copy.notification.title}</div>
            <div className="text-[14px] text-white/60 mt-1.5 leading-snug">{copy.notification.body}</div>
            <div className="mt-5 grid grid-cols-2 gap-3">
              <button className="py-3 rounded-2xl bg-white text-ink text-[15px] font-medium">{copy.notification.primary}</button>
              <button className="py-3 rounded-2xl bg-black/60 text-white text-[15px] font-medium">{copy.notification.secondary}</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
