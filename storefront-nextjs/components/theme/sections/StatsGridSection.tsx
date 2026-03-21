"use client";

import { useEffect, useRef, useState, type CSSProperties } from "react";
import type { StatItemModel, StatsGridModel } from "@/components/theme/models";
import styles from "../content-page.module.css";

function AnimatedStatValue({ item }: { item: StatItemModel }) {
  const valueRef = useRef<HTMLSpanElement>(null);
  const [displayValue, setDisplayValue] = useState(0);

  useEffect(() => {
    const element = valueRef.current;
    if (!element) {
      return undefined;
    }

    if (window.matchMedia("(prefers-reduced-motion: reduce)").matches) {
      const frameId = window.requestAnimationFrame(() => {
        setDisplayValue(item.value);
      });

      return () => {
        window.cancelAnimationFrame(frameId);
      };
    }

    let frameId = 0;
    let hasAnimated = false;

    const observer = new IntersectionObserver(
      (entries) => {
        if (!entries[0]?.isIntersecting || hasAnimated) {
          return;
        }

        hasAnimated = true;
        const startTime = performance.now();
        const duration = 1400;

        const animate = (timestamp: number) => {
          const progress = Math.min((timestamp - startTime) / duration, 1);
          const easedProgress = 1 - Math.pow(1 - progress, 3);
          setDisplayValue(Math.round(item.value * easedProgress));

          if (progress < 1) {
            frameId = window.requestAnimationFrame(animate);
          }
        };

        frameId = window.requestAnimationFrame(animate);
        observer.disconnect();
      },
      { threshold: 0.55 },
    );

    observer.observe(element);

    return () => {
      observer.disconnect();
      if (frameId) {
        window.cancelAnimationFrame(frameId);
      }
    };
  }, [item.value]);

  return (
    <span ref={valueRef} className={styles.statValue}>
      {displayValue}
      {item.suffix ?? ""}
    </span>
  );
}

export default function StatsGridSection({ model }: { model: StatsGridModel }) {
  return (
    <section className={styles.statsSection}>
      <div
        className={`${styles.statsTitleColumn} ${styles.contentReveal}`.trim()}
        data-reveal
        style={{ "--reveal-delay": "60ms" } as CSSProperties}
      >
        {model.subtitle ? <span className={styles.sectionLabel}>{model.subtitle}</span> : null}
        {model.title ? <h2 className={styles.statsTitle}>{model.title}</h2> : null}
      </div>

      <div className={styles.statsItemsColumn}>
        {model.items.map((item, index) => (
          <article
            key={item.id}
            className={`${styles.statRow} ${styles.contentReveal}`.trim()}
            data-reveal
            style={{ "--reveal-delay": `${120 + index * 80}ms` } as CSSProperties}
          >
            <AnimatedStatValue item={item} />
            <p className={styles.statLabel}>{item.label}</p>
          </article>
        ))}
      </div>
    </section>
  );
}
