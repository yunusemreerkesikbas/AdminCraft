"use client";

import { useEffect, useRef, useState } from "react";

type Props = { value: number; color: string; size: number; inset: number; durationMs?: number };

export function AnimatedDonut({ value, color, size, inset, durationMs = 1400 }: Props) {
  const ref = useRef<HTMLDivElement | null>(null);
  const [pct, setPct] = useState(0);

  useEffect(() => {
    const el = ref.current;
    if (!el) return;
    const io = new IntersectionObserver(
      ([entry]) => {
        if (!entry.isIntersecting) return;
        io.disconnect();
        const start = performance.now();
        let raf = 0;
        const tick = (t: number) => {
          const p = Math.min(1, (t - start) / durationMs);
          const eased = 1 - Math.pow(1 - p, 3);
          setPct(value * eased);
          if (p < 1) raf = requestAnimationFrame(tick);
        };
        raf = requestAnimationFrame(tick);
        return () => cancelAnimationFrame(raf);
      },
      { threshold: 0.4 }
    );
    io.observe(el);
    return () => io.disconnect();
  }, [value, durationMs]);

  return (
    <div
      ref={ref}
      className="relative rounded-full"
      style={{ width: size, height: size, background: `conic-gradient(${color} ${pct}%, rgba(255,255,255,0.08) 0)` }}
    >
      <div className="absolute bg-[#131515] rounded-full flex items-center justify-center" style={{ inset }}>
        <span className={`text-white font-medium ${size >= 120 ? "text-[18px]" : "text-[14px]"}`}>{Math.round(pct)}%</span>
      </div>
    </div>
  );
}
