import Link from "next/link";
import styles from "../service-page.module.css";
import type { BigTextCtaModel } from "@/components/theme/models";
import ServiceReveal from "./ServiceReveal";

export default function BigTextCtaSection({ model }: { model: BigTextCtaModel }) {
  if (!model.button) {
    return null;
  }

  const content = (
    <>
      <div className={styles.bigTextMeta}>
        <span>{model.leftLine}</span>
        <span>{model.rightLine}</span>
      </div>
      <span className={styles.bigTextLinkText}>{model.button.label}</span>
    </>
  );

  return (
    <section className={styles.bigTextSection}>
      <ServiceReveal className={styles.serviceContainer}>
        {model.button.external ? (
          <a href={model.button.href} className={styles.bigTextLink} target="_blank" rel="noopener noreferrer">
            {content}
          </a>
        ) : (
          <Link href={model.button.href} className={styles.bigTextLink}>
            {content}
          </Link>
        )}
      </ServiceReveal>
    </section>
  );
}
