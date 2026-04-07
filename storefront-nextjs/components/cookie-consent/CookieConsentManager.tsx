"use client";

import { useTranslations } from "next-intl";
import Script from "next/script";
import { useEffect, useState } from "react";

declare global {
  interface Window {
    gtag?: (...args: unknown[]) => void;
  }
}

const STORAGE_KEY = "craftive_cookie_consent";

const CONSENT_GRANTED = {
  analytics_storage: "granted",
  ad_storage: "granted",
  ad_user_data: "granted",
  ad_personalization: "granted",
} as const;

const CONSENT_DENIED = {
  analytics_storage: "denied",
  ad_storage: "denied",
  ad_user_data: "denied",
  ad_personalization: "denied",
} as const;

interface Props {
  gaId?: string;
  cookieConsentEnabled: boolean;
  cookieConsentText?: string | null;
}

export function CookieConsentManager({
  gaId,
  cookieConsentEnabled,
  cookieConsentText,
}: Props) {
  const translate = useTranslations("CookieConsent");

  const [consented, setConsented] = useState<boolean | null | undefined>(
    undefined,
  );

  useEffect(() => {
    const stored = localStorage.getItem(STORAGE_KEY);
    if (stored === null) {
      setConsented(null);
    } else {
      const accepted = stored === "true";
      setConsented(accepted);
      if (accepted) {
        window.gtag?.("consent", "update", CONSENT_GRANTED);
      }
    }
  }, []);

  const accept = () => {
    localStorage.setItem(STORAGE_KEY, "true");
    setConsented(true);
    window.gtag?.("consent", "update", CONSENT_GRANTED);
  };

  const reject = () => {
    localStorage.setItem(STORAGE_KEY, "false");
    setConsented(false);
    window.gtag?.("consent", "update", CONSENT_DENIED);
  };

  if (consented === undefined) return null;

  const shouldLoadGA = !!gaId;
  const showBanner = cookieConsentEnabled && consented === null;

  return (
    <>
      {shouldLoadGA && (
        <>
          <Script
            src={`https://www.googletagmanager.com/gtag/js?id=${encodeURIComponent(gaId!)}`}
            strategy="afterInteractive"
          />
          <Script id="google-analytics" strategy="afterInteractive">{`
            window.dataLayer = window.dataLayer || [];
            function gtag(){dataLayer.push(arguments);}
            gtag('js', new Date());
            gtag('config', '${gaId}');
          `}</Script>
        </>
      )}
      {showBanner && (
        <div className="fixed bottom-0 left-0 right-0 z-50 border-t border-gray-200 bg-white shadow-lg">
          <div className="mx-auto flex max-w-7xl items-center justify-between gap-4 px-4 py-4">
            <div className="flex flex-1 items-center gap-3">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                viewBox="0 0 60 60"
                className="h-8 w-8 shrink-0"
                aria-hidden="true"
              >
                <circle cx="30" cy="30" r="28" fill="#FFE6A1" />
                <circle
                  cx="30"
                  cy="30"
                  r="28"
                  fill="#FFD796"
                  fillOpacity="0.5"
                />
                <circle cx="19" cy="18" r="6" fill="#B97850" />
                <circle cx="38" cy="14" r="4" fill="#B97850" />
                <circle cx="44" cy="30" r="5" fill="#B97850" />
                <circle cx="34" cy="42" r="6" fill="#B97850" />
                <circle cx="18" cy="38" r="4" fill="#B97850" />
                <circle cx="28" cy="28" r="3" fill="#A5694B" />
                <circle cx="20" cy="18" r="1.5" fill="#A5694B" />
                <circle cx="38" cy="14" r="1.5" fill="#A5694B" />
                <circle cx="44" cy="30" r="2" fill="#A5694B" />
                <circle cx="34" cy="42" r="2" fill="#A5694B" />
                <circle cx="18" cy="38" r="1.5" fill="#A5694B" />
              </svg>
              <p className="text-sm text-gray-700">{cookieConsentText}</p>
            </div>
            <div className="flex shrink-0 gap-2">
              <button
                type="button"
                onClick={reject}
                className="rounded-lg border border-gray-300 px-5 py-2 text-sm font-medium text-gray-700 transition-colors hover:bg-gray-50"
              >
                {translate("reject")}
              </button>
              <button
                type="button"
                onClick={accept}
                className="rounded-lg bg-gray-900 px-5 py-2 text-sm font-medium text-white transition-colors hover:bg-gray-700"
              >
                {translate("accept")}
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}
