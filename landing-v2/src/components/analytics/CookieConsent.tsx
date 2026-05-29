"use client";

import { useEffect, useSyncExternalStore } from "react";
import { useTranslations } from "next-intl";
import { gtag, isAnalyticsEnabled } from "@/lib/analytics";

const STORAGE_KEY = "craftive_cookie_consent";
const CONSENT_EVENT = "craftive-consent-change";

function updateConsent(granted: boolean) {
  gtag("consent", "update", {
    analytics_storage: granted ? "granted" : "denied",
  });
}

// Subscribe to consent changes so useSyncExternalStore re-renders the banner
// without React state living inside an effect.
function subscribe(onChange: () => void) {
  window.addEventListener(CONSENT_EVENT, onChange);
  window.addEventListener("storage", onChange);
  return () => {
    window.removeEventListener(CONSENT_EVENT, onChange);
    window.removeEventListener("storage", onChange);
  };
}

function getStoredDecision(): string | null {
  return localStorage.getItem(STORAGE_KEY);
}

/**
 * KVKK / GDPR cookie consent banner wired to Google Consent Mode v2.
 *
 * GA4 starts with analytics_storage denied (see GoogleAnalytics). This banner
 * persists the visitor's decision under `craftive_cookie_consent` and flips
 * the consent state without reloading. Mirrors the storefront
 * CookieConsentManager contract (docs/3rd-party/google-analytics-ga4.md).
 */
export function CookieConsent() {
  const t = useTranslations("consent");
  // External store (localStorage) → "true" | "false" | null. Server renders null.
  const decision = useSyncExternalStore(subscribe, getStoredDecision, () => null);

  // Restore previously granted consent on mount (external-system sync, no setState).
  useEffect(() => {
    if (isAnalyticsEnabled && decision === "true") updateConsent(true);
  }, [decision]);

  function decide(granted: boolean) {
    localStorage.setItem(STORAGE_KEY, granted ? "true" : "false");
    updateConsent(granted);
    window.dispatchEvent(new Event(CONSENT_EVENT));
  }

  if (!isAnalyticsEnabled || decision !== null) return null;

  return (
    <div
      role="region"
      aria-live="polite"
      aria-label={t("title")}
      className="fixed bottom-4 left-4 right-4 sm:left-auto sm:right-6 sm:bottom-6 z-[100]
                 sm:max-w-[420px] rounded-2xl bg-white border border-line
                 shadow-[0_24px_60px_-20px_rgba(0,0,0,0.35)] p-5"
    >
      <p className="font-heading text-[16px] font-medium text-ink">{t("title")}</p>
      <p className="mt-2 text-[13px] leading-relaxed text-ink-muted">
        {t("description")}{" "}
        <a
          href="mailto:hello@craftive.io"
          className="underline underline-offset-2 hover:text-ink transition-colors"
        >
          {t("privacyLabel")}
        </a>
      </p>
      <div className="mt-4 flex items-center gap-2.5">
        <button
          type="button"
          onClick={() => decide(true)}
          className="flex-1 px-4 py-2.5 rounded-full bg-ink text-white text-[13px] font-medium
                     hover:bg-ink/90 transition-colors"
        >
          {t("accept")}
        </button>
        <button
          type="button"
          onClick={() => decide(false)}
          className="flex-1 px-4 py-2.5 rounded-full bg-surface text-ink text-[13px] font-medium
                     hover:bg-surface/70 transition-colors"
        >
          {t("reject")}
        </button>
      </div>
    </div>
  );
}
