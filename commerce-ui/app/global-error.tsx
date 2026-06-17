"use client";

import { useEffect } from "react";
import { FALLBACK_LOCALE } from "@/lib/core/i18n/locale";
import trMessages from "@/messages/tr.json";

export default function GlobalError({
  error,
  reset,
}: {
  error: Error & { digest?: string };
  reset: () => void;
}) {
  const copy = trMessages.Error;

  useEffect(() => {
    console.error("[commerce-ui] Root error:", error);
  }, [error]);

  return (
    <html lang={FALLBACK_LOCALE}>
      <body>
        <main
          id="main-content"
          className="commerce-shell flex min-h-screen items-center justify-center px-6 text-center"
        >
          <div className="surface-panel max-w-md px-6 py-8">
            <p className="eyebrow">{copy.title}</p>
            <h1 className="mt-3 text-3xl font-semibold">{copy.rootTitle}</h1>
            <p className="mt-3 text-sm text-[var(--muted)]">
              {copy.rootDescription}
            </p>
            <button
              onClick={reset}
              className="commerce-action mt-6"
            >
              {copy.retry}
            </button>
          </div>
        </main>
      </body>
    </html>
  );
}
