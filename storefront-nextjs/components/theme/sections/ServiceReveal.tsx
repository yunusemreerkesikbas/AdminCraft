"use client";

import type { CSSProperties, ElementType, ReactNode } from "react";
import { useEffect, useRef, useState } from "react";
import styles from "../service-page.module.css";

type RevealDirection = "up" | "left" | "right";

type ServiceRevealProps = {
  as?: ElementType;
  children: ReactNode;
  className?: string;
  delayMs?: number;
  direction?: RevealDirection;
  style?: CSSProperties;
  once?: boolean;
};

const directionClassMap: Record<RevealDirection, string> = {
  up: styles.revealUp,
  left: styles.revealLeft,
  right: styles.revealRight,
};

export default function ServiceReveal({
  as,
  children,
  className = "",
  delayMs = 0,
  direction = "up",
  style,
  once = true,
}: ServiceRevealProps) {
  const Component = (as ?? "div") as ElementType;
  const ref = useRef<HTMLElement | null>(null);
  const [visible, setVisible] = useState(false);

  useEffect(() => {
    const node = ref.current;
    if (!node) {
      return undefined;
    }

    if (window.matchMedia("(prefers-reduced-motion: reduce)").matches) {
      const frameId = window.requestAnimationFrame(() => {
        setVisible(true);
      });

      return () => {
        window.cancelAnimationFrame(frameId);
      };
    }

    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            setVisible(true);
            if (once) {
              observer.unobserve(entry.target);
            }
          } else if (!once) {
            setVisible(false);
          }
        });
      },
      {
        threshold: 0.18,
        rootMargin: "0px 0px -10% 0px",
      },
    );

    observer.observe(node);
    return () => observer.disconnect();
  }, [once]);

  return (
    <Component
      ref={ref}
      className={[
        styles.revealBase,
        directionClassMap[direction],
        visible ? styles.revealVisible : "",
        className,
      ]
        .filter(Boolean)
        .join(" ")}
      style={{
        "--reveal-delay": `${delayMs}ms`,
        ...style,
      } as CSSProperties}
    >
      {children}
    </Component>
  );
}
