import Image from "next/image";
import styles from "../theme.module.css";
import type { AboutModel } from "@/components/theme/models";

export default function AboutSection({ model }: { model: AboutModel }) {
  return (
    <section className={styles.aboutArea}>
      <div className={styles.contentContainer}>
        <div className={styles.aboutTitleRow}>
          <div className={styles.aboutTitleWrap}>
            <h2 className={styles.aboutTitle}>{model.title}</h2>
          </div>
        </div>

        <div className={styles.aboutGrid}>
          <div className={styles.aboutThumbColumn}>
            <div className={styles.aboutThumbBox}>
              <div className={styles.aboutMainThumb}>
                {model.mainImage ? (
                  <Image
                    src={model.mainImage.src}
                    alt={model.mainImage.alt}
                    width={model.mainImage.width}
                    height={model.mainImage.height}
                  />
                ) : null}
              </div>

              <div className={styles.aboutInnerThumb}>
                {model.innerImage ? (
                  <Image
                    src={model.innerImage.src}
                    alt={model.innerImage.alt}
                    width={model.innerImage.width}
                    height={model.innerImage.height}
                  />
                ) : null}
                {model.innerLabel ? <span className={styles.aboutThumbText}>{model.innerLabel}</span> : null}
              </div>
            </div>
          </div>

          <div className={styles.aboutContentColumn}>
            <div className={styles.aboutContent}>
              {model.eyebrow ? <span>{model.eyebrow}</span> : null}
              {model.paragraphs.map((paragraph) => (
                <p key={paragraph}>{paragraph}</p>
              ))}
            </div>
          </div>

          <div className={styles.aboutSideColumn}>
            <div className={styles.aboutSideThumb}>
              {model.sideImage ? (
                <Image
                  src={model.sideImage.src}
                  alt={model.sideImage.alt}
                  fill
                  sizes="200px"
                  className={styles.coverImage}
                />
              ) : null}
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
