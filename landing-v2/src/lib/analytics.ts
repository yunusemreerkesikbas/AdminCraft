// Thin, SSR-safe GA4 (gtag.js) event helper for the landing site.
//
// Collection follows the repo standard documented in
// docs/3rd-party/google-analytics-ga4.md: a GA4 Measurement ID supplied via
// NEXT_PUBLIC_GA_ID, gated by Google Consent Mode v2 (see CookieConsent).
// When NEXT_PUBLIC_GA_ID is unset (e.g. local dev), every call is a no-op.

export const GA_MEASUREMENT_ID = process.env.NEXT_PUBLIC_GA_ID ?? "";

export const isAnalyticsEnabled = GA_MEASUREMENT_ID.length > 0;

type GtagFn = (...args: unknown[]) => void;

declare global {
  interface Window {
    dataLayer?: unknown[];
    gtag?: GtagFn;
  }
}

export function gtag(...args: unknown[]): void {
  if (typeof window === "undefined" || typeof window.gtag !== "function") return;
  window.gtag(...args);
}

/** Conversion / interaction events tracked across the landing experience. */
export type AnalyticsEvent =
  | "demo_open"
  | "demo_submit"
  | "demo_submit_error"
  | "newsletter_subscribe"
  | "cta_click"
  | "outbound_click";

export function track(event: AnalyticsEvent, params?: Record<string, unknown>): void {
  if (typeof window === "undefined") return;
  gtag("event", event, params ?? {});
}
