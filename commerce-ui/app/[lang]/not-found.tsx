import { getLocale, getTranslations } from "next-intl/server";
import type { Metadata } from "next";
import { ActionLink } from "@/components/ui/StorefrontPrimitives";
import {
  FALLBACK_LOCALE,
  isBundledLocale,
  withLocalePath,
} from "@/lib/core/i18n/locale";

export const metadata: Metadata = {
  robots: { index: false, follow: false },
};

export default async function NotFound() {
  const requestLocale = await getLocale().catch(() => FALLBACK_LOCALE);
  const locale = isBundledLocale(requestLocale) ? requestLocale : FALLBACK_LOCALE;
  const translate = await getTranslations({ locale, namespace: "NotFound" });

  return (
    <main
      id="main-content"
      className="commerce-container flex min-h-[70vh] items-center justify-center py-16 text-center"
    >
      <section className="surface-panel max-w-md px-6 py-8">
        <p className="eyebrow">{translate("eyebrow")}</p>
        <h1 className="mt-3 text-3xl font-semibold">{translate("title")}</h1>
        <p className="mt-3 text-sm text-[var(--muted)]">
          {translate("description")}
        </p>
        <div className="page-actions justify-center">
          <ActionLink
            href={withLocalePath(locale)}
            label={translate("action")}
            variant="secondary"
          />
        </div>
      </section>
    </main>
  );
}
