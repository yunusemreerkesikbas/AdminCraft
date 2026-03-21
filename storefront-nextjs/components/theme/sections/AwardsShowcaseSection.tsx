"use client";

import Image from "next/image";
import { useState } from "react";
import type { AwardsShowcaseModel } from "@/components/theme/models";
import styles from "../content-page.module.css";
import type { CSSProperties } from "react";

export default function AwardsShowcaseSection({ model }: { model: AwardsShowcaseModel }) {
  const [activeId, setActiveId] = useState(model.items[0]?.id ?? "");

  const activeItem = model.items.find((item) => item.id === activeId) ?? model.items[0];

  if (!activeItem) {
    return null;
  }

  return (
    <section className={styles.awardsSection}>
      <div className={styles.awardsInner}>
        <div
          className={`${styles.awardsHeader} ${styles.contentReveal}`.trim()}
          data-reveal
          style={{ "--reveal-delay": "40ms" } as CSSProperties}
        >
          {model.subtitle ? <span className={styles.awardsLabel}>{model.subtitle}</span> : null}
          <h2 className={styles.srOnly}>{model.title}</h2>
          {model.description ? <p className={styles.awardsDescription}>{model.description}</p> : null}
        </div>

        <div className={styles.awardsGrid}>
          <div
            className={`${styles.awardsPreviewWrap} ${styles.contentReveal}`.trim()}
            data-reveal
            style={{ "--reveal-delay": "120ms" } as CSSProperties}
          >
            <div className={styles.awardsPreview}>
              <div
                key={activeItem.id}
                className={`${styles.awardsPreviewMedia} ${!activeItem.image ? styles.awardsPreviewPlaceholder : ""}`.trim()}
              >
                {activeItem.image ? (
                  <Image
                    src={activeItem.image.src}
                    alt={activeItem.image.alt}
                    fill
                    sizes="(max-width: 1199px) 100vw, 32vw"
                    className={styles.awardsPreviewImage}
                    data-parallax="18"
                  />
                ) : null}
              </div>
            </div>
          </div>

          <div className={styles.awardsList}>
            {model.items.map((item, index) => {
              const isActive = item.id === activeItem.id;

              return (
                <button
                  key={item.id}
                  type="button"
                  className={`${styles.awardsItem} ${styles.contentReveal} ${isActive ? styles.awardsItemActive : ""}`.trim()}
                  data-reveal
                  style={{ "--reveal-delay": `${180 + index * 70}ms` } as CSSProperties}
                  onMouseEnter={() => setActiveId(item.id)}
                  onFocus={() => setActiveId(item.id)}
                >
                  <div className={styles.awardsItemMain}>
                    <span className={styles.awardsBadge}>{item.subtitle ?? "x1"}</span>
                    <p className={styles.awardsItemTitle}>{item.title}</p>
                  </div>
                  <span className={styles.awardsItemDate}>{item.date ?? ""}</span>
                </button>
              );
            })}
          </div>
        </div>
      </div>
    </section>
  );
}
