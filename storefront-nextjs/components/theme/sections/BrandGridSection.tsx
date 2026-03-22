import Image from "next/image";
import type { CSSProperties } from "react";
import type { BrandGridModel } from "@/components/theme/models";
import styles from "../service-page.module.css";

export default function BrandGridSection({ model }: { model: BrandGridModel }) {
  return (
    <section className={styles.brandGridSection}>
      <div className={styles.brandGrid}>
        {model.items.map((item) => {
          const aspectRatio = item.image ? item.image.width / item.image.height : 1;
          const logoScale = aspectRatio >= 4 ? 1.28 : aspectRatio >= 2.8 ? 1.2 : 1.12;
          const logoMaxHeight = aspectRatio >= 4 ? 44 : aspectRatio >= 2.8 ? 52 : 60;
          const mobileLogoScale = aspectRatio >= 4 ? 1.28 : aspectRatio >= 2.8 ? 1.18 : 1.12;
          const mobileLogoMaxHeight = aspectRatio >= 4 ? 54 : aspectRatio >= 2.8 ? 60 : 68;
          const mobileLogoTargetWidth = aspectRatio >= 4 ? 170 : aspectRatio >= 2.8 ? 152 : 132;

          return (
          <div
            key={item.id}
            className={styles.brandGridItem}
            style={
              {
                ["--brand-logo-scale" as string]: String(logoScale),
                ["--brand-logo-max-height" as string]: `${logoMaxHeight}px`,
                ["--brand-logo-scale-mobile" as string]: String(mobileLogoScale),
                ["--brand-logo-max-height-mobile" as string]: `${mobileLogoMaxHeight}px`,
                ["--brand-logo-target-width-mobile" as string]: `${mobileLogoTargetWidth}px`,
              } as CSSProperties
            }
          >
            {item.image ? (
              <div className={styles.brandGridImageWrap}>
                <Image
                  src={item.image.src}
                  alt={item.image.alt}
                  width={item.image.width}
                  height={item.image.height}
                  className={styles.brandGridImage}
                  sizes="(max-width: 767px) 90vw, (max-width: 991px) 50vw, 220px"
                />
              </div>
            ) : null}

            {item.texts.length > 0 ? (
              <div className={styles.brandGridTexts} aria-hidden="true">
                {item.texts.map((t, i) => (
                  <span key={`${item.id}-t-${i}`}>{t}</span>
                ))}
              </div>
            ) : null}
          </div>
        )})}
      </div>
    </section>
  );
}

