"use client";

import { useParams } from "next/navigation";
import enMessages from "@/messages/en.json";
import trMessages from "@/messages/tr.json";
import {
  FALLBACK_LOCALE,
  isBundledLocale,
  type BundledLocale,
} from "@/lib/core/i18n/locale";

const loadingMessages = {
  en: enMessages.Loading,
  tr: trMessages.Loading,
} satisfies Record<BundledLocale, typeof trMessages.Loading>;

export default function Loading() {
  const params = useParams<{ lang?: string | string[] }>();
  const lang = Array.isArray(params.lang) ? params.lang[0] : params.lang;
  const locale = lang && isBundledLocale(lang) ? lang : FALLBACK_LOCALE;
  const copy = loadingMessages[locale];

  return (
    <main
      id="main-content"
      className="commerce-container flex min-h-screen items-center justify-center px-6"
    >
      <div role="status" aria-live="polite">
        <span className="sr-only">{copy.label}</span>
        <div className="loading-bar" aria-hidden="true">
          <div className="loading-bar__fill" />
        </div>
      </div>
    </main>
  );
}
