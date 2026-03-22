import styles from "../theme.module.css";
import { ThemeTag } from "../utils/shared";
import type { VideoModel } from "@/components/theme/models";

export default function VideoSection({ model }: { model: VideoModel }) {
  return (
    <section className={styles.videoArea} aria-label={model.title}>
      <div className={styles.heroContainer}>
        <div className={styles.videoPin}>
          <div className={styles.videoWrap}>
            <video
              className={styles.videoPlayer}
              loop
              muted
              autoPlay
              playsInline
              poster={model.posterImage?.src}
            >
              <source src={model.videoUrl} type="video/mp4" />
            </video>

            <div className={styles.videoOverlay} />

            <div className={styles.videoContent}>
              {model.subtitle ? (
                <ThemeTag className={`${styles.sectionTagInverse} ${styles.videoTag}`}>{model.subtitle}</ThemeTag>
              ) : null}
              <h2 className={styles.videoTitle}>{model.title}</h2>
              {model.description ? <p>{model.description}</p> : null}
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
