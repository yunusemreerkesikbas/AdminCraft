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
    console.error("[Storefront] Unhandled error:", error);
  }, [error]);

  return (
    <div className="flex min-h-[60vh] flex-col items-center justify-center gap-4 text-center">
      <h2 className="text-2xl font-semibold">{translate("title")}</h2>
      <p className="text-slate-500">{translate("description")}</p>
      <button
        onClick={reset}
        className="rounded-md bg-slate-900 px-4 py-2 text-white hover:bg-slate-800"
      >
        {translate("retry")}
      </button>
    </div>
  );
}
