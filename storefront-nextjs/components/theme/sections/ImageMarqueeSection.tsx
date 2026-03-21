import Image from "next/image";
import styles from "../service-page.module.css";
import type { ImageMarqueeModel } from "@/components/theme/models";

export default function ImageMarqueeSection({ model }: { model: ImageMarqueeModel }) {
  const marqueeItems = [...model.items, ...model.items];

  return (
    <section className={styles.imageMarqueeSection}>
      <div className={styles.imageMarqueeTrack}>
        {marqueeItems.map((item, index) => (
          <div
            key={`${item.id}-${index}`}
            className={`${styles.imageMarqueeCard} ${index % 2 === 0 ? styles.imageMarqueeCardPrimary : styles.imageMarqueeCardSecondary}`}
          >
            <Image
              src={item.image.src}
              alt={item.image.alt}
              width={item.image.width}
              height={item.image.height}
              className={styles.imageMarqueeImage}
              sizes="(max-width: 767px) 72vw, (max-width: 991px) 45vw, 360px"
            />
          </div>
        ))}
      </div>
    </section>
  );
}
