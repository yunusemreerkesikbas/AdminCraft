"use client";

import { useEffect } from "react";
import styles from "./theme.module.css";

const clamp = (value: number, min: number, max: number): number =>
  Math.min(max, Math.max(min, value));

const lerp = (start: number, end: number, progress: number): number =>
  start + (end - start) * progress;

const getPinnedProgress = (element: HTMLElement): number => {
  const range = Math.max(element.offsetHeight - window.innerHeight, 1);
  return clamp(-element.getBoundingClientRect().top / range, 0, 1);
};

export default function Motion() {
  useEffect(() => {
    const root = document.documentElement;
    root.dataset.homeTwoMotion = "true";

    const prefersReducedMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
    const revealTargets = [
      document.querySelector(`.${styles.aboutArea}`),
      document.querySelector(`.${styles.videoArea}`),
      document.querySelector(`.${styles.serviceArea}`),
      document.querySelector(`.${styles.awardArea}`),
      document.querySelector(`.${styles.instagramArea}`),
    ].filter(Boolean) as HTMLElement[];

    if (prefersReducedMotion) {
      revealTargets.forEach((element) => {
        element.dataset.visible = "true";
      });
      return () => {
        delete root.dataset.homeTwoMotion;
      };
    }

    const observer = new IntersectionObserver(
      (entries, currentObserver) => {
        entries.forEach((entry) => {
          if (!entry.isIntersecting) {
            return;
          }

          (entry.target as HTMLElement).dataset.visible = "true";
          currentObserver.unobserve(entry.target);
        });
      },
      { threshold: 0.24, rootMargin: "0px 0px -12% 0px" }
    );

    revealTargets.forEach((element) => observer.observe(element));

    const videoArea = document.querySelector(`.${styles.videoArea}`) as HTMLElement | null;
    const videoWrap = document.querySelector(`.${styles.videoWrap}`) as HTMLElement | null;
    const videoContainer = videoArea?.querySelector(`.${styles.heroContainer}`) as HTMLElement | null;

    const aboutArea = document.querySelector(`.${styles.aboutArea}`) as HTMLElement | null;

    const projectArea = document.querySelector(`.${styles.projectArea}`) as HTMLElement | null;
    const projectViewport = projectArea?.querySelector(`.${styles.projectViewport}`) as HTMLElement | null;
    const projectStrip = projectArea?.querySelector(`.${styles.projectStrip}`) as HTMLElement | null;

    const awardArea = document.querySelector(`.${styles.awardArea}`) as HTMLElement | null;

    const instagramArea = document.querySelector(`.${styles.instagramArea}`) as HTMLElement | null;
    const instagramStage = instagramArea?.querySelector(`.${styles.instagramStage}`) as HTMLElement | null;

    const cleanups: Array<() => void> = [];
    const circleButtons = Array.from(
      document.querySelectorAll(`.${styles.circleButton}`)
    ) as HTMLElement[];

    circleButtons.forEach((button) => {
      const dot = button.querySelector(`.${styles.circleButtonDot}`) as HTMLElement | null;

      const handleMove = (event: MouseEvent) => {
        const rect = button.getBoundingClientRect();
        const offsetX = event.clientX - rect.left;
        const offsetY = event.clientY - rect.top;
        const translateX = ((offsetX - rect.width / 2) / rect.width) * 26;
        const translateY = ((offsetY - rect.height / 2) / rect.height) * 26;

        button.style.transform = `translate3d(${translateX}px, ${translateY}px, 0)`;

        if (dot) {
          dot.style.left = `${offsetX}px`;
          dot.style.top = `${offsetY}px`;
        }
      };

      const handleLeave = () => {
        button.style.transform = "translate3d(0, 0, 0)";
      };

      button.addEventListener("mousemove", handleMove);
      button.addEventListener("mouseenter", handleMove);
      button.addEventListener("mouseleave", handleLeave);

      cleanups.push(() => {
        button.removeEventListener("mousemove", handleMove);
        button.removeEventListener("mouseenter", handleMove);
        button.removeEventListener("mouseleave", handleLeave);
      });
    });

    const measure = () => {
      if (videoArea && videoWrap && videoContainer) {
        const active = window.innerWidth >= 1400;
        videoArea.dataset.motionPinned = active ? "true" : "false";

        if (active) {
          const startWidth = Math.max(videoContainer.clientWidth - 30, 320);
          const endWidth = Math.min(1110, startWidth);
          const startHeight = clamp(Math.round(window.innerHeight * 0.82), 640, 850);
          const endHeight = Math.min(560, startHeight);

          videoArea.dataset.startWidth = `${startWidth}`;
          videoArea.dataset.endWidth = `${endWidth}`;
          videoArea.dataset.startHeight = `${startHeight}`;
          videoArea.dataset.endHeight = `${endHeight}`;
          videoArea.style.setProperty(
            "--video-section-height",
            `${Math.round(window.innerHeight + Math.max(startHeight - endHeight, 0) + 360)}px`
          );
        } else {
          delete videoArea.dataset.startWidth;
          delete videoArea.dataset.endWidth;
          delete videoArea.dataset.startHeight;
          delete videoArea.dataset.endHeight;
          videoArea.style.removeProperty("--video-section-height");
          videoWrap.style.removeProperty("width");
          videoWrap.style.removeProperty("height");
        }
      }

      if (projectArea && projectViewport && projectStrip) {
        const active = window.innerWidth >= 1200;
        projectArea.dataset.motionPinned = active ? "true" : "false";

        if (active) {
          const maxOffset = Math.max(projectStrip.scrollWidth - projectViewport.clientWidth, 0);
          projectArea.dataset.maxOffset = `${maxOffset}`;
          projectArea.style.setProperty(
            "--project-scroll-height",
            `${Math.round(window.innerHeight + maxOffset)}px`
          );
        } else {
          delete projectArea.dataset.maxOffset;
          projectArea.style.removeProperty("--project-scroll-height");
          projectStrip.style.removeProperty("transform");
        }
      }

      if (instagramArea && instagramStage) {
        const active = window.innerWidth >= 1200;
        instagramArea.dataset.motionPinned = active ? "true" : "false";

        if (active) {
          instagramArea.style.setProperty(
            "--instagram-section-height",
            `${Math.round(window.innerHeight * 1.8)}px`
          );
          const startWidth = Math.min(window.innerWidth - 32, 1920);
          const startHeight = Math.min(window.innerHeight - 32, 950);
          const endWidth = Math.min(580, window.innerWidth - 32);
          const endHeight = Math.min(580, window.innerHeight - 32);

          instagramArea.dataset.instagramStartWidth = `${startWidth}`;
          instagramArea.dataset.instagramEndWidth = `${endWidth}`;
          instagramArea.dataset.instagramStartHeight = `${startHeight}`;
          instagramArea.dataset.instagramEndHeight = `${endHeight}`;
        } else {
          delete instagramArea.dataset.instagramStartWidth;
          delete instagramArea.dataset.instagramEndWidth;
          delete instagramArea.dataset.instagramStartHeight;
          delete instagramArea.dataset.instagramEndHeight;
          instagramArea.style.removeProperty("--instagram-section-height");
          instagramArea.style.removeProperty("--instagram-stage-width");
          instagramArea.style.removeProperty("--instagram-stage-height");
          instagramArea.style.removeProperty("--instagram-progress");
        }
      }
    };

    let animationFrame = 0;

    const update = () => {
      animationFrame = 0;

      if (videoArea && videoWrap && videoArea.dataset.motionPinned === "true") {
        const progress = getPinnedProgress(videoArea);
        const startWidth = Number(videoArea.dataset.startWidth ?? 0);
        const endWidth = Number(videoArea.dataset.endWidth ?? 0);
        const startHeight = Number(videoArea.dataset.startHeight ?? 0);
        const endHeight = Number(videoArea.dataset.endHeight ?? 0);

        videoWrap.style.width = `${lerp(startWidth, endWidth, progress)}px`;
        videoWrap.style.height = `${lerp(startHeight, endHeight, progress)}px`;
      }

      if (aboutArea) {
        const progress = clamp(
          (window.innerHeight - aboutArea.getBoundingClientRect().top) /
            (window.innerHeight + aboutArea.offsetHeight * 0.2),
          0,
          1
        );

        aboutArea.style.setProperty("--about-main-rotation", `${lerp(0, -10, progress).toFixed(3)}deg`);
        aboutArea.style.setProperty("--about-inner-rotation", `${lerp(0, 12, progress).toFixed(3)}deg`);
      }

      if (projectArea && projectStrip && projectArea.dataset.motionPinned === "true") {
        const progress = getPinnedProgress(projectArea);
        const maxOffset = Number(projectArea.dataset.maxOffset ?? 0);
        projectStrip.style.transform = `translate3d(-${maxOffset * progress}px, 0, 0)`;
      }

      if (awardArea) {
        const progress = clamp(
          (window.innerHeight - awardArea.getBoundingClientRect().top) /
            (window.innerHeight + awardArea.offsetHeight * 0.45),
          0,
          1
        );
        awardArea.style.setProperty("--award-progress", progress.toFixed(4));
      }

      if (instagramArea && instagramArea.dataset.motionPinned === "true") {
        const progress = getPinnedProgress(instagramArea);
        const startWidth = Number(instagramArea.dataset.instagramStartWidth ?? 1200);
        const endWidth = Number(instagramArea.dataset.instagramEndWidth ?? 580);
        const startHeight = Number(instagramArea.dataset.instagramStartHeight ?? 900);
        const endHeight = Number(instagramArea.dataset.instagramEndHeight ?? 580);
        instagramArea.style.setProperty("--instagram-progress", progress.toFixed(4));
        instagramArea.style.setProperty(
          "--instagram-stage-width",
          `${lerp(startWidth, endWidth, progress)}px`
        );
        instagramArea.style.setProperty(
          "--instagram-stage-height",
          `${lerp(startHeight, endHeight, progress)}px`
        );
      }
    };

    const requestUpdate = () => {
      if (animationFrame) {
        return;
      }

      animationFrame = window.requestAnimationFrame(update);
    };

    const handleResize = () => {
      measure();
      requestUpdate();
    };

    const resizeObserver =
      typeof ResizeObserver === "undefined"
        ? null
        : new ResizeObserver(() => {
            measure();
            requestUpdate();
          });

    [videoContainer, projectViewport, projectStrip, instagramStage]
      .filter(Boolean)
      .forEach((element) => resizeObserver?.observe(element as Element));

    measure();
    update();

    window.addEventListener("scroll", requestUpdate, { passive: true });
    window.addEventListener("resize", handleResize);
    window.addEventListener("load", handleResize);

    return () => {
      observer.disconnect();
      resizeObserver?.disconnect();
      cleanups.forEach((cleanup) => cleanup());
      window.removeEventListener("scroll", requestUpdate);
      window.removeEventListener("resize", handleResize);
      window.removeEventListener("load", handleResize);
      if (animationFrame) {
        window.cancelAnimationFrame(animationFrame);
      }
      delete root.dataset.homeTwoMotion;
    };
  }, []);

  return null;
}
