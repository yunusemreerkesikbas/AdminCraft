"use client";

import "swiper/css";
import Image from "next/image";
import Link from "next/link";
import { useEffect, useId, useRef, useState, type CSSProperties } from "react";
import { Autoplay } from "swiper/modules";
import { Swiper, SwiperSlide } from "swiper/react";
import type { PeopleCarouselModel, PersonCardModel } from "@/components/theme/models";
import styles from "../content-page.module.css";

const sliderBreakpoints = {
  0: { slidesPerView: 1.15, spaceBetween: 16 },
  640: { slidesPerView: 2.2, spaceBetween: 18 },
  992: { slidesPerView: 3.45, spaceBetween: 22 },
  1200: { slidesPerView: 4.5, spaceBetween: 24 },
  1440: { slidesPerView: 5.45, spaceBetween: 28 },
};

const maxSlidesPerView = Math.ceil(
  Math.max(...Object.values(sliderBreakpoints).map((breakpoint) => breakpoint.slidesPerView)),
);
const minLoopSlides = maxSlidesPerView * 2;

const buildLoopPeople = (people: PersonCardModel[]): PersonCardModel[] => {
  if (people.length === 0) {
    return [];
  }

  if (people.length >= minLoopSlides) {
    return people;
  }

  const repeatCount = Math.ceil(minLoopSlides / people.length);
  return Array.from({ length: repeatCount }, () => people).flat();
};

function ProfileLink({ person }: { person: PersonCardModel }) {
  if (!person.profileHref) {
    return null;
  }

  if (person.external) {
    return (
      <a
        href={person.profileHref}
        className={styles.teamModalLink}
        target="_blank"
        rel="noopener noreferrer"
      >
        View profile
      </a>
    );
  }

  return (
    <Link href={person.profileHref} className={styles.teamModalLink}>
      View profile
    </Link>
  );
}

export default function PeopleCarouselSection({ model }: { model: PeopleCarouselModel }) {
  const [activePerson, setActivePerson] = useState<PersonCardModel | null>(null);
  const dialogRef = useRef<HTMLDialogElement>(null);
  const dialogTitleId = useId();
  const ariaLabel = model.title ?? model.subtitle ?? "Team";
  const loopPeople = buildLoopPeople(model.people);
  const enableLoop = model.people.length > 1;

  useEffect(() => {
    const dialog = dialogRef.current;
    if (!dialog) return;
    if (activePerson) {
      dialog.showModal();
    } else {
      dialog.close();
    }
  }, [activePerson]);

  useEffect(() => {
    const dialog = dialogRef.current;
    if (!dialog) return;
    const handleClose = () => setActivePerson(null);
    dialog.addEventListener("close", handleClose);
    return () => dialog.removeEventListener("close", handleClose);
  }, []);

  return (
    <>
      <section className={styles.teamSection} aria-label={ariaLabel}>
        {model.subtitle || model.title ? (
          <div className={styles.srOnly}>
            {model.subtitle ? <span>{model.subtitle}</span> : null}
            {model.title ? <h2>{model.title}</h2> : null}
          </div>
        ) : null}

        <Swiper
          className={styles.teamSwiper}
          breakpoints={sliderBreakpoints}
          loop={enableLoop}
          speed={11000}
          loopAdditionalSlides={enableLoop ? maxSlidesPerView : 0}
          autoplay={
            enableLoop
              ? {
                  delay: 1,
                  disableOnInteraction: false,
                  pauseOnMouseEnter: true,
                }
              : false
          }
          modules={[Autoplay]}
        >
          {loopPeople.map((person, index) => (
            <SwiperSlide key={`${person.id}-${index}`}>
              <article
                className={`${styles.teamCard} ${styles.contentReveal}`.trim()}
                data-reveal
                style={{ "--reveal-delay": `${120 + index * 70}ms` } as CSSProperties}
              >
                <button
                  type="button"
                  className={styles.teamCardButton}
                  onClick={() => setActivePerson(person)}
                >
                  <div className={`${styles.teamMedia} ${!person.image ? styles.teamMediaPlaceholder : ""}`.trim()}>
                    {person.image ? (
                      <Image
                        src={person.image.src}
                        alt={person.image.alt}
                        fill
                        sizes="(max-width: 639px) 88vw, (max-width: 991px) 42vw, (max-width: 1439px) 30vw, 22vw"
                        className={styles.teamImage}
                      />
                    ) : null}
                  </div>

                  <div className={styles.teamContent}>
                    {person.role ? <span className={styles.teamRole}>{person.role}</span> : null}
                    <h3 className={styles.teamName}>{person.name}</h3>
                  </div>
                </button>
              </article>
            </SwiperSlide>
          ))}
        </Swiper>
      </section>

      <dialog
        ref={dialogRef}
        aria-labelledby={dialogTitleId}
        className={styles.teamModalOverlay}
        onClick={(event) => {
          if (event.target === dialogRef.current) {
            setActivePerson(null);
          }
        }}
      >
        {activePerson ? (
          <div className={styles.teamModal}>
            <div className={styles.teamModalHeader}>
              <button
                type="button"
                className={styles.teamModalClose}
                onClick={() => setActivePerson(null)}
                aria-label="Close profile"
              >
                x
              </button>
            </div>

            <div className={styles.teamModalBody}>
              <div className={styles.teamModalMedia}>
                {activePerson.image ? (
                  <Image
                    src={activePerson.image.src}
                    alt={activePerson.image.alt}
                    fill
                    sizes="(max-width: 1199px) 100vw, 34vw"
                    className={styles.teamImage}
                  />
                ) : null}
              </div>

              <div className={styles.teamModalContent}>
                {activePerson.role ? <span className={styles.teamRole}>{activePerson.role}</span> : null}
                <h3 id={dialogTitleId} className={styles.teamModalTitle}>
                  {activePerson.name}
                </h3>
                {activePerson.bio ? <p className={styles.teamModalCopy}>{activePerson.bio}</p> : null}
                <ProfileLink person={activePerson} />
              </div>
            </div>
          </div>
        ) : null}
      </dialog>
    </>
  );
}
