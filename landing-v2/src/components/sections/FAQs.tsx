"use client";

import { useMemo, useState } from "react";
import { useTranslations } from "next-intl";
import { PlusIcon, HeadsetIcon } from "@/components/icons";
import { Reveal } from "@/components/Reveal";

type Item = { q: string; a: string };

export function FAQs() {
  const t = useTranslations("faq");
  const items = useMemo(() => t.raw("items") as Item[], [t]);
  const [open, setOpen] = useState(0);

  return (
    <section id="faq" className="bg-white py-20">
      <div className="max-w-6xl mx-auto px-6 grid grid-cols-1 md:grid-cols-[1fr_2fr] gap-12">
        <div>
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
          <Reveal delay={140}>
            <div className="mt-8 rounded-3xl bg-surface p-7 max-w-xs">
              <span className="size-11 rounded-full bg-[#0E121B] flex items-center justify-center">
                <HeadsetIcon size={20} className="text-white" />
              </span>
              <p className="mt-4 text-ink text-[18px] font-medium">{t("contactTitle")}</p>
              <a
                href="https://wa.me/905394204255?text=Merhaba%2C%20Craftive%20hakk%C4%B1nda%20bilgi%20almak%20istiyorum."
                target="_blank"
                rel="noreferrer"
                className="mt-4 inline-flex items-center gap-2 px-6 py-3 rounded-full bg-ink text-white text-[14px] font-medium hover:bg-ink/90 transition-colors"
              >
                {t("contactCta")}
              </a>
            </div>
          </Reveal>
        </div>

        <Reveal delay={120}>
          <div className="space-y-3">
            {items.map((item, i) => {
              const isOpen = open === i;
              return (
                <button
                  key={i}
                  onClick={() => setOpen(isOpen ? -1 : i)}
                  className="w-full text-left rounded-2xl border border-line bg-white px-6 py-5 hover:bg-surface/50 transition-colors"
                >
                  <div className="flex items-center justify-between gap-4">
                    <span className="text-ink text-[16px] font-medium">{item.q}</span>
                    <span
                      className={`flex-shrink-0 size-8 rounded-full bg-surface flex items-center justify-center text-ink-muted transition-transform ${
                        isOpen ? "rotate-45" : ""
                      }`}
                    >
                      <PlusIcon size={16} />
                    </span>
                  </div>
                  <div
                    className="grid transition-[grid-template-rows] duration-300 ease-out"
                    style={{ gridTemplateRows: isOpen ? "1fr" : "0fr" }}
                  >
                    <div className="overflow-hidden">
                      <p className="pt-4 text-ink-muted text-[15px] leading-relaxed">{item.a}</p>
                    </div>
                  </div>
                </button>
              );
            })}
          </div>
        </Reveal>
      </div>
    </section>
  );
}
