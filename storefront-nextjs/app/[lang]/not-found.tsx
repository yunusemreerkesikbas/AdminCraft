import { headers } from "next/headers";
import { setRequestLocale, getTranslations } from "next-intl/server";
import type { Metadata } from "next";
import { isValidLocaleFormat, requireMessageLocale, FALLBACK_LOCALE } from "@/lib/core/i18n/locale";

export const metadata: Metadata = {
  robots: { index: false, follow: false },
};

export default async function NotFound() {
  const requestHeaders = await headers();
  const rawLang = requestHeaders.get("x-lang");
  const lang = rawLang && isValidLocaleFormat(rawLang) ? requireMessageLocale(rawLang) : FALLBACK_LOCALE;

  setRequestLocale(lang);
  const translate = await getTranslations("NotFound");

  return (
    <div className="py-24 text-center">
      <h1 className="text-2xl font-semibold">{translate("title")}</h1>
      <p className="mt-2 text-sm text-slate-500">{translate("description")}</p>
    </div>
  );
}
