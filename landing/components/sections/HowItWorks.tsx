import { Badge } from "@/components/ui/badge";
import { ChevronRight, LayoutDashboard, Layers, Monitor, Rocket, Server } from "lucide-react";
import { AnimateInView } from "@/components/AnimateInView";

type Step = {
  key: string;
  icon: string;
  title: string;
  description: string;
};

type HowItWorksContent = {
  sectionTag: string;
  heading: string;
  subheading: string;
  steps: Step[];
};

const STEP_ICONS: Record<string, React.ComponentType<{ className?: string }>> = {
  tenant: LayoutDashboard,
  content: Layers,
  frontend: Monitor,
  deploy: Rocket,
};

export function HowItWorks({ content }: { content: HowItWorksContent }) {
  return (
    <section
      id="howitworks"
      className="relative overflow-hidden px-4 py-24 sm:px-6 lg:px-20"
      style={{ background: "linear-gradient(160deg, #000000 0%, #060f22 40%, #0a1530 70%, #000000 100%)" }}
    >
      {/* Blue radial glow */}
      <div
        className="pointer-events-none absolute inset-0"
        style={{
          background:
            "radial-gradient(ellipse 70% 50% at 50% 0%, rgba(37,99,235,0.12) 0%, transparent 65%)",
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

      <div className="relative mx-auto max-w-[1440px]">
        {/* Header */}
        <div className="flex flex-col items-center gap-4 text-center">
          <AnimateInView>
            <Badge className="rounded-full border border-white/10 bg-white/10 px-4 py-1 text-xs font-semibold text-white/70 backdrop-blur-sm">
              {content.sectionTag}
            </Badge>
          </AnimateInView>
          <AnimateInView className="animate-in-view-delay-1">
            <h2 className="font-heading max-w-xl text-3xl font-bold tracking-tight text-white sm:text-4xl">
              {content.heading}
            </h2>
          </AnimateInView>
          <AnimateInView className="animate-in-view-delay-2">
            <p className="max-w-xl text-white/50">{content.subheading}</p>
          </AnimateInView>
        </div>

        {/* Step cards */}
        <AnimateInView className="mt-16 animate-in-view-delay-2">
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
            {content.steps.map((step, index) => {
              const Icon = STEP_ICONS[step.key] ?? Server;
              const stepNum = String(index + 1).padStart(2, "0");
              const isLast = index === content.steps.length - 1;

              return (
                <div key={step.key} className="relative">
                  {/* Connector */}
                  {!isLast && (
                    <div className="absolute -right-3 top-1/2 z-10 hidden -translate-y-1/2 lg:flex h-6 w-6 items-center justify-center rounded-full bg-white/5 ring-1 ring-white/10">
                      <ChevronRight className="h-3.5 w-3.5 text-white/30" />
                    </div>
                  )}

                  <div className="group relative flex h-full flex-col gap-6 overflow-hidden rounded-2xl bg-white/[0.04] p-7 ring-1 ring-white/10 transition-all duration-300 hover:bg-white/[0.07] hover:ring-[var(--color-theme-3)]/40">
                    {/* Accent top line */}
                    <div
                      className="absolute inset-x-0 top-0 h-px"
                      style={{
                        background: "linear-gradient(90deg, var(--color-theme-3) 0%, #1D4ED8 100%)",
                        opacity: 0.7,
                      }}
                    />

                    {/* Ghost step number */}
                    <span
                      className="pointer-events-none absolute right-5 top-3 select-none font-heading text-8xl font-black leading-none text-white/[0.05]"
                      aria-hidden
                    >
                      {stepNum}
                    </span>

                    {/* Icon */}
                    <div
                      className="relative flex h-14 w-14 items-center justify-center rounded-2xl transition-transform duration-300 group-hover:scale-105"
                      style={{
                        background: "linear-gradient(135deg, rgba(37,99,235,0.25) 0%, rgba(29,78,216,0.15) 100%)",
                        boxShadow: "0 0 0 1px rgba(37,99,235,0.25), 0 8px 24px rgba(37,99,235,0.15)",
                      }}
                    >
                      <Icon className="h-6 w-6 text-[#93c5fd]" />
                    </div>

                    {/* Text */}
                    <div className="relative flex flex-col gap-2">
                      <div className="flex items-center gap-2.5">
                        <span
                          className="flex h-5 w-5 shrink-0 items-center justify-center rounded-md text-[9px] font-black text-white"
                          style={{
                            background: "linear-gradient(135deg, var(--color-theme-3) 0%, #1D4ED8 100%)",
                          }}
                        >
                          {stepNum}
                        </span>
                        <h3 className="font-heading text-base font-bold text-white">
                          {step.title}
                        </h3>
                      </div>
                      <p className="text-sm leading-relaxed text-white/45">
                        {step.description}
                      </p>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        </AnimateInView>
      </div>
    </section>
  );
}
