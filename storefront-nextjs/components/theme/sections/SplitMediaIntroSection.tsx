import Image from "next/image";
import type { CSSProperties } from "react";
import styles from "../content-page.module.css";
import type { SplitMediaIntroModel } from "@/components/theme/models";

export default function SplitMediaIntroSection({ model }: { model: SplitMediaIntroModel }) {
  const [leadParagraph, ...followParagraphs] = model.paragraphs;

  return (
    <section className={styles.storySection}>
      <div className={styles.storyMediaBand}>
        <div
          className={`${styles.storyLeftThumb} ${styles.contentReveal} ${!model.mainImage ? styles.storySurfaceWarm : ""}`.trim()}
          data-reveal
          style={{ "--reveal-delay": "40ms" } as CSSProperties}
        >
          {model.mainImage ? (
            <Image
              src={model.mainImage.src}
              alt={model.mainImage.alt}
              fill
              sizes="(max-width: 991px) 100vw, 42vw"
              className={styles.storyImage}
              data-parallax="26"
            />
          ) : null}
        </div>

        <div className={styles.storyRightThumb}>
          <div
            className={`${styles.storyRightPrimary} ${styles.contentReveal} ${!model.sideImage ? styles.storySurfaceCool : ""}`.trim()}
            data-reveal
            style={{ "--reveal-delay": "150ms" } as CSSProperties}
          >
            {model.sideImage ? (
              <Image
                src={model.sideImage.src}
                alt={model.sideImage.alt}
                fill
                sizes="(max-width: 991px) 100vw, 33vw"
                className={styles.storyImage}
                data-parallax="18"
              />
            ) : null}
          </div>

          <div
            className={`${styles.storyRightOverlay} ${styles.contentReveal} ${!model.innerImage ? styles.storySurfaceAccent : ""}`.trim()}
            data-reveal
            style={{ "--reveal-delay": "240ms" } as CSSProperties}
          >
            {model.innerImage ? (
              <Image
                src={model.innerImage.src}
                alt={model.innerImage.alt}
                fill
                sizes="(max-width: 991px) 72vw, 22vw"
                className={styles.storyImage}
                data-parallax="32"
              />
            ) : null}
            {model.innerLabel ? <span className={styles.storyInnerLabel}>{model.innerLabel}</span> : null}
          </div>
        </div>
      </div>

      <div
        className={`${styles.storyIntroBlock} ${styles.contentReveal}`.trim()}
        data-reveal
        style={{ "--reveal-delay": "120ms" } as CSSProperties}
      >
        {model.introLabel ? <span className={styles.storyHello}>{model.introLabel}</span> : null}
        {leadParagraph ? <p className={styles.storyLead}>{leadParagraph}</p> : null}
        {followParagraphs.map((paragraph) => (
          <p key={paragraph} className={styles.storyFollow}>
            {paragraph}
          </p>
        ))}
      </div>

      <div
        className={`${styles.storyDetailGrid} ${styles.contentReveal}`.trim()}
        data-reveal
        style={{ "--reveal-delay": "220ms" } as CSSProperties}
      >
        <div className={styles.storyCategoryTitleBox}>
          <h2 className={styles.storyCategoryTitle}>
            {model.heading}
            {model.headingAccent ? <span>{model.headingAccent}</span> : null}
          </h2>
        </div>

        <div className={styles.storyLists}>
          {model.listGroups.length > 0 ? (
            model.listGroups.map((group, index) => (
              <article
                key={group.id}
                className={`${styles.storyListGroup} ${index === 0 ? styles.storyListGroupOffset : styles.storyListGroupWide}`.trim()}
              >
                {group.title ? <h3 className={styles.storyListTitle}>{group.title}</h3> : null}
                <ul className={styles.storyList}>
                  {group.items.map((item) => (
                    <li key={item}>{item}</li>
                  ))}
                </ul>
              </article>
            ))
          ) : null}
        </div>
      </div>
    </section>
  );
}
