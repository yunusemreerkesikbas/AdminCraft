"use client";

import { motion, useMotionValue, useSpring, useTransform, useReducedMotion } from "framer-motion";
import type { MouseEvent } from "react";

export function HeroVisual({ alt }: { alt: string }) {
  const shouldReduce = useReducedMotion();

  const x = useMotionValue(0);
  const y = useMotionValue(0);

  const rotateX = useSpring(useTransform(y, [-200, 200], [5, -5]), {
    stiffness: 120,
    damping: 20,
  });
  const rotateY = useSpring(useTransform(x, [-200, 200], [-5, 5]), {
    stiffness: 120,
    damping: 20,
  });

  function handleMouse(e: MouseEvent<HTMLDivElement>) {
    if (shouldReduce) return;
    const rect = e.currentTarget.getBoundingClientRect();
    x.set(e.clientX - rect.left - rect.width / 2);
    y.set(e.clientY - rect.top - rect.height / 2);
  }

  function handleLeave() {
    x.set(0);
    y.set(0);
  }

  return (
    <motion.div
      className="relative w-full max-w-[min(1280px,100%)] perspective-[1400px]"
      initial={{ opacity: 0, y: 40, scale: 0.95 }}
      animate={{ opacity: 1, y: 0, scale: 1 }}
      transition={{ duration: 0.8, delay: 0.3, ease: [0.22, 1, 0.36, 1] }}
      onMouseMove={handleMouse}
      onMouseLeave={handleLeave}
    >
      <motion.div
        className="relative overflow-hidden rounded-2xl border border-neutral-200/60 bg-white shadow-[0_40px_100px_-28px_rgba(15,23,42,0.3)]"
        style={shouldReduce ? {} : { rotateX, rotateY }}
      >
        <div className="relative max-h-[min(760px,84vh)] overflow-hidden sm:max-h-[min(860px,88vh)]">
          <img
            src="/images/hero-2.png"
            alt={alt}
            className="block w-full"
            loading="eager"
          />
          {/* Bottom fade - masks the cut-off edge */}
          <div
            className="pointer-events-none absolute inset-x-0 bottom-0 h-20"
            style={{
              background: "linear-gradient(to bottom, rgba(255,255,255,0) 0%, rgba(255,255,255,0.95) 100%)",
            }}
          />
        </div>

        {/* Subtle glow overlay */}
        <div
          className="pointer-events-none absolute inset-0 opacity-15"
          style={{
            background:
              "radial-gradient(circle at 30% 20%, rgba(37,99,235,0.12) 0%, transparent 50%)",
          }}
        />
      </motion.div>

      {/* Shadow/reflection */}
      <div
        className="pointer-events-none absolute -bottom-5 left-[8%] right-[8%] h-10 rounded-full bg-black/[0.08] blur-xl"
        aria-hidden="true"
      />
    </motion.div>
  );
}
