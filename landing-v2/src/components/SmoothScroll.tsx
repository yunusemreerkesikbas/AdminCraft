"use client";
import { useEffect } from "react";

export function SmoothScroll() {
  useEffect(() => {
    let frame: number;
    let lenis: { raf: (t: number) => void; destroy: () => void } | null = null;
    let mounted = true;

    (async () => {
      try {
        const Lenis = (await import("lenis")).default;
        if (!mounted) return;
        lenis = new Lenis({ duration: 1.2, easing: (t: number) => Math.min(1, 1.001 - Math.pow(2, -10 * t)) });
        const raf = (time: number) => {
          lenis?.raf(time);
          frame = requestAnimationFrame(raf);
        };
        frame = requestAnimationFrame(raf);
      } catch {
        // lenis not installed yet — fall back to native smooth scroll (set in globals.css)
      }
    })();

    return () => {
      mounted = false;
      if (frame) cancelAnimationFrame(frame);
      lenis?.destroy();
    };
  }, []);
  return null;
}
