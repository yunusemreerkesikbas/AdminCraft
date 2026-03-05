"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { LanguageInfo } from "@/lib/types";
import { toUrlLocale } from "@/lib/i18n";

interface Props {
  enabledLanguages: LanguageInfo[];
  currentLang: string;
}

export default function LanguageSwitcher({ enabledLanguages, currentLang }: Props) {
  const pathname = usePathname();

  const switchPath = (targetLang: string): string => {
    // pathname: /tr/products/abc → /en/products/abc
    const segments = pathname.split("/").filter(Boolean);
    segments[0] = targetLang;
    return `/${segments.join("/")}`;
  };

  if (enabledLanguages.length <= 1) return null;

  return (
    <div className="flex items-center gap-2 text-sm">
      {enabledLanguages.map((lang) => {
        const urlCode = toUrlLocale(lang.code);
        const isActive = urlCode === currentLang;
        return (
          <Link
            key={lang.code}
            href={switchPath(urlCode)}
            className={isActive ? "font-semibold text-slate-900" : "text-slate-500 hover:text-slate-900"}
          >
            {lang.nativeName}
          </Link>
        );
      })}
    </div>
  );
}
