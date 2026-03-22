import Image from "next/image";
import Link from "next/link";
import type { CSSProperties } from "react";
import styles from "../content-page.module.css";
import type { LogoItemModel, LogoMarqueeModel } from "@/components/theme/models";
function LogoCard({ logo }: { logo: LogoItemModel }) {
  const image = (
    <Image
      src={logo.image.src}
      alt={logo.image.alt}
      width={logo.image.width}
      height={logo.image.height}
      sizes="180px"
      className={styles.logoItemImage}
    />
  );

  if (!logo.href) {
    return <div className={styles.logoItem}>{image}</div>;
  }

  if (logo.external) {
    return (
      <a href={logo.href} className={styles.logoItem} target="_blank" rel="noopener noreferrer">
        {image}
      </a>
    );
  }

  return (
    <Link href={logo.href} className={styles.logoItem}>
      {image}
    </Link>
  );
}

export default function LogoMarqueeSection({ model }: { model: LogoMarqueeModel }) {
  const marqueeItems = [...model.logos, ...model.logos];
  const hasLogos = model.logos.length > 0;

  return (
    <section className={styles.logoSection}>
      <div className={styles.logoInner}>
        <div
          className={`${styles.logoHeader} ${styles.contentReveal}`.trim()}
          data-reveal
          style={{ "--reveal-delay": "60ms" } as CSSProperties}
        >
          {model.subtitle ? <span className={styles.logoLabel}>{model.subtitle}</span> : null}
          {model.title ? <h2 className={styles.logoTitle}>{model.title}</h2> : null}
        </div>

        <div className={styles.logoMarquee}>
          {hasLogos ? (
            <div className={styles.logoTrack}>
              {marqueeItems.map((logo, index) => (
                <LogoCard key={`${logo.id}-${index}`} logo={logo} />
              ))}
            </div>
          ) : (
            <div className={styles.logoGhostRow}>
              <span className={styles.logoGhost} />
              <span className={styles.logoGhost} />
              <span className={styles.logoGhost} />
              <span className={styles.logoGhost} />
              <span className={styles.logoGhost} />
            </div>
          )}
        </div>

        {model.description ? (
          <div
            className={`${styles.logoFooter} ${styles.contentReveal}`.trim()}
            data-reveal
            style={{ "--reveal-delay": "160ms" } as CSSProperties}
          >
            <p className={styles.logoDescription}>{model.description}</p>
          </div>
        ) : null}
      </div>
    </section>
  );
}
