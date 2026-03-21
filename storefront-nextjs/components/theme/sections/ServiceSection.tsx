"use client";

import { useId, useState } from "react";
import Image from "next/image";
import styles from "../theme.module.css";
import { ThemeTag } from "../utils/shared";
import type { ServiceModel } from "@/components/theme/models";

export default function ServiceSection({ model }: { model: ServiceModel }) {
  const accordionId = useId();
  const [openServiceId, setOpenServiceId] = useState<string | null>(model.items[0]?.id ?? null);

  return (
    <section className={styles.serviceArea}>
      <div className={styles.contentContainer}>
        <div className={styles.serviceTitleBox}>
          {model.subtitle ? <ThemeTag>{model.subtitle}</ThemeTag> : null}
          <h2 className={styles.serviceTitle}>{model.title}</h2>
        </div>

        <div className={styles.serviceGrid}>
          <div className={styles.serviceShapeWrap}>
            <Image src="/theme/default/service/sv-shape-1.png" alt="Service shape" width={414} height={414} />
          </div>

          <div className={styles.serviceAccordion}>
            {model.items.map((service) => {
              const isOpen = openServiceId === service.id;
              const buttonId = `${accordionId}-button-${service.id}`;
              const panelId = `${accordionId}-panel-${service.id}`;

              return (
                <div
                  key={service.id}
                  className={`${styles.accordionItem} ${isOpen ? styles.accordionItemOpen : ""}`}
                >
                  <h3 className={styles.accordionHeader}>
                    <button
                      id={buttonId}
                      type="button"
                      className={styles.accordionButton}
                      aria-expanded={isOpen}
                      aria-controls={panelId}
                      onClick={() =>
                        setOpenServiceId((currentServiceId) =>
                          currentServiceId === service.id ? null : service.id,
                        )
                      }
                    >
                      <span className={styles.accordionIconWrap}>
                        {service.icon ? (
                          <Image
                            src={service.icon.src}
                            alt={service.title}
                            width={service.icon.width}
                            height={service.icon.height}
                          />
                        ) : null}
                      </span>
                      <span>{service.title}</span>
                      <span className={styles.accordionIcon} />
                    </button>
                  </h3>

                  <div
                    id={panelId}
                    role="region"
                    aria-labelledby={buttonId}
                    className={`${styles.accordionCollapse} ${isOpen ? styles.accordionCollapseOpen : ""}`}
                  >
                    <div className={styles.accordionCollapseInner}>
                      <div className={styles.accordionBody}>
                        {service.description ? <p>{service.description}</p> : null}
                      </div>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      </div>
    </section>
  );
}
