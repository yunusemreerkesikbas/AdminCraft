import Image from "next/image";
import styles from "../service-page.module.css";
import type { ServiceHeroModel } from "@/components/theme/models";
import ServiceReveal from "./ServiceReveal";

const splitTitle = (title: string): string[] => {
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

  return [words[0] ?? title, words.slice(1).join(" ")];
};

export default function ServiceHeroSection({ model }: { model: ServiceHeroModel }) {
  const titleLines = splitTitle(model.title);

  return (
    <section className={styles.serviceHeroSection}>
      <div className={styles.serviceContainer}>
        <div className={styles.serviceHeroTitleWrap}>
          <ServiceReveal as="h1" className={`${styles.serviceHeroTitle} ${styles.serviceHeroTitleAnimated}`} direction="right">
            {titleLines.map((line, index) => (
              <span key={`${line}-${index}`} style={{ ["--line-index" as string]: index }}>
                {line}
              </span>
            ))}
          </ServiceReveal>
          {model.description ? (
            <ServiceReveal as="p" className={styles.serviceHeroLead} delayMs={180}>
              {model.description}
            </ServiceReveal>
          ) : null}
        </div>

        <ServiceReveal className={styles.serviceHeroMediaWrap} delayMs={120}>
          <div className={styles.serviceHeroMedia}>
            {model.backgroundImage ? (
              <Image
                src={model.backgroundImage.src}
                alt={model.backgroundImage.alt}
                width={model.backgroundImage.width}
                height={model.backgroundImage.height}
                priority
                sizes="(max-width: 991px) 100vw, 1530px"
                className={styles.serviceHeroImage}
              />
            ) : null}
          </div>

          {model.overlayImage ? (
            <Image
              src={model.overlayImage.src}
              alt={model.overlayImage.alt}
              width={model.overlayImage.width}
              height={model.overlayImage.height}
              sizes="220px"
              className={styles.serviceHeroShape}
            />
          ) : null}
        </ServiceReveal>
      </div>
    </section>
  );
}
