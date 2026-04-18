"use client";

import {
  AnimateInView,
  StaggerContainer,
  StaggerItem,
} from "@/components/AnimateInView";
import { Badge } from "@/components/ui/badge";
import {
  motion,
  useReducedMotion,
  useScroll,
  useTransform,
} from "framer-motion";
import { Layers, LayoutDashboard, Monitor, Rocket, Server } from "lucide-react";
import type { ComponentType } from "react";
import { useRef } from "react";

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

type TimelineStep = {
  step: Step;
  stepNum: string;
  isTop: boolean;
};

const STEP_ICONS: Record<string, ComponentType<{ className?: string }>> = {
  tenant: LayoutDashboard,
  content: Layers,
  frontend: Monitor,
  deploy: Rocket,
};

function buildTimelineSteps(steps: Step[]): TimelineStep[] {
  return steps.map((step, index) => ({
    step,
    stepNum: String(index + 1).padStart(2, "0"),
    isTop: index % 2 === 0,
  }));
}

function StepCard({ item }: { item: TimelineStep }) {
  const { step, stepNum } = item;
  const Icon = STEP_ICONS[step.key] ?? Server;

  return (
    <div className="group relative w-full max-w-[320px] overflow-hidden rounded-[28px] border border-white/10 bg-[linear-gradient(180deg,rgba(15,23,42,0.82)_0%,rgba(10,15,30,0.9)_100%)] p-6 shadow-[0_28px_80px_-48px_rgba(0,0,0,0.78)] backdrop-blur-sm transition-[border-color,box-shadow,background-color] duration-300 motion-reduce:transition-none hover:border-[#3b82f6]/40 hover:bg-[linear-gradient(180deg,rgba(15,23,42,0.9)_0%,rgba(10,15,30,0.96)_100%)] hover:shadow-[0_40px_100px_-56px_rgba(59,130,246,0.34)]">
      <div
        className="pointer-events-none absolute -left-10 top-0 h-28 w-28 rounded-full bg-[#60a5fa]/10 blur-2xl opacity-0 transition-[opacity,transform] duration-300 motion-reduce:transition-none group-hover:translate-x-4 group-hover:opacity-100"
        aria-hidden="true"
      />
      <div
        className="pointer-events-none absolute inset-x-6 bottom-0 h-px bg-[linear-gradient(90deg,rgba(96,165,250,0)_0%,rgba(96,165,250,0.75)_50%,rgba(96,165,250,0)_100%)] opacity-0 transition-opacity duration-300 motion-reduce:transition-none group-hover:opacity-100"
        aria-hidden="true"
      />
      <div
        className="pointer-events-none absolute inset-x-0 top-0 h-px opacity-80"
        style={{
          background:
            "linear-gradient(90deg, rgba(59,130,246,0) 0%, rgba(96,165,250,0.95) 50%, rgba(59,130,246,0) 100%)",
        }}
        aria-hidden="true"
      />
      <div
        className="pointer-events-none absolute right-5 top-3 select-none font-heading text-8xl font-black leading-none text-white/[0.05]"
        aria-hidden="true"
      >
        {stepNum}
      </div>

      <div className="relative flex flex-col gap-5">
        <div
          className="flex h-14 w-14 items-center justify-center rounded-[20px] border border-[#60a5fa]/20 bg-[linear-gradient(135deg,rgba(37,99,235,0.28)_0%,rgba(29,78,216,0.12)_100%)] shadow-[0_14px_32px_-20px_rgba(37,99,235,0.55)] transition-[transform,border-color,box-shadow] duration-300 motion-reduce:transition-none group-hover:-translate-y-0.5 group-hover:border-[#93c5fd]/32 group-hover:shadow-[0_22px_40px_-20px_rgba(37,99,235,0.68)]"
          aria-hidden="true"
        >
          <Icon className="h-6 w-6 text-[#93c5fd] transition-transform duration-300 motion-reduce:transition-none group-hover:-rotate-3" />
        </div>

        <div className="space-y-3">
          <h3 className="font-heading text-lg font-bold tracking-tight text-white transition-colors duration-300 motion-reduce:transition-none group-hover:text-[#dbeafe]">
            {step.title}
          </h3>
          <p className="text-sm leading-7 text-white/62 transition-colors duration-300 motion-reduce:transition-none group-hover:text-white/74">
            {step.description}
          </p>
        </div>
      </div>
    </div>
  );
}

function ScrollDrivenProgressNode({
  index,
  total,
}: {
  index: number;
  total: number;
}) {
  const shouldReduce = useReducedMotion();

  return (
    <div className="relative flex items-center justify-center">
      <div
        className="pointer-events-none absolute h-16 w-16 rounded-full border border-[#93c5fd]/10 bg-[radial-gradient(circle,rgba(96,165,250,0.12)_0%,rgba(96,165,250,0.03)_45%,transparent_72%)]"
        aria-hidden="true"
      />
      <div
        className="pointer-events-none absolute h-11 w-11 rounded-full border border-[#60a5fa]/18 bg-[#081225]/92 shadow-[0_0_0_10px_rgba(15,23,42,0.42)]"
        aria-hidden="true"
      />
      <motion.div
        className="pointer-events-none absolute h-6 w-6 rounded-full border border-[#bfdbfe]/18 bg-[#60a5fa]/22"
        animate={
          shouldReduce
            ? {}
            : {
                boxShadow: [
                  "0 0 16px rgba(96,165,250,0.3)",
                  "0 0 32px rgba(96,165,250,0.6)",
                  "0 0 16px rgba(96,165,250,0.3)",
                ],
              }
        }
        transition={{ duration: 2.5, repeat: Infinity, delay: index * 0.4 }}
        aria-hidden="true"
      />
      <span
        className="h-1.5 w-1.5 rounded-full bg-[#7dd3fc] shadow-[0_0_10px_rgba(125,211,252,0.9)]"
        aria-hidden="true"
      />
    </div>
  );
}

function DesktopTimeline({ items }: { items: TimelineStep[] }) {
  const shouldReduce = useReducedMotion();
  const sectionRef = useRef<HTMLDivElement>(null);
  const { scrollYProgress } = useScroll({
    target: sectionRef,
    offset: ["start 80%", "end 40%"],
  });
  const lineWidth = useTransform(scrollYProgress, [0, 1], ["0%", "100%"]);

  return (
    <div className="relative mt-20 hidden lg:block" ref={sectionRef}>
      {/* Static line (background) */}
      <div
        className="pointer-events-none absolute left-[7%] right-[7%] top-1/2 h-px -translate-y-1/2 bg-[rgba(96,165,250,0.15)]"
        aria-hidden="true"
      />
      {/* Animated progress line */}
      {!shouldReduce && (
        <motion.div
          className="pointer-events-none absolute left-[7%] top-1/2 h-px -translate-y-1/2 bg-[linear-gradient(90deg,rgba(96,165,250,0.65)_0%,rgba(125,211,252,0.85)_50%,rgba(96,165,250,0.65)_100%)]"
          style={{ width: lineWidth }}
          aria-hidden="true"
        />
      )}
      <div
        className="pointer-events-none absolute left-[10%] right-[10%] top-1/2 h-24 -translate-y-1/2 blur-3xl"
        style={{
          background:
            "radial-gradient(circle at center, rgba(59,130,246,0.22) 0%, rgba(59,130,246,0.08) 40%, transparent 75%)",
        }}
        aria-hidden="true"
      />

      <StaggerContainer
        className="grid min-h-[34rem] grid-cols-4 gap-6"
        staggerDelay={0.15}
      >
        {items.map((item, index) => (
          <StaggerItem key={item.step.key} variant="fadeUp" className="h-full">
            <div className="grid h-full grid-rows-[1fr_auto_1fr] justify-items-center">
              {item.isTop ? (
                <div className="flex h-full flex-col items-center justify-end">
                  <StepCard item={item} />
                  <div
                    className="h-16 w-px bg-[linear-gradient(180deg,rgba(96,165,250,0)_0%,rgba(96,165,250,0.82)_100%)]"
                    aria-hidden="true"
                  />
                </div>
              ) : (
                <div />
              )}
              <ScrollDrivenProgressNode index={index} total={items.length} />
              {!item.isTop ? (
                <div className="flex h-full flex-col items-center justify-start">
                  <div
                    className="h-16 w-px bg-[linear-gradient(180deg,rgba(96,165,250,0.82)_0%,rgba(96,165,250,0)_100%)]"
                    aria-hidden="true"
                  />
                  <StepCard item={item} />
                </div>
              ) : (
                <div />
              )}
            </div>
          </StaggerItem>
        ))}
      </StaggerContainer>
    </div>
  );
}

function MobileTimeline({ items }: { items: TimelineStep[] }) {
  const shouldReduce = useReducedMotion();
  const sectionRef = useRef<HTMLDivElement>(null);
  const { scrollYProgress } = useScroll({
    target: sectionRef,
    offset: ["start 80%", "end 50%"],
  });
  const lineHeight = useTransform(scrollYProgress, [0, 1], ["0%", "100%"]);

  return (
    <div className="relative mt-14 lg:hidden" ref={sectionRef}>
      {/* Static line */}
      <div
        className="pointer-events-none absolute left-[27px] top-6 bottom-6 w-px bg-[rgba(96,165,250,0.15)]"
        aria-hidden="true"
      />
      {/* Animated progress line */}
      {!shouldReduce && (
        <motion.div
          className="pointer-events-none absolute left-[27px] top-6 w-px bg-[linear-gradient(180deg,rgba(96,165,250,0.72)_0%,rgba(125,211,252,0.82)_50%,rgba(96,165,250,0.72)_100%)]"
          style={{ height: lineHeight }}
          aria-hidden="true"
        />
      )}

      <StaggerContainer className="space-y-5" staggerDelay={0.12}>
        {items.map((item) => (
          <StaggerItem key={item.step.key} variant="fadeUp">
            <div className="relative pl-16">
              <div
                className="absolute left-[15px] top-8 flex h-6 w-6 items-center justify-center rounded-full bg-[#60a5fa] shadow-[0_0_24px_rgba(96,165,250,0.65)]"
                aria-hidden="true"
              >
                <div className="h-2.5 w-2.5 rounded-full bg-white" />
              </div>
              <StepCard item={item} />
            </div>
          </StaggerItem>
        ))}
      </StaggerContainer>
    </div>
  );
}

export function HowItWorks({ content }: { content: HowItWorksContent }) {
  const timelineSteps = buildTimelineSteps(content.steps);

  return (
    <section
      id="howitworks"
      className="relative overflow-hidden px-4 py-24 sm:px-6 lg:px-20"
      style={{
        background:
          "linear-gradient(180deg, #01050d 0%, #071121 34%, #0b1730 68%, #020611 100%)",
      }}
    >
      <div
        className="pointer-events-none absolute inset-0"
        style={{
          background:
            "radial-gradient(ellipse 68% 44% at 50% 0%, rgba(59,130,246,0.16) 0%, rgba(29,78,216,0.08) 34%, transparent 72%)",
        }}
        aria-hidden="true"
      />
      <div
        className="pointer-events-none absolute inset-0 opacity-[0.04]"
        style={{
          backgroundImage:
            "radial-gradient(circle, #ffffff 1px, transparent 1px)",
          backgroundSize: "28px 28px",
        }}
        aria-hidden="true"
      />
      <div
        className="pointer-events-none absolute inset-x-0 top-0 h-32 bg-[linear-gradient(180deg,rgba(147,197,253,0.06)_0%,rgba(147,197,253,0)_100%)]"
        aria-hidden="true"
      />

      <div className="relative mx-auto max-w-[1440px]">
        <div className="flex flex-col items-center gap-4 text-center">
          <AnimateInView>
            <Badge className="rounded-full border border-white/10 bg-white/8 px-4 py-1 text-xs font-semibold text-white/72 backdrop-blur-sm">
              {content.sectionTag}
            </Badge>
          </AnimateInView>
          <AnimateInView delay={1}>
            <h2 className="font-display max-w-2xl text-3xl text-white sm:text-4xl lg:text-5xl">
              {content.heading}
            </h2>
          </AnimateInView>
          <AnimateInView delay={2}>
            <p className="max-w-2xl text-white/54 sm:text-base">
              {content.subheading}
            </p>
          </AnimateInView>
        </div>

        <DesktopTimeline items={timelineSteps} />
        <MobileTimeline items={timelineSteps} />
      </div>
    </section>
  );
}
