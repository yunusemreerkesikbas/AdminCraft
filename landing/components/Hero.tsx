import Link from "next/link";
import Image from "next/image";
import siteConfig from "@/config/site.json";

const HERO_IMAGE_URL = "https://framerusercontent.com/images/wS0wlJJf9F4AMClu9L04pmsdQ6o.png";

type HeroContent = {
  headline: string;
  subheadline: string;
  primaryCta: string;
  secondaryCta: string;
};

type HeroProps = {
  content: HeroContent;
  locale: string;
};

export function Hero({ content, locale }: HeroProps) {
  const base = `/${locale}`;

  return (
    <section className="relative overflow-hidden bg-[var(--color-light-neutral-1)]">
      <div className="pointer-events-none absolute inset-0 bg-[radial-gradient(ellipse_80%_50%_at_50%_-20%,var(--color-theme-6)_0%,transparent_50%)]" aria-hidden />
      <div className="relative mx-auto flex w-full max-w-[1440px] flex-col items-center gap-20 px-4 pb-[60px] pt-[160px] sm:px-10 md:gap-20 lg:px-20">
        <div className="flex w-full max-w-[52%] min-w-0 flex-col items-center gap-8 text-center lg:max-w-[745px]">
          <div className="flex w-full flex-col items-center gap-8 animate-fade-in-up">
            <div className="flex flex-col gap-5">
              <h1 className="heading-1 text-balance text-[var(--color-dark-neutral-1)]">
                {content.headline}
              </h1>
              <p className="text-base leading-[1.5] text-[var(--color-dark-neutral-2)]">
                {content.subheadline}
              </p>
            </div>
            <div className="flex flex-wrap justify-center gap-3 pt-0 animate-fade-in-up animate-fade-in-up-delay-1">
              <a
                href={`mailto:${siteConfig.contactEmail}?subject=Demo%20Talebi`}
                className="btn-primary"
              >
                {content.primaryCta}
              </a>
              <Link href={`${base}#features`} className="btn-secondary">
                {content.secondaryCta}
              </Link>
            </div>
          </div>
        </div>
        <div className="relative w-full max-w-[90%] animate-fade-in-up animate-fade-in-up-delay-2 lg:max-w-[52%]">
          <Image
            src={HERO_IMAGE_URL}
            alt=""
            width={900}
            height={574}
            className="animate-float h-auto max-h-[574px] w-full object-contain"
            priority
            unoptimized
          />
        </div>
      </div>
    </section>
  );
}
