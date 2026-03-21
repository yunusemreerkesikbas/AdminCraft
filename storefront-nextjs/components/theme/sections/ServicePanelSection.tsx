import Image from "next/image";
import Link from "next/link";
import type { CSSProperties } from "react";
import styles from "../service-page.module.css";
import type { ServicePanelModel } from "@/components/theme/models";
import { RightArrowIcon, ShapeTwoIcon } from "@/components/theme/utils/icons";
import ServiceReveal from "./ServiceReveal";

const splitButtonLabel = (label: string): string[] => {
  const explicitLines = label
    .split(/\r?\n/)
    .map((line) => line.trim())
    .filter(Boolean);

  if (explicitLines.length > 1) {
    return explicitLines;
  }

  const words = label.split(/\s+/).filter(Boolean);
  if (words.length < 2) {
    return [label];
  }

  return [words[0] ?? label, words.slice(1).join(" ")];
};

export default function ServicePanelSection({ model }: { model: ServicePanelModel }) {
  return (
    <section className={styles.servicePanelSection}>
      <div className={styles.servicePanelInner}>
        {model.items.map((panel, index) => (
          <ServiceReveal
            as="article"
            key={panel.id}
            className={styles.servicePanelCard}
            style={
              {
                ["--panel-reveal-delay" as string]: `${80 + index * 120}ms`,
                ["--panel-z-index" as string]: `${index + 1}`,
              } as CSSProperties
            }
            delayMs={index * 90}
          >
            <div className={styles.servicePanelMedia}>
              {panel.image ? (
                <Image
                  src={panel.image.src}
                  alt={panel.image.alt}
                  width={panel.image.width}
                  height={panel.image.height}
                  className={styles.servicePanelImage}
                  sizes="(max-width: 991px) 100vw, 520px"
                />
              ) : null}
            </div>

            <div className={styles.servicePanelContent}>
              <div className={styles.servicePanelTitleBox}>
                <div className={styles.servicePanelSubtitle}>
                  <i>
                    {panel.panelNumber > 0 ? String(panel.panelNumber).padStart(2, "0") : "00"}
                  </i>
                  {panel.panelSubtitle}
                </div>
                <h3 className={styles.servicePanelTitle}>{panel.title}</h3>
              </div>

              <div className={styles.servicePanelSpaceWrap}>
                {panel.description ? <p className={styles.servicePanelDescription}>{panel.description}</p> : null}

                {panel.bullets.length > 0 ? (
                  <ul className={styles.servicePanelList}>
                    {panel.bullets.map((b, i) => (
                      <li key={`${panel.id}-b-${i}`}>{b}</li>
                    ))}
                  </ul>
                ) : null}

                {panel.button ? (
                  panel.button.external ? (
                    <a href={panel.button.href} className={styles.servicePanelButton} target="_blank" rel="noopener noreferrer">
                      <span className={styles.servicePanelButtonShape} aria-hidden="true" />
                      <span className={styles.servicePanelButtonLabel}>
                        {splitButtonLabel(panel.button.label).map((line, index) => (
                          <span key={`${panel.id}-btn-${index}`}>{line}</span>
                        ))}
                        <span className={styles.servicePanelButtonArrow} aria-hidden="true">
                          <RightArrowIcon color="currentColor" />
                        </span>
                      </span>
                      <span className={styles.servicePanelButtonSvg} aria-hidden="true">
                        <ShapeTwoIcon />
                      </span>
                    </a>
                  ) : (
                    <Link href={panel.button.href} className={styles.servicePanelButton}>
                      <span className={styles.servicePanelButtonLabel}>
                        {splitButtonLabel(panel.button.label).map((line, index) => (
                          <span key={`${panel.id}-btn-${index}`}>{line}</span>
                        ))}
                        <span className={styles.servicePanelButtonArrow} aria-hidden="true">
                          <RightArrowIcon color="currentColor" />
                        </span>
                      </span>
                      <span className={styles.servicePanelButtonSvg} aria-hidden="true">
                        <ShapeTwoIcon />
                      </span>
                    </Link>
                  )
                ) : null}
              </div>
            </div>
          </ServiceReveal>
        ))}
      </div>
    </section>
  );
}

