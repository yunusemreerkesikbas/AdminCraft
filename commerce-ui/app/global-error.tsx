"use client";

import { useEffect } from "react";
import { FALLBACK_LOCALE } from "@/lib/core/i18n/locale";

export default function GlobalError({
  error,
  reset,
}: {
  error: Error & { digest?: string };
  reset: () => void;
}) {
  useEffect(() => {
    console.error("[commerce-ui] Root error:", error);
  }, [error]);

  return (
    <html lang={FALLBACK_LOCALE}>
      <body>
        <main className="flex min-h-screen items-center justify-center px-6 text-center">
          <div className="max-w-md space-y-4">
            <h1 className="text-2xl font-semibold">Bir hata olustu</h1>
            <p className="text-sm text-neutral-500">
              Commerce storefront istegi tamamlayamadi.
            </p>
            <button
              onClick={reset}
              className="rounded-md bg-neutral-950 px-4 py-2 text-sm font-medium text-white"
            >
              Tekrar dene
            </button>
          </div>
        </main>
      </body>
    </html>
  );
}
