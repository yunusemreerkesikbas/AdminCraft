import Image from "next/image";
import styles from "../service-page.module.css";
import type { ServiceModel } from "@/components/theme/models";
import { LeafIcon } from "@/components/theme/utils/icons";
import ServiceReveal from "./ServiceReveal";

const splitHeading = (title: string): string[] => {
  const explicitLines = title
    .split(/\r?\n/)
    .map((line) => line.trim())
    .filter(Boolean);

  if (explicitLines.length > 1) {
    return explicitLines;
  }

  const words = title.split(/\s+/).filter(Boolean);
  if (words.length < 6) {
    return [title];
  }

  const midpoint = Math.ceil(words.length / 2);
  return [words.slice(0, midpoint).join(" "), words.slice(midpoint).join(" ")];
};

export default function ServiceCardsGridSection({ model }: { model: ServiceModel }) {
  const titleLines = splitHeading(model.title);

  return (
    <section className={styles.serviceCardsSection}>
      <div className={styles.serviceContainer}>
        <ServiceReveal className={styles.serviceCardsHeading}>
          <h2 className={styles.serviceCardsTitle}>
            {model.subtitle ? (
              <span className={styles.serviceCardsEyebrow}>
                <span className={styles.serviceCardsEyebrowIcon}>
                  <LeafIcon />
                </span>
                {model.subtitle}
              </span>
            ) : null}
            <span className={styles.serviceCardsTitleSpacer} aria-hidden="true" />
            {titleLines.map((line, index) => (
              <span key={`${line}-${index}`} className={styles.serviceCardsTitleLine}>
                {line}
                {index < titleLines.length - 1 ? " " : ""}
              </span>
            ))}
          </h2>
        </ServiceReveal>

        <div className={styles.serviceCardsGrid}>
          <div className={styles.serviceCardsSpacer} aria-hidden="true" />
          {model.items.map((item, index) => (
            <ServiceReveal key={item.id} as="article" className={styles.serviceCard} delayMs={index * 120}>
              <div className={styles.serviceCardIconWrap}>
                {item.icon ? (
                  <Image
                    src={item.icon.src}
                    alt={item.icon.alt}
                    width={item.icon.width}
                    height={item.icon.height}
                    className={styles.serviceCardIcon}
                  />
                ) : null}
              </div>

              <div className={styles.serviceCardContent}>
                <h3 className={styles.serviceCardTitle}>{item.title}</h3>
                {item.description ? <p className={styles.serviceCardDescription}>{item.description}</p> : null}
              </div>
            </ServiceReveal>
          ))}
        </div>
      </div>
    </section>
  );
}
