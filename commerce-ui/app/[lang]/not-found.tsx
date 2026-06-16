import { getTranslations, setRequestLocale } from "next-intl/server";
import type { Metadata } from "next";
import { FALLBACK_LOCALE } from "@/lib/core/i18n/locale";

export const metadata: Metadata = {
  robots: { index: false, follow: false },
};

export default async function NotFound() {
  setRequestLocale(FALLBACK_LOCALE);
  const translate = await getTranslations("NotFound");

  return (
    <section className="mx-auto flex min-h-[60vh] max-w-md flex-col items-center justify-center px-6 text-center">
      <p className="text-xs font-semibold uppercase tracking-[0.18em] text-neutral-500">
        404
      </p>
      <h1 className="mt-3 text-2xl font-semibold">{translate("title")}</h1>
      <p className="mt-2 text-sm text-neutral-500">
        {translate("description")}
      </p>
    </section>
  );
}
