import styles from "../theme.module.css";
import type { MarqueeModel } from "@/components/theme/models";

function LineTrack({ items, reverse = false }: { items: string[]; reverse?: boolean }) {
  return (
    <div className={`${styles.lineTextTrackWrap} ${reverse ? styles.lineTextTrackWrapReverse : ""}`}>
      <div className={`${styles.lineTextTrack} ${reverse ? styles.lineTextTrackReverse : ""}`}>
        {items.map((item, index) => (
          <span key={`${item}-${index}`} className={styles.lineTextItem}>
            {item}
          </span>
        ))}
        <div aria-hidden="true" style={{ display: "contents" }}>
          {items.map((item, index) => (
            <span key={`dup-${item}-${index}`} className={styles.lineTextItem}>
              {item}
            </span>
          ))}
        </div>
      </div>
    </div>
  );
}

export default function MarqueeText({ model }: { model: MarqueeModel }) {
  return (
    <section className={styles.lineTextArea}>
      <LineTrack items={model.primary} />
      <LineTrack items={model.secondary} reverse />
    </section>
  );
}
