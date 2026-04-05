"use client";

import type { ReactNode } from "react";

type MarqueeTrackProps = {
  children: ReactNode;
  direction?: "left" | "right";
  speed?: number;
  className?: string;
};

export function MarqueeTrack({
  children,
  direction = "left",
  speed = 30,
  className = "",
}: MarqueeTrackProps) {
  const animClass =
    direction === "left" ? "animate-marquee-left" : "animate-marquee-right";

  return (
    <div
      className={`marquee-track relative overflow-hidden ${className}`}
      aria-hidden="true"
    >
      {/* Edge fades */}
      <div className="pointer-events-none absolute inset-y-0 left-0 z-10 w-16 bg-gradient-to-r from-white to-transparent sm:w-24" />
      <div className="pointer-events-none absolute inset-y-0 right-0 z-10 w-16 bg-gradient-to-l from-white to-transparent sm:w-24" />

      <div
        className={`flex w-max gap-8 ${animClass}`}
        style={{ animationDuration: `${speed}s` }}
      >
        {children}
        {/* Duplicate for seamless loop */}
        {children}
      </div>
    </div>
  );
}
