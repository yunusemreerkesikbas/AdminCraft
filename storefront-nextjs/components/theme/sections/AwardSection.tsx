import Image from "next/image";
import styles from "../theme.module.css";
import { ThemeCircleButton } from "../utils/shared";
import type { AwardModel } from "@/components/theme/models";

export default function AwardSection({ model }: { model: AwardModel }) {
  return (
    <section className={styles.awardArea}>
      <div className={styles.awardShape}>
        <span className={styles.awardGlow} />
        <span className={styles.awardImageWrap}>
          {model.image ? (
            <Image
              src={model.image.src}
              alt={model.image.alt}
              fill
              sizes="(max-width: 767px) 80vw, (max-width: 1199px) 320px, (max-width: 1399px) 360px, 533px"
              className={styles.awardImage}
            />
          ) : null}
        </span>
      </div>

      <div className={styles.awardContainer}>
        <div className={styles.awardTitleBox}>
          {model.eyebrow ? <span className={styles.awardSubtitle}>{model.eyebrow}</span> : null}
          <h2 className={`${styles.awardTitle} ${styles.awardTitlePrimary}`}>{model.primaryTitle}</h2>
          {model.secondaryTitle ? (
            <h2 className={`${styles.awardTitle} ${styles.awardTitleSecondary}`}>
              <span>{model.secondaryTitle}</span>
            </h2>
          ) : null}
          {model.description ? <p className={styles.awardDescription}>{model.description}</p> : null}
        </div>

        <div className={styles.awardButtonBox}>
          {model.button ? (
            <ThemeCircleButton href={model.button.href} external={model.button.external}>
              <>
                {model.button.label}
              </>
            </ThemeCircleButton>
          ) : null}
        </div>
      </div>
    </section>
  );
}
