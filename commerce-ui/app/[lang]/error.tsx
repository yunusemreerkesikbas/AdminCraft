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
    <section className="mx-auto flex min-h-[60vh] max-w-md flex-col items-center justify-center gap-4 px-6 text-center">
      <h1 className="text-2xl font-semibold">{translate("title")}</h1>
      <p className="text-sm text-neutral-500">{translate("description")}</p>
      <button
        onClick={reset}
        className="rounded-md bg-neutral-950 px-4 py-2 text-sm font-medium text-white"
      >
        {translate("retry")}
      </button>
    </section>
  );
}
