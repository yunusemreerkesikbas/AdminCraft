"use client";
import Image from "next/image";
import { useTranslations } from "next-intl";
import { ASSETS } from "@/lib/assets";
import { ArrowRightIcon } from "@/components/icons";
import { Reveal } from "@/components/Reveal";
import { DemoButton } from "@/components/DemoButton";
import { NewsletterForm } from "@/components/NewsletterForm";

type FooterLink = { label: string; href: string; external?: boolean };
type FooterCol = { title: string; links: FooterLink[] };

export function CTAFooter() {
  const t = useTranslations("ctaFooter");
  const f = useTranslations("footer");
  const columns = f.raw("columns") as FooterCol[];
  const year = new Date().getFullYear();

  return (
    <section id="cta-footer" className="relative bg-white">
      {/* CTA block (light cloud background) */}
      <div className="relative overflow-hidden bg-surface pt-24 pb-32">
        {/* Cloud texture top + bottom */}
        <Image src={ASSETS.clouds.medium} alt="" width={2988} height={588} className="absolute top-0 left-0 w-full opacity-60 pointer-events-none" />
        <Image src={ASSETS.clouds.big} alt="" width={2988} height={588} className="absolute bottom-0 left-0 w-full opacity-70 pointer-events-none" />

        <div className="relative max-w-6xl mx-auto px-6 grid grid-cols-1 md:grid-cols-2 gap-8 items-center min-h-[520px]">
          {/* Left: heading + buttons */}
          <div>
            <Reveal>
              <h2 className="font-heading text-[34px] md:text-[44px] leading-[1.1] font-medium tracking-[-0.02em] text-ink">
                {t("headline1")}<br />{t("headline2")}
              </h2>
            </Reveal>
            <Reveal delay={80}>
              <p className="mt-4 text-[17px] text-ink-muted max-w-sm leading-relaxed">
                {t("subhead")}
              </p>
            </Reveal>
            <Reveal delay={150}>
              <div className="mt-8 flex flex-row flex-wrap items-center gap-3">
                <DemoButton className="px-7 py-4 rounded-full bg-ink text-white text-[15px] font-medium flex items-center gap-2.5 hover:bg-ink/90 transition-colors shadow-card">
                  {t("primaryCta")}
                  <ArrowRightIcon size={16} />
                </DemoButton>
                <a
                  href="https://wa.me/905394204255?text=Merhaba%2C%20Craftive%20hakk%C4%B1nda%20bilgi%20almak%20istiyorum."
                  target="_blank"
                  rel="noreferrer"
                  className="px-7 py-4 rounded-full bg-black/5 text-ink text-[15px] font-medium flex items-center gap-2.5 hover:bg-black/10 transition-colors"
                >
                  {t("secondaryCta")}
                </a>
              </div>
            </Reveal>
          </div>

          {/* Right: app image */}
          <Reveal delay={120}>
            <div className="flex justify-center">
              <Image
                src="/images/icons/craftive-app.png"
                alt={t("handPhoneAlt")}
                width={420}
                height={860}
                className="w-[420px] md:w-[520px] h-auto drop-shadow-2xl"
              />
            </div>
          </Reveal>
        </div>
      </div>

      {/* Footer block */}
      <footer className="bg-white py-16">
        <div className="max-w-6xl mx-auto px-6 grid grid-cols-1 md:grid-cols-[1.5fr_repeat(3,1fr)] gap-12">
          <div>
            <div className="flex items-center gap-2 mb-5">
              <Image src={ASSETS.brand.logo} alt="Craftive" width={150} height={32} />
            </div>
            <p className="text-ink-muted text-[14px] mb-5 max-w-sm">{f("tagline")}</p>
            <NewsletterForm />
          </div>

          {columns.map((col) => (
            <div key={col.title}>
              <div className="text-ink text-[14px] font-medium mb-4">{col.title}</div>
              <ul className="space-y-3">
                {col.links.map((l) => (
                  <li key={l.label}>
                    <a
                      href={l.href}
                      target={l.external ? "_blank" : undefined}
                      rel={l.external ? "noreferrer" : undefined}
                      className="text-ink-muted hover:text-ink text-[14px] transition-colors"
                    >
                      {l.label}
                    </a>
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </div>

        <div className="mt-16 pt-8 border-t border-line max-w-6xl mx-auto px-6 flex items-center justify-between text-[12px] text-ink-faint">
          <span>{f("hosting")}</span>
          <span>{f("copyright", { year })}</span>
        </div>
      </footer>
    </section>
  );
}
