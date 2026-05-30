"use client";

import { useMemo, useState } from "react";
import { useTranslations } from "next-intl";
import { Reveal } from "@/components/Reveal";

const TECH_STACK = [
  { name: "Angular", icon: "/angular.svg" },
  { name: "TypeScript", icon: "/typescript.svg" },
  { name: "Next.js", icon: "/nextjs.svg" },
  { name: "Spring Boot", icon: "/spring-boot.svg" },
  { name: "MySQL", icon: "/mysql.svg" },
  { name: "Docker", icon: "/docker.svg" },
  { name: "Cloudflare", icon: "/cloudflare.svg" },
  { name: "DigitalOcean", icon: "/digitalocean.svg" },
  { name: "GitHub", icon: "/github.svg" },
];

type Testimonial = { quote: string; name: string; role: string };

export function Reviews() {
  const t = useTranslations("reviews");
  const testimonials = useMemo(() => t.raw("testimonials") as Testimonial[], [t]);
  const [showAll, setShowAll] = useState(false);

  return (
    <section id="reviews" className="bg-white py-14 md:py-20">
      <div className="max-w-6xl mx-auto px-6">

        {/* Tech stack label + marquee */}
        <p className="text-center text-[11px] font-semibold tracking-[0.22em] uppercase text-ink-faint mb-6">
          {t("techStackLabel")}
        </p>
        <div className="overflow-hidden mask-fade-x mb-12">
          <div className="flex gap-3 marquee-x" style={{ width: "max-content" }}>
            {[...TECH_STACK, ...TECH_STACK, ...TECH_STACK].map((tech, i) => (
              <div
                key={i}
                className="flex items-center gap-2.5 flex-shrink-0 px-5 py-2.5 rounded-full border border-line bg-white shadow-soft"
              >
                <img src={tech.icon} alt="" className="size-5 object-contain" />
                <span className="text-[12px] font-semibold tracking-[0.06em] uppercase text-ink-muted whitespace-nowrap">
                  {tech.name}
                </span>
              </div>
            ))}
          </div>
        </div>

        {/* Badge + headline */}
        <div className="text-center mb-10">
          <Reveal>
            <div className="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-surface mb-5">
              <span className="size-1.5 rounded-full bg-[#0E121B]" />
              <span className="text-[13px] font-medium text-ink-muted">{t("badge")}</span>
            </div>
          </Reveal>
          <Reveal delay={80}>
            <h2 className="font-heading text-[34px] md:text-[48px] leading-[1.1] font-medium tracking-[-0.02em] text-ink">
              {t("headline")}
            </h2>
          </Reveal>
        </div>

        {/* Testimonials grid */}
        <Reveal delay={100}>
          <div className="relative">
            <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
              {testimonials.map((tItem, i) => {
                const mobileHidden = !showAll && i > 3;
                const mobilePartial = !showAll && i === 3;
                return (
                  <div
                    key={i}
                    className={`rounded-2xl bg-surface p-6 flex flex-col ${
                      mobileHidden ? "hidden md:block" : ""
                    } ${mobilePartial ? "opacity-40 md:opacity-100" : ""}`}
                  >
                    <span className="font-heading text-[40px] leading-none text-ink/10 -mb-1">"</span>
                    <p className="text-ink text-[15px] leading-relaxed flex-1">{tItem.quote}</p>
                    <div className="mt-5 pt-4 border-t border-line">
                      <div className="text-ink font-semibold text-[13px] leading-tight">{tItem.name}</div>
                    </div>
                  </div>
                );
              })}
            </div>

            {!showAll && (
              <div className="md:hidden absolute bottom-0 inset-x-0 h-40 bg-gradient-to-t from-white via-white/80 to-transparent flex items-end justify-center pb-4">
                <button
                  onClick={() => setShowAll(true)}
                  className="px-6 py-3.5 rounded-full bg-ink text-white text-[14px] font-medium shadow-lg"
                >
                  {t("viewAll")}
                </button>
              </div>
            )}
          </div>
        </Reveal>


      </div>
    </section>
  );
}
