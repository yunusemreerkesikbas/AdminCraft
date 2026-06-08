"use client";

import Image from "next/image";
import { useEffect, useMemo, useState } from "react";
import { useTranslations, useLocale } from "next-intl";
import { ASSETS } from "@/lib/assets";
import { MenuIcon, XIcon, ArrowRightIcon } from "@/components/icons";
import { DemoRequestModal } from "@/components/modals/DemoRequestModal";
import { routing } from "@/i18n/routing";
import { track } from "@/lib/analytics";

function switchLocale(newLocale: string) {
  // Build regex dynamically from routing config — stays correct if locales are added
  const pattern = routing.locales.join("|");
  const path = window.location.pathname;
  const newPath = path.replace(new RegExp(`^/(${pattern})(/|$)`), `/${newLocale}$2`);
  window.location.href = newPath + window.location.search + window.location.hash;
}

export function Nav() {
  const t = useTranslations("nav");
  const locale = useLocale();
  const [modalOpen, setModalOpen] = useState(false);

  const NAV_LINKS = useMemo(
    () => [
      { label: t("neler"), href: "#features", id: "features" },
      { label: t("portfolyo"), href: "#social", id: "social" },
      { label: t("surec"), href: "#smart-assist", id: "smart-assist" },
      { label: t("docs"), href: "https://docs.craftive.io", id: "docs", external: true },
      { label: t("sss"), href: "#faq", id: "faq" },
    ],
    [t],
  );

  const [active, setActive] = useState<string>("");
  const [menuOpen, setMenuOpen] = useState(false);

  function openDemo() {
    track("demo_open", { location: "nav" });
    setModalOpen(true);
  }

  useEffect(() => {
    const ids = NAV_LINKS.map((l) => l.id);

    const update = () => {
      const refY = window.innerHeight * 0.4;
      let current = "";
      for (const id of ids) {
        const el = document.getElementById(id);
        if (!el) continue;
        const rect = el.getBoundingClientRect();
        if (rect.top <= refY && rect.bottom > refY) {
          current = id;
          break;
        }
      }
      setActive(current);
    };

    update();
    window.addEventListener("scroll", update, { passive: true });
    window.addEventListener("resize", update);
    return () => {
      window.removeEventListener("scroll", update);
      window.removeEventListener("resize", update);
    };
  }, [NAV_LINKS]);

  useEffect(() => {
    const onResize = () => { if (window.innerWidth >= 1024) setMenuOpen(false); };
    window.addEventListener("resize", onResize);
    return () => window.removeEventListener("resize", onResize);
  }, []);

  return (
    <>
    <DemoRequestModal open={modalOpen} onClose={() => setModalOpen(false)} locale={locale} />
    <nav className="fixed top-5 md:top-[30px] left-1/2 -translate-x-1/2 z-50 flex flex-col items-stretch px-0">
      {/* Pills row */}
      <div className="flex items-center gap-1.5 w-[calc(100vw-40px)] lg:w-auto justify-between lg:justify-start">
        {/* Brand */}
        <a
          href="#hero"
          className="flex items-center justify-center h-[52px] md:h-[58px] lg:h-[64px] px-4 md:px-5 lg:px-[26px] rounded-full bg-white shadow-pill flex-shrink-0"
        >
          <Image src={ASSETS.brand.logo} alt="Craftive" width={150} height={32} className="w-[96px] md:w-[120px] lg:w-[150px] h-auto" priority />
        </a>

        {/* Center links — desktop only (lg+) */}
        <div className="hidden lg:flex items-center h-[64px] px-3 rounded-full bg-white shadow-pill gap-1">
          {NAV_LINKS.map((link) => {
            const isActive = active === link.id;
            return (
              <a
                key={link.href}
                href={link.href}
                target={link.external ? "_blank" : undefined}
                rel={link.external ? "noreferrer" : undefined}
                className={`px-4 py-2.5 text-[15px] leading-none font-medium rounded-full transition-colors whitespace-nowrap ${
                  isActive ? "bg-surface text-ink" : "text-ink hover:bg-surface/60"
                }`}
              >
                {link.label}
              </a>
            );
          })}
        </div>

        {/* Demo CTA + lang selector + hamburger */}
        <div className="flex items-center h-[52px] md:h-[58px] lg:h-[64px] px-2 md:px-2.5 rounded-full bg-white shadow-pill gap-1 md:gap-1.5">
          {/* TR/EN toggle — desktop only (lg+): sliding ink pill */}
          <div
            className="hidden lg:flex relative items-center h-8 rounded-full p-0.5 flex-shrink-0"
            style={{ background: "#ede9e3" }}
          >
            {/* Sliding background pill */}
            <span
              aria-hidden
              className="absolute top-0.5 bottom-0.5 left-0.5 w-9 rounded-full pointer-events-none"
              style={{
                background: "#1a1a1a",
                transform: locale === "en" ? "translateX(36px)" : "translateX(0)",
                transition: "transform 220ms cubic-bezier(0.34,1.56,0.64,1)",
              }}
            />
            {(["tr", "en"] as const).map((lang) => {
              const isActive = locale === lang;
              return (
                <button
                  key={lang}
                  onClick={() => !isActive && switchLocale(lang)}
                  aria-label={lang === "tr" ? "Türkçe" : "English"}
                  aria-pressed={isActive}
                  className="relative z-10 w-9 h-full rounded-full text-[11px] font-black tracking-[0.09em] uppercase transition-colors duration-150 flex-shrink-0"
                  style={{ color: isActive ? "#fff" : "#a09a90" }}
                  onMouseEnter={(e) => { if (!isActive) e.currentTarget.style.color = "#1a1a1a"; }}
                  onMouseLeave={(e) => { if (!isActive) e.currentTarget.style.color = "#a09a90"; }}
                >
                  {lang.toUpperCase()}
                </button>
              );
            })}
          </div>

          {/* Demo CTA — desktop (lg+) */}
          <button
            onClick={openDemo}
            className="hidden lg:inline-flex h-[48px] px-5 rounded-full bg-ink text-white items-center gap-2 text-[14px] font-medium whitespace-nowrap hover:bg-ink/90 transition-colors flex-shrink-0"
          >
            {t("demoCta")}
            <ArrowRightIcon size={14} />
          </button>

          {/* Demo CTA — mobile + tablet */}
          <button
            onClick={openDemo}
            className="lg:hidden h-[40px] md:h-[44px] px-4 md:px-5 rounded-full bg-ink text-white inline-flex items-center text-[13px] md:text-[14px] font-medium whitespace-nowrap hover:bg-ink/90 transition-colors flex-shrink-0"
          >
            {t("demoCta")}
          </button>

          {/* Hamburger — mobile + tablet */}
          <button
            onClick={() => setMenuOpen((v) => !v)}
            className="lg:hidden size-[40px] md:size-[44px] rounded-2xl bg-surface text-ink flex items-center justify-center hover:bg-black/8 transition-colors flex-shrink-0"
            aria-label={menuOpen ? t("menuClose") : t("menuOpen")}
          >
            {menuOpen ? <XIcon size={15} /> : <MenuIcon size={16} />}
          </button>
        </div>
      </div>

      {/* Mobile + tablet dropdown — slides down when open */}
      <div
        className="lg:hidden grid transition-[grid-template-rows] duration-300 ease-out mt-1.5"
        style={{ gridTemplateRows: menuOpen ? "1fr" : "0fr" }}
      >
        <div className="overflow-hidden">
          <div className="rounded-2xl bg-white shadow-pill py-3 px-3">
            {NAV_LINKS.map((link) => (
              <a
                key={link.href}
                href={link.href}
                target={link.external ? "_blank" : undefined}
                rel={link.external ? "noreferrer" : undefined}
                onClick={() => setMenuOpen(false)}
                className="block px-3 py-[9px] text-[16px] font-medium text-ink rounded-xl hover:bg-surface transition-colors"
              >
                {link.label}
              </a>
            ))}

            {/* Lang switcher — mobile */}
            <div className="mt-2 pt-2.5 border-t border-[#f0ece4]">
              <p
                className="px-3 pb-1.5 text-[8px] font-black tracking-[0.26em] uppercase"
                style={{ color: "#c9c4bc" }}
              >
                {t("langLabel")}
              </p>
              <div className="flex gap-1 px-1 pb-0.5">
                {(["tr", "en"] as const).map((lang) => {
                  const isActive = locale === lang;
                  return (
                    <button
                      key={lang}
                      onClick={() => { if (!isActive) { setMenuOpen(false); switchLocale(lang); } }}
                      className="flex-1 flex items-center justify-between px-3 py-2 rounded-xl text-[13px] font-semibold transition-all duration-200"
                      style={{
                        background: isActive ? "#1a1a1a" : "transparent",
                        color: isActive ? "#fff" : "#8a847c",
                      }}
                      onMouseEnter={(e) => { if (!isActive) { e.currentTarget.style.background = "#f0ece4"; e.currentTarget.style.color = "#1a1a1a"; } }}
                      onMouseLeave={(e) => { if (!isActive) { e.currentTarget.style.background = "transparent"; e.currentTarget.style.color = "#8a847c"; } }}
                    >
                      <span>{lang === "tr" ? "Türkçe" : "English"}</span>
                      {isActive && (
                        <span
                          className="size-[5px] rounded-full flex-shrink-0"
                          style={{ background: "#0E121B" }}
                        />
                      )}
                    </button>
                  );
                })}
              </div>
            </div>
          </div>
        </div>
      </div>
    </nav>
    </>
  );
}
