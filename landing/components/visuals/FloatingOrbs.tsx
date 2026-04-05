"use client";

import { motion, useReducedMotion } from "framer-motion";

type Orb = {
  className: string;
  animate?: Record<string, number[]>;
  duration?: number;
};

const DEFAULT_ORBS: Orb[] = [
  {
    className:
      "absolute -top-20 -left-20 h-72 w-72 rounded-full bg-blue-500/[0.07] blur-3xl",
    animate: { x: [0, 40, -20, 0], y: [0, -30, 20, 0], scale: [1, 1.15, 0.95, 1] },
    duration: 14,
  },
  {
    className:
      "absolute top-1/3 -right-16 h-56 w-56 rounded-full bg-indigo-400/[0.06] blur-3xl",
    animate: { x: [0, -30, 20, 0], y: [0, 25, -15, 0], scale: [1, 0.9, 1.1, 1] },
    duration: 18,
  },
  {
    className:
      "absolute bottom-10 left-1/4 h-40 w-40 rounded-full bg-sky-400/[0.05] blur-3xl",
    animate: { x: [0, 25, -15, 0], y: [0, -20, 10, 0] },
    duration: 16,
  },
];

export function FloatingOrbs({
  orbs = DEFAULT_ORBS,
  className = "",
}: {
  orbs?: Orb[];
  className?: string;
}) {
  const shouldReduce = useReducedMotion();

  return (
    <div
      className={`pointer-events-none absolute inset-0 overflow-hidden ${className}`}
      aria-hidden="true"
    >
      {orbs.map((orb, i) =>
        shouldReduce ? (
          <div key={i} className={orb.className} />
        ) : (
          <motion.div
            key={i}
            className={orb.className}
            animate={orb.animate}
            transition={{
              duration: orb.duration ?? 14,
              repeat: Infinity,
              ease: "easeInOut",
            }}
          />
        ),
      )}
    </div>
  );
}
