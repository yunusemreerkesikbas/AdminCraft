import Image from "next/image";
import styles from "../theme.module.css";
import { ThemeButton } from "../utils/shared";
import type { InstagramModel } from "@/components/theme/models";

const floatingClasses = [
  styles.instagramFloatOne,
  styles.instagramFloatTwo,
  styles.instagramFloatThree,
  styles.instagramFloatFour,
  styles.instagramFloatFive,
  styles.instagramFloatSix,
  styles.instagramFloatSeven,
];

export default function InstagramSection({ model }: { model: InstagramModel }) {
  return (
    <section className={styles.instagramArea}>
      <div className={styles.instagramPin}>
        <div className={styles.instagramStage}>
          {model.floatingImages.map((item, index) => (
            <div key={`${item.src}-${index}`} className={`${styles.instagramFloatingThumb} ${floatingClasses[index]}`}>
              <Image
                src={item.src}
                alt={item.alt}
                fill
                sizes="(max-width: 1199px) 0px, 220px"
                className={styles.coverImage}
              />
            </div>
          ))}

          <div className={styles.instagramBadge}>
            <span>IG</span>
          </div>

          <div className={styles.instagramMainThumb}>
            {model.mainImage ? (
              <Image
                src={model.mainImage.src}
                alt={model.mainImage.alt}
                fill
                sizes="(max-width: 1199px) 100vw, 100vw"
                className={styles.coverImage}
              />
            ) : null}
          </div>

            <div className={styles.instagramContentWrap}>
              <div className={styles.instagramTitleBox}>
                {model.eyebrow ? <span className={styles.instagramSubtitle}>{model.eyebrow}</span> : null}
                {model.handle ? <h4 className={styles.instagramTitle}>{model.handle}</h4> : null}
              </div>

            <div className={styles.instagramContent}>
              {model.description ? <p>{model.description}</p> : null}
              {model.button ? (
                <ThemeButton href={model.button.href} dark external={model.button.external}>
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
