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

export const withLocalePath = (locale: string, path?: string): string => {
  if (!path) {
    return `/${locale}`;
  }

  const normalizedPath = path.startsWith("/") ? path : `/${path}`;
  return `/${locale}${normalizedPath}`;
};
