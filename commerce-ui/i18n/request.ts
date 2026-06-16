import { getRequestConfig } from "next-intl/server";
import { FALLBACK_LOCALE, requireMessageLocale } from "@/lib/core/i18n/locale";

export default getRequestConfig(async ({ requestLocale }) => {
  const rawLang = await requestLocale;
  const messageLocale = rawLang
    ? requireMessageLocale(rawLang)
    : FALLBACK_LOCALE;

  return {
    locale: messageLocale,
    messages: (await import(`../messages/${messageLocale}.json`)).default,
  };
});
