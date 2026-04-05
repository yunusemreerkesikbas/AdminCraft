"use client";

import { type ReactNode } from "react";
import {
  motion,
  useReducedMotion,
  type Variant,
  type Transition,
} from "framer-motion";

const TRANSITION_BASE: Transition = {
  duration: 0.6,
  ease: [0.22, 1, 0.36, 1],
};

const VARIANTS: Record<string, { hidden: Variant; visible: Variant }> = {
  fadeUp: {
    hidden: { opacity: 0, y: 24 },
    visible: { opacity: 1, y: 0 },
  },
  fadeDown: {
    hidden: { opacity: 0, y: -24 },
    visible: { opacity: 1, y: 0 },
  },
  fadeLeft: {
    hidden: { opacity: 0, x: -32 },
    visible: { opacity: 1, x: 0 },
  },
  fadeRight: {
    hidden: { opacity: 0, x: 32 },
    visible: { opacity: 1, x: 0 },
  },
  scaleIn: {
    hidden: { opacity: 0, scale: 0.92, y: 16 },
    visible: { opacity: 1, scale: 1, y: 0 },
  },
  fade: {
    hidden: { opacity: 0 },
    visible: { opacity: 1 },
  },
};

type AnimateInViewProps = {
  children: ReactNode;
  className?: string;
  delay?: number;
  variant?: keyof typeof VARIANTS;
  once?: boolean;
  amount?: number;
  as?: "div" | "section" | "article" | "span";
};

export function AnimateInView({
  children,
  className = "",
  delay = 0,
  variant = "fadeUp",
  once = true,
  amount = 0.15,
  as = "div",
}: AnimateInViewProps) {
  const shouldReduce = useReducedMotion();
  const v = VARIANTS[variant] ?? VARIANTS.fadeUp;

  const Component = motion[as];

  if (shouldReduce) {
    const Tag = as;
    return <Tag className={className}>{children}</Tag>;
  }

  return (
    <Component
      initial="hidden"
      whileInView="visible"
      viewport={{ once, amount }}
      variants={v}
      transition={{ ...TRANSITION_BASE, delay: delay * 0.12 }}
      className={className}
    >
      {children}
    </Component>
  );
}

/* Stagger container — wrap children that each have AnimateInView */
export function StaggerContainer({
  children,
  className = "",
  staggerDelay = 0.08,
  as = "div",
}: {
  children: ReactNode;
  className?: string;
  staggerDelay?: number;
  as?: "div" | "section" | "ul";
}) {
  const shouldReduce = useReducedMotion();
  const Component = motion[as];

  if (shouldReduce) {
    const Tag = as;
    return <Tag className={className}>{children}</Tag>;
  }

  return (
    <Component
      initial="hidden"
      whileInView="visible"
      viewport={{ once: true, amount: 0.1 }}
      transition={{ staggerChildren: staggerDelay }}
      className={className}
    >
      {children}
    </Component>
  );
}

/* Motion item for use inside StaggerContainer */
export function StaggerItem({
  children,
  className = "",
  variant = "fadeUp",
}: {
  children: ReactNode;
  className?: string;
  variant?: keyof typeof VARIANTS;
}) {
  const shouldReduce = useReducedMotion();
  const v = VARIANTS[variant] ?? VARIANTS.fadeUp;

  if (shouldReduce) {
    return <div className={className}>{children}</div>;
  }

  return (
    <motion.div
      variants={v}
      transition={TRANSITION_BASE}
      className={className}
    >
      {children}
    </motion.div>
  );
}
