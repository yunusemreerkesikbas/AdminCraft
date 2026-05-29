"use client";

import Script from "next/script";
import { GA_MEASUREMENT_ID, isAnalyticsEnabled } from "@/lib/analytics";

/**
 * Loads GA4 (gtag.js) with Google Consent Mode v2.
 *
 * Consent defaults to "denied" before any config call, so no analytics or ad
 * cookies are set until the visitor accepts via the cookie banner
 * (see CookieConsent, which calls gtag('consent','update', ...)).
 * `wait_for_update` buffers hits briefly so an early accept is not lost.
 *
 * Renders nothing when NEXT_PUBLIC_GA_ID is unset.
 */
export function GoogleAnalytics() {
  if (!isAnalyticsEnabled) return null;

  return (
    <>
      <Script
        id="ga-consent-default"
        strategy="afterInteractive"
        // Must run before gtag('config') so the denied defaults apply first.
        dangerouslySetInnerHTML={{
          __html: `
            window.dataLayer = window.dataLayer || [];
            function gtag(){dataLayer.push(arguments);}
            window.gtag = gtag;
            gtag('consent', 'default', {
              analytics_storage: 'denied',
              ad_storage: 'denied',
              ad_user_data: 'denied',
              ad_personalization: 'denied',
              wait_for_update: 500
            });
            gtag('js', new Date());
            gtag('config', '${GA_MEASUREMENT_ID}');
          `,
        }}
      />
      <Script
        id="ga-gtag"
        strategy="afterInteractive"
        src={`https://www.googletagmanager.com/gtag/js?id=${GA_MEASUREMENT_ID}`}
      />
    </>
  );
}
