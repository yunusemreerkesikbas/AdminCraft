import Image from "next/image";
import styles from "../theme.module.css";
import { ThemeButton } from "../utils/shared";
import type { HeroModel } from "@/components/theme/models";

export default function HeroBanner({ model }: { model: HeroModel }) {
  const backgroundImage = model.backgroundImage;

  return (
    <section className={styles.heroArea}>
      <div className={styles.heroContainer}>
        <div className={styles.heroWrapper}>
          <div className={styles.heroBackground}>
            {backgroundImage ? (
              <Image
                src={backgroundImage.src}
                alt={backgroundImage.alt}
                fill
                priority
                sizes="100vw"
                className={styles.heroBackgroundImage}
              />
            ) : null}
          </div>

          <div className={styles.heroContentWrap}>
            <div className={styles.heroTitleBox}>
              <h1 className={`${styles.heroTitle} ${styles.heroTitlePrimary}`}>{model.title}</h1>
              {model.subtitle ? (
                <h2 className={`${styles.heroTitle} ${styles.heroTitleSecondary}`}>
                  <span>{model.subtitle}</span>
                </h2>
              ) : null}
            </div>

            <div className={`${styles.heroContent} ${styles.heroContentReveal}`}>
              {model.description ? <p>{model.description}</p> : null}
              {model.button ? (
                <ThemeButton href={model.button.href} external={model.button.external}>
                  {model.button.label}
                </ThemeButton>
              ) : null}
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
