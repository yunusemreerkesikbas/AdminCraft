import Image from "next/image";
import { AnimateInView } from "./AnimateInView";

const BENEFITS_PHONE_URL = "https://framerusercontent.com/images/Pshc1MVHDcvxjjsk8Y9uxlV9ON8.png";

type BenefitCard = {
  title: string;
  description: string;
};

type BenefitsProps = {
  sectionTag: string;
  heading: string;
  subheading: string;
  cards: BenefitCard[];
};

export function Benefits({ sectionTag, heading, subheading, cards }: BenefitsProps) {
  return (
    <section id="benefits" className="bg-[var(--color-theme-6)] pt-[120px] pb-16 sm:pb-24">
      <div className="mx-auto flex max-w-[1280px] flex-col items-center gap-16 px-4 sm:px-10 lg:px-20">
        <AnimateInView>
          <div className="flex max-w-[950px] flex-col items-center gap-4 text-center">
            <p className="text-sm font-medium uppercase tracking-wide text-[var(--color-dark-neutral-2)]">
              {sectionTag}
            </p>
            <h2 className="font-heading text-3xl font-semibold leading-tight text-[var(--color-dark-neutral-1)] md:text-4xl lg:text-5xl">
              {heading}
            </h2>
            <p className="max-w-[600px] text-base leading-relaxed text-[var(--color-dark-neutral-2)]">
              {subheading}
            </p>
          </div>
        </AnimateInView>
        <AnimateInView delay={1} variant="scaleIn">
        <div className="grid w-full grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-[28%_38%_28%] lg:gap-0 lg:items-stretch">
          <div className="flex flex-col justify-center gap-6 py-16 lg:py-24">
            {cards.slice(0, 2).map((card, i) => (
              <div
                key={i}
                className="card-hover flex flex-col gap-4 rounded-xl bg-white p-6 shadow-sm"
              >
                <h3 className="font-heading text-lg font-medium text-[var(--color-dark-neutral-1)]">
                  {card.title}
                </h3>
                <p className="text-sm leading-relaxed text-[var(--color-dark-neutral-2)]">
                  {card.description}
                </p>
              </div>
            ))}
          </div>
          <div className="relative flex min-h-[400px] items-end justify-center overflow-hidden rounded-t-[32px] bg-[var(--color-shade)]/30 lg:min-h-[763px]">
            <Image
              src={BENEFITS_PHONE_URL}
              alt=""
              width={400}
              height={675}
              className="relative bottom-0 object-contain object-bottom"
              unoptimized
            />
          </div>
          <div className="flex flex-col justify-center gap-6 py-16 lg:py-24">
            {cards.slice(2, 4).map((card, i) => (
              <div
                key={i + 2}
                className="flex flex-col gap-4 rounded-xl bg-white p-6 shadow-sm animate-fade-in-up"
                style={{ animationDelay: `${0.25 + i * 0.05}s` }}
              >
                <h3 className="font-heading text-lg font-medium text-[var(--color-dark-neutral-1)]">
                  {card.title}
                </h3>
                <p className="text-sm leading-relaxed text-[var(--color-dark-neutral-2)]">
                  {card.description}
                </p>
              </div>
            ))}
          </div>
        </div>
        </AnimateInView>
      </div>
    </section>
  );
}
