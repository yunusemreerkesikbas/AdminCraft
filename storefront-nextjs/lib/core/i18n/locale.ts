import type { LanguageInfo, SiteDeliveryResponse } from "../../types";

export const BUNDLED_MESSAGE_LOCALES = ["tr", "en"] as const;
export type BundledLocale = (typeof BUNDLED_MESSAGE_LOCALES)[number];
export const FALLBACK_LOCALE: BundledLocale = BUNDLED_MESSAGE_LOCALES[0];

export const isBundledLocale = (value: string): value is BundledLocale =>
  BUNDLED_MESSAGE_LOCALES.some((locale) => locale === value);

export const isValidLocaleFormat = (value?: string): boolean =>
  !!value && /^[a-z]{2,3}$/.test(value);

export const toUrlLocale = (code: string): string => code.toLowerCase();

export const toApiLocale = (lang: string): string => {
  const normalizedLang = lang.trim().toLowerCase();
  if (!isValidLocaleFormat(normalizedLang)) {
    throw new Error(`Invalid locale format: "${lang}"`);
  }

  return normalizedLang.toUpperCase();
};

export const isRtlByConfig = (lang: string, enabledLanguages: LanguageInfo[]): boolean =>
  enabledLanguages.find((language) => language.code.toLowerCase() === lang.toLowerCase())?.isRtl ?? false;

export const isLocaleEnabled = (lang: string, enabledLanguages: LanguageInfo[]): boolean =>
  enabledLanguages.some((language) => language.code.toLowerCase() === lang.toLowerCase());

export const resolveSiteDefaultLocale = (
  site: Pick<SiteDeliveryResponse, "defaultLanguage" | "enabledLanguages">,
): string => {
  const defaultLocale = toUrlLocale(site.defaultLanguage);

  if (!isLocaleEnabled(defaultLocale, site.enabledLanguages)) {
    throw new Error(`Site default locale "${defaultLocale}" is not enabled.`);
  }

  return defaultLocale;
};

export const requireMessageLocale = (lang: string): BundledLocale => {
  const normalizedLang = lang.trim().toLowerCase();
  if (!isValidLocaleFormat(normalizedLang)) {
    throw new Error(`Invalid locale format: "${lang}"`);
  }

  if (!isBundledLocale(normalizedLang)) {
    throw new Error(`Missing message catalog for locale "${normalizedLang}".`);
  }

  return normalizedLang;
};

export const normalizeLanguage = (value: string | undefined): string =>
  value ? toApiLocale(value) : "";

export const withLocalePath = (locale: string, path?: string): string => {
  if (!path) {
    return `/${locale}`;
  }

  const normalizedPath = path.startsWith("/") ? path : `/${path}`;
  return `/${locale}${normalizedPath}`;
};
