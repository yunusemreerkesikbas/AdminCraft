"use client";

import { useEffect, useRef, useState } from "react";
import { useLocale, useTranslations } from "next-intl";
import {
  getCraftiveApiOrigin,
  NewsletterSubscribeError,
  subscribeNewsletter,
} from "@/lib/platform-api";
import { track } from "@/lib/analytics";
import { toLocaleTag } from "@/lib/utils";

export function NewsletterForm() {
  const locale = useLocale();
  const f = useTranslations("footer");
  const localeTag = toLocaleTag(locale);

  // Records when the form became interactive — used for the anti-bot
  // "submitted too fast" server check. Set on mount to keep render pure.
  const formStartedAtRef = useRef<number>(0);
  useEffect(() => {
    formStartedAtRef.current = Date.now();
  }, []);

  const [email, setEmail] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [submitted, setSubmitted] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const apiOrigin = getCraftiveApiOrigin();

  async function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setError(null);

    const fd = new FormData(e.currentTarget);
    const honeypot = String(fd.get("website") ?? "").trim();

    if (!apiOrigin) { setError(f("subscribeErrApiConfig")); return; }
    if (!email.trim()) return;

    setSubmitting(true);
    try {
      const result = await subscribeNewsletter(
        email.trim(),
        localeTag,
        honeypot,
        formStartedAtRef.current,
      );
      setSubmitted(true);
      track("newsletter_subscribe", { source: "LANDING_FOOTER", locale: localeTag });
      void result;
    } catch (err) {
      if (NewsletterSubscribeError.is(err)) {
        setError(err.apiMessage ?? (err.reason === "network" ? f("subscribeErrNetwork") : f("subscribeErrHttp")));
      } else if (err instanceof Error && err.message === "api_not_configured") {
        setError(f("subscribeErrApiConfig"));
      } else {
        setError(f("subscribeErrHttp"));
      }
    } finally {
      setSubmitting(false);
    }
  }

  if (submitted) {
    return (
      <p className="text-[13px] text-ink max-w-sm py-3">
        {f("subscribeSuccess")}
      </p>
    );
  }

  return (
    <form onSubmit={(e) => void handleSubmit(e)} className="max-w-sm">
      {/* Honeypot — invisible bot trap; value forwarded to backend for server-side validation */}
      <div
        aria-hidden="true"
        style={{ position: "absolute", left: "-9999px", width: "1px", height: "1px", overflow: "hidden" }}
      >
        <input name="website" type="text" tabIndex={-1} autoComplete="off" defaultValue="" />
      </div>

      <div className="flex items-center gap-2">
        <input
          type="email"
          required
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          disabled={submitting || !apiOrigin}
          autoComplete="email"
          maxLength={255}
          placeholder={f("emailPlaceholder")}
          className="flex-1 px-4 py-3 rounded-full border border-line text-[14px] focus:border-ink/40 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ink/30 disabled:opacity-40"
        />
        <button
          type="submit"
          disabled={submitting || !apiOrigin}
          className="px-5 py-3 rounded-full bg-ink text-white text-[13px] font-medium hover:bg-ink/90 transition-colors whitespace-nowrap disabled:opacity-40 disabled:pointer-events-none"
        >
          {submitting ? f("subscribing") : f("subscribe")}
        </button>
      </div>
      {error && (
        <p role="alert" className="mt-2 text-[12px]" style={{ color: "#b32020" }}>
          {error}
        </p>
      )}
    </form>
  );
}
