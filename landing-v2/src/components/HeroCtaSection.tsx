"use client";

import { useState } from "react";
import { useLocale } from "next-intl";
import { PlayIcon } from "@/components/icons";
import { DemoRequestModal } from "@/components/modals/DemoRequestModal";
import { track } from "@/lib/analytics";

type Props = {
  primaryCta: string;
  secondaryCta: string;
};

export function HeroCtaSection({ primaryCta, secondaryCta }: Props) {
  const locale = useLocale();
  const [modalOpen, setModalOpen] = useState(false);

  return (
    <>
      <div className="mt-9 grid grid-cols-1 md:grid-cols-2 gap-4 md:gap-3">
        <button
          onClick={() => {
            track("demo_open", { location: "hero" });
            setModalOpen(true);
          }}
          className="w-full px-7 py-[13px] md:py-4 bg-white text-ink rounded-full text-[16px] md:text-[18px] font-medium leading-none hover:bg-white/90 active:scale-[0.98] transition-all"
        >
          {primaryCta}
        </button>
        <a
          href="#smart-assist"
          onClick={() => track("cta_click", { cta_id: "hero_secondary", location: "hero" })}
          className="w-full px-7 py-[13px] md:py-4 bg-white/10 backdrop-blur-md text-white rounded-full text-[16px] md:text-[18px] font-medium leading-none flex items-center justify-center gap-2 hover:bg-white/20 transition-colors border border-white/15"
        >
          <span className="size-5 rounded-full bg-white text-ink flex items-center justify-center flex-shrink-0">
            <PlayIcon size={10} />
          </span>
          {secondaryCta}
        </a>
      </div>

      <DemoRequestModal
        open={modalOpen}
        onClose={() => setModalOpen(false)}
        locale={locale}
      />
    </>
  );
}
