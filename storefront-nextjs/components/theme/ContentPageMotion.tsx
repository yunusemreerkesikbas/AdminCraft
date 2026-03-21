"use client";

import { useEffect } from "react";

export default function ContentPageMotion() {
  useEffect(() => {
    const revealElements = Array.from(
      document.querySelectorAll<HTMLElement>("[data-reveal], [data-reveal-title], [data-reveal-bg]"),
    );
    const parallaxElements = Array.from(document.querySelectorAll<HTMLElement>("[data-parallax]"));

    if (revealElements.length === 0 && parallaxElements.length === 0) {
      return undefined;
    }

    const prefersReducedMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
    if (prefersReducedMotion) {
      revealElements.forEach((element) => {
        element.dataset.revealed = "true";
      });
      parallaxElements.forEach((element) => {
        element.style.setProperty("--parallax-shift", "0px");
      });
      return undefined;
    }

    let frameId = 0;

    const updateParallax = () => {
      const viewportHeight = window.innerHeight || 1;

      parallaxElements.forEach((element) => {
        const intensity = Number(element.dataset.parallax ?? "0");
        if (!intensity) {
          return;
        }

        const rect = element.getBoundingClientRect();
        const progress = (viewportHeight - rect.top) / (viewportHeight + rect.height);
        const normalized = Math.max(-1, Math.min(1, (progress - 0.5) * 2));
        element.style.setProperty("--parallax-shift", `${(normalized * intensity).toFixed(2)}px`);
      });

      frameId = 0;
    };

    const requestParallaxFrame = () => {
      if (frameId) {
        return;
      }

      frameId = window.requestAnimationFrame(updateParallax);
    };

    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (!entry.isIntersecting) {
            return;
          }

          entry.target.setAttribute("data-revealed", "true");
          observer.unobserve(entry.target);
        });
      },
      {
        threshold: 0.16,
        rootMargin: "0px 0px -12% 0px",
      },
    );

    revealElements.forEach((element) => observer.observe(element));
    updateParallax();
    window.addEventListener("scroll", requestParallaxFrame, { passive: true });
    window.addEventListener("resize", requestParallaxFrame);

    return () => {
      observer.disconnect();
      window.removeEventListener("scroll", requestParallaxFrame);
      window.removeEventListener("resize", requestParallaxFrame);
      if (frameId) {
        window.cancelAnimationFrame(frameId);
      }
    };
  }, []);

  return null;
}
