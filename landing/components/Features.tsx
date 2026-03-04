import Link from "next/link";
import { AnimateInView } from "./AnimateInView";

type FeatureCard = {
  title: string;
  description: string;
  bgClass: string;
  ctaLabel?: string;
  ctaUrl?: string;
};

type FeaturesProps = {
  sectionTag: string;
  heading: string;
  subheading: string;
  cards: FeatureCard[];
};

function FeatureVisualCard() {
  return (
    <div className="relative overflow-hidden rounded-xl bg-white shadow-md">
      <div
        className="absolute left-0 top-0 h-full w-1 bg-gradient-to-b from-[var(--color-theme-4)] via-[#a5a3f8] to-[var(--color-theme-6)]"
        aria-hidden
      />
      <div className="p-3.5 pl-4">
        <div className="mb-2.5 flex items-start justify-between gap-1">
          <h4 className="font-heading text-xs font-semibold text-[var(--color-dark-neutral-1)]">
            Net volume from sales
          </h4>
          <button
            type="button"
            className="rounded p-0.5 text-[var(--color-dark-neutral-2)] hover:bg-[var(--color-light-neutral-2)]"
            aria-label="Options"
          >
            <span className="inline-block size-3.5 text-center text-sm leading-none">⋮</span>
          </button>
        </div>
        <div className="mb-2.5 flex gap-2">
          <div className="rounded-md bg-[var(--color-light-neutral-2)] px-2 py-1.5">
            <p className="text-[10px] text-[var(--color-dark-neutral-2)]">Today</p>
            <p className="font-heading text-xs font-semibold text-[var(--color-dark-neutral-1)]">
              SGD 4,887
            </p>
          </div>
          <div className="rounded-md bg-[var(--color-light-neutral-2)] px-2 py-1.5">
            <p className="text-[10px] text-[var(--color-dark-neutral-2)]">Previous</p>
            <p className="font-heading text-xs font-semibold text-[var(--color-dark-neutral-1)]">
              SGD 4,887
            </p>
          </div>
        </div>
        <div className="flex items-end gap-1">
          {[28, 44, 32, 52, 38, 46, 34].map((h, i) => (
            <div
              key={i}
              className="flex-1 rounded-t bg-[var(--color-theme-4)] transition-transform duration-200 hover:scale-105"
              style={{ height: `${h}px` }}
              aria-hidden
            />
          ))}
        </div>
        <p className="mt-1.5 text-center text-[9px] text-[var(--color-dark-neutral-2)]">
          Sun Mon Tue Wed Thu Fri Sat
        </p>
      </div>
    </div>
  );
}

export function Features({ sectionTag, heading, subheading, cards }: FeaturesProps) {
  return (
    <section id="features" className="bg-[var(--color-light-neutral-1)] py-20">
      <div className="mx-auto flex max-w-[1440px] flex-col gap-0 px-4 sm:px-10 lg:px-20">
        <AnimateInView>
          <div className="flex max-w-[851px] flex-col items-center gap-4 text-center">
            <p className="text-sm font-medium uppercase tracking-wide text-[var(--color-dark-neutral-2)]">
              {sectionTag}
            </p>
            <h2 className="font-heading text-3xl font-semibold leading-tight text-[var(--color-dark-neutral-1)] md:text-4xl">
              {heading}
            </h2>
            <p className="text-base leading-relaxed text-[var(--color-dark-neutral-2)]">
              {subheading}
            </p>
          </div>
        </AnimateInView>

        <div className="mt-16 flex flex-col gap-0">
          {cards.map((card, i) => {
            const zIndex = 10 + i * 10;
            return (
              <div
                key={i}
                className={`min-h-[50vh] lg:min-h-[80vh] ${card.bgClass} flex flex-col gap-8 px-0 py-12 lg:flex-row lg:items-center lg:justify-between lg:gap-12 lg:py-24`}
              >
                <AnimateInView
                  delay={i === 0 ? 0 : (i === 1 ? 1 : 2)}
                  variant="scaleIn"
                  className="lg:max-w-md lg:flex-1"
                >
                  <div className="flex flex-col items-start gap-4 text-left">
                    <h3 className="font-heading text-2xl font-semibold leading-tight text-[var(--color-dark-neutral-1)] md:text-3xl">
                      {card.title}
                    </h3>
                    <p className="text-base leading-relaxed text-[var(--color-dark-neutral-2)]">
                      {card.description}
                    </p>
                    {card.ctaLabel && card.ctaUrl && (
                      <a
                        href={card.ctaUrl}
                        className="focus-ring inline-flex items-center gap-1.5 rounded-lg border border-[var(--color-theme-4)] bg-white px-4 py-2.5 text-sm font-medium text-[var(--color-dark-neutral-1)] transition-colors hover:bg-[var(--color-theme-6)]"
                      >
                        {card.ctaLabel}
                        <span className="text-[var(--color-dark-neutral-2)]" aria-hidden>↗</span>
                      </a>
                    )}
                  </div>
                </AnimateInView>

                <div
                  className="card-hover sticky top-[140px] w-full lg:w-[36%] lg:max-w-md lg:shrink-0"
                  style={{ zIndex }}
                >
                  <AnimateInView
                    delay={i === 0 ? 1 : (i === 1 ? 2 : 3)}
                    variant="scaleIn"
                    rootMargin="0px 0px -80px 0px"
                  >
                    <FeatureVisualCard />
                  </AnimateInView>
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </section>
  );
}
