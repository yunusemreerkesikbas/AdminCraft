"use client";

import { useEffect, useRef, useState, type ReactNode } from "react";

type AnimateInViewProps = {
  children: ReactNode;
  className?: string;
  delay?: 0 | 1 | 2 | 3;
  rootMargin?: string;
  variant?: "fadeUp" | "scaleIn";
};

export function AnimateInView({
  children,
  className = "",
  delay = 0,
  rootMargin = "0px 0px -40px 0px",
  variant = "fadeUp",
}: AnimateInViewProps) {
  const ref = useRef<HTMLDivElement>(null);
  const [inView, setInView] = useState(false);

  useEffect(() => {
    const el = ref.current;
    if (!el) return;

    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          setInView(true);
          observer.unobserve(entry.target);
        }
      },
      { rootMargin, threshold: 0.1 }
    );
    observer.observe(el);
    return () => observer.disconnect();
  }, [rootMargin]);

  const delayClass = delay === 0 ? "" : ` animate-in-view-delay-${delay}`;
  const variantClass = variant === "scaleIn" ? " animate-scale-in" : "";

  return (
    <div
      ref={ref}
      className={`animate-in-view${delayClass}${variantClass}${inView ? " is-in-view" : ""} ${className}`.trim()}
    >
      {children}
    </div>
  );
}
