import { getRequestConfig } from "next-intl/server";
import { FALLBACK_LOCALE, resolveMessageLocale } from "@/lib/i18n";

export default getRequestConfig(async ({ requestLocale }) => {
  const lang = await requestLocale;
  const messageLocale = resolveMessageLocale(lang ?? FALLBACK_LOCALE);
  return {
    locale: lang ?? FALLBACK_LOCALE,
    messages: (await import(`../messages/${messageLocale}.json`)).default,
  };
});
