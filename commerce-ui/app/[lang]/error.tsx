"use client";

import { useTranslations } from "next-intl";
import { useEffect } from "react";

export default function LocaleError({
  error,
  reset,
}: {
  error: Error & { digest?: string };
  reset: () => void;
}) {
  const translate = useTranslations("Error");

  useEffect(() => {
    console.error("[commerce-ui] Route error:", error);
  }, [error]);

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
        <button onClick={reset} className="commerce-action mt-6">
          {translate("retry")}
        </button>
      </section>
    </main>
  );
}
