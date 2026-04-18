"use client";

import { useDemoContext } from "@/components/layout/DemoContext";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { FloatingOrbs } from "@/components/visuals/FloatingOrbs";
import { HeroVisual } from "@/components/visuals/HeroVisual";
import { motion, useReducedMotion } from "framer-motion";
import Link from "next/link";

type HeroContent = {
  badge: string;
  headline: string;
  subheadline: string;
  primaryCta: string;
  secondaryCta: string;
  mockupAlt: string;
  visual?: unknown;
};

type HeroProps = { content: HeroContent; locale: string };

const wordVariants = {
  hidden: { opacity: 0, y: 20, filter: "blur(8px)" },
  visible: { opacity: 1, y: 0, filter: "blur(0px)" },
};

export function Hero({ content, locale }: HeroProps) {
  const { openContact } = useDemoContext();
  const shouldReduce = useReducedMotion();
  const base = `/${locale}`;

  const words = content.headline.split(" ");

  return (
    <section className="relative overflow-hidden bg-[var(--color-light-neutral-1)]">
      {/* Animated gradient mesh background */}
      <div
        className="pointer-events-none absolute inset-0 animate-gradient-shift"
        style={{
          background:
            "radial-gradient(ellipse 60% 50% at 50% 20%, rgba(37,99,235,0.08) 0%, transparent 60%), radial-gradient(ellipse 40% 40% at 80% 60%, rgba(129,140,248,0.06) 0%, transparent 50%), radial-gradient(ellipse 50% 30% at 20% 80%, rgba(56,189,248,0.05) 0%, transparent 50%)",
          backgroundSize: "200% 200%",
        }}
        aria-hidden
      />

      {/* Dot pattern */}
      <div
        className="pointer-events-none absolute inset-0 opacity-[0.03]"
        style={{
          backgroundImage: "radial-gradient(circle, #000 1px, transparent 1px)",
          backgroundSize: "26px 26px",
        }}
        aria-hidden
      />

      <FloatingOrbs />

      {/* Bottom fade */}
      <div
        className="pointer-events-none absolute inset-x-0 bottom-0 h-24"
        style={{
          background:
            "linear-gradient(to bottom, rgba(255,255,255,0) 0%, rgba(247,248,250,0.88) 100%)",
        }}
        aria-hidden
      />

      <div className="relative mx-auto flex w-full max-w-[1440px] flex-col items-center gap-12 px-4 pb-10 pt-[112px] sm:px-10 sm:pb-16 lg:flex-row lg:items-center lg:justify-between lg:gap-16 lg:px-20 lg:pb-20 lg:pt-[138px]">
        {/* Text content */}
        <div className="relative z-10 flex w-full max-w-[620px] min-w-0 flex-col items-center gap-6 text-center lg:items-start lg:text-left">
          <motion.div
            initial={{ opacity: 0, y: 12 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5, ease: [0.22, 1, 0.36, 1] }}
          >
            <Badge
              variant="secondary"
              className="rounded-full border border-neutral-200 bg-white px-4 py-1.5 text-[0.72rem] font-semibold tracking-[-0.01em] text-neutral-600 shadow-[0_1px_0_rgba(255,255,255,0.7),0_8px_20px_-18px_rgba(15,23,42,0.35)]"
            >
              {content.badge}
            </Badge>
          </motion.div>

          <div className="flex flex-col items-center gap-4 lg:items-start">
            <h1 className="heading-hero max-w-[12ch] text-balance text-[var(--color-dark-neutral-1)] lg:text-[5rem] lg:leading-[0.94]">
              {shouldReduce ? (
                content.headline
              ) : (
                <motion.span
                  initial="hidden"
                  animate="visible"
                  transition={{ staggerChildren: 0.08, delayChildren: 0.15 }}
                  className="inline"
                >
                  {words.map((word, i) => (
                    <motion.span
                      key={i}
                      variants={wordVariants}
                      transition={{ duration: 0.5, ease: [0.22, 1, 0.36, 1] }}
                      className="inline-block mr-[0.25em]"
                    >
                      {word}
                    </motion.span>
                  ))}
                </motion.span>
              )}
            </h1>
            <motion.p
              className="max-w-[52ch] text-[1rem] leading-[1.75] text-neutral-600 sm:text-[1.05rem]"
              initial={{ opacity: 0, y: 16 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.6, delay: 0.5, ease: [0.22, 1, 0.36, 1] }}
            >
              {content.subheadline}
            </motion.p>
          </div>

          <motion.div
            className="flex flex-wrap items-center justify-center gap-3 lg:justify-start"
            initial={{ opacity: 0, y: 16 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6, delay: 0.65, ease: [0.22, 1, 0.36, 1] }}
          >
            <Button
              onClick={openContact}
              size="lg"
              className="min-h-11 rounded-[14px] bg-neutral-950 px-7 text-white shadow-[0_18px_30px_-18px_rgba(15,23,42,0.72)] transition-[background-color,box-shadow,transform] duration-200 hover:scale-[1.01] hover:bg-neutral-800 hover:shadow-[0_24px_40px_-22px_rgba(15,23,42,0.72)] active:scale-[0.98] motion-reduce:transform-none motion-reduce:transition-none"
            >
              {content.primaryCta}
            </Button>
            <Button
              asChild
              variant="outline"
              size="lg"
              className="min-h-11 rounded-[14px] border-neutral-200 bg-white px-6 text-neutral-700 shadow-[0_10px_24px_-20px_rgba(15,23,42,0.3)] transition-[background-color,border-color,color,box-shadow] duration-200 hover:border-neutral-300 hover:bg-neutral-50 hover:text-neutral-950 motion-reduce:transition-none"
            >
              <Link href={`${base}#features`}>{content.secondaryCta}</Link>
            </Button>
          </motion.div>
        </div>

        {/* Hero visual mockup */}
        <div className="relative z-10 hidden lg:block">
          <HeroVisual alt={content.mockupAlt} />
        </div>
      </div>
    </section>
  );
}
