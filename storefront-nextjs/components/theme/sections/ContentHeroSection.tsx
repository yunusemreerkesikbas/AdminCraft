import Image from "next/image";
import Link from "next/link";
import type { CSSProperties } from "react";
import styles from "../content-page.module.css";
import type { ContentHeroModel } from "@/components/theme/models";

const normalizeAnchor = (target?: string): string | null => {
  if (!target) {
    return null;
  }

  return target.startsWith("#") ? target : `#${target}`;
};

const splitTitleLines = (title: string): string[] => {
  const explicitLines = title
    .split(/\r?\n/)
    .map((line) => line.trim())
    .filter(Boolean);

  if (explicitLines.length > 1) {
    return explicitLines;
  }

  const words = title.split(/\s+/).filter(Boolean);
  if (words.length <= 2) {
    return [title];
  }

  let bestBreak = 1;
  let smallestDelta = Number.POSITIVE_INFINITY;

  for (let index = 1; index < words.length; index += 1) {
    const firstLine = words.slice(0, index).join(" ");
    const secondLine = words.slice(index).join(" ");
    const delta = Math.abs(firstLine.length - secondLine.length);

    if (delta < smallestDelta) {
      smallestDelta = delta;
      bestBreak = index;
    }
  }

  return [words.slice(0, bestBreak).join(" "), words.slice(bestBreak).join(" ")];
};

function ActionButton({
  href,
  className,
  external,
  children,
}: {
  href: string;
  className: string;
  external?: boolean;
  children: React.ReactNode;
}) {
  if (external) {
    return (
      <a href={href} className={className} target="_blank" rel="noopener noreferrer">
        {children}
      </a>
    );
  }

  return (
    <Link href={href} className={className}>
      {children}
    </Link>
  );
}

export default function ContentHeroSection({ model }: { model: ContentHeroModel }) {
  const scrollHref = normalizeAnchor(model.scrollTarget);
  const titleLines = splitTitleLines(model.title);

  return (
    <section className={styles.heroSection}>
      <div className={styles.heroBackground} data-reveal-bg>
        {model.backgroundImage ? (
          <Image
            src={model.backgroundImage.src}
            alt={model.backgroundImage.alt}
            fill
            priority
            sizes="100vw"
            className={styles.heroImage}
            data-parallax="72"
          />
        ) : null}
      </div>

      {scrollHref ? (
        <a
          href={scrollHref}
          className={styles.heroScrollLink}
          data-reveal
          style={{ "--reveal-delay": "260ms" } as CSSProperties}
        >
          <span>Scroll to explore</span>
        </a>
      ) : null}

      <div className={styles.heroInner}>
        <div className={styles.heroHeadline}>
          {model.eyebrow ? (
            <span
              className={`${styles.heroSubtitle} ${styles.contentReveal}`.trim()}
              data-reveal
              style={{ "--reveal-delay": "60ms" } as CSSProperties}
            >
              {model.eyebrow}
            </span>
          ) : null}
          <h1 className={styles.heroTitle} data-reveal-title>
            {titleLines.map((line, index) => (
              <span key={`${line}-${index}`} className={styles.heroTitleLine}>
                <span
                  className={styles.heroTitleLineInner}
                  style={{ "--line-delay": `${120 + index * 90}ms` } as CSSProperties}
                >
                  {line}
                </span>
              </span>
            ))}
          </h1>
          {model.description ? (
            <p
              className={`${styles.heroLead} ${styles.contentReveal}`.trim()}
              data-reveal
              style={{ "--reveal-delay": "220ms" } as CSSProperties}
            >
              {model.description}
            </p>
          ) : null}
        </div>

        {model.supportingText || model.button || scrollHref ? (
          <div
            className={`${styles.heroEditorial} ${styles.contentReveal}`.trim()}
            data-reveal
            style={{ "--reveal-delay": "320ms" } as CSSProperties}
          >
            {model.supportingText ? <p className={styles.heroSupportingText}>{model.supportingText}</p> : null}

            {model.button ? (
              <ActionButton
                href={model.button.href}
                className={styles.heroTextButton}
                external={model.button.external}
              >
                {model.button.label}
              </ActionButton>
            ) : null}
          </div>
        ) : null}
      </div>
    </section>
  );
}
