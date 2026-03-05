import { LanguageInfo } from "./types";

export const FALLBACK_LOCALE = process.env.NEXT_PUBLIC_FALLBACK_LOCALE ?? "en";
export const AVAILABLE_MESSAGES = ["tr", "en"] as const;
export type AvailableLocale = (typeof AVAILABLE_MESSAGES)[number];

// lang URL segmenti için format validasyonu (middleware'de kullanılır)
export const isValidLocaleFormat = (value?: string): boolean =>
  !!value && /^[a-z]{2,3}$/.test(value);

// Tenant'ın dil kodunu URL-uyumlu formata çevirir: "TR" → "tr"
export const toUrlLocale = (code: string): string => code.toLowerCase();

// URL locale'ini API formatına çevirir: "tr" → "TR"
export const toApiLocale = (lang: string): string => lang.toUpperCase();

// RTL kontrolü site config'den gelir
export const isRtlByConfig = (lang: string, enabledLanguages: LanguageInfo[]): boolean =>
  enabledLanguages.find((l) => l.code.toLowerCase() === lang.toLowerCase())?.isRtl ?? false;

// Messages dosyası resolving: bilinmeyen dil → FALLBACK_LOCALE
export const resolveMessageLocale = (lang: string): AvailableLocale =>
  AVAILABLE_MESSAGES.includes(lang as AvailableLocale) ? (lang as AvailableLocale) : FALLBACK_LOCALE;

// Geriye dönük uyumluluk: cms-client.ts ve sayfa bileşenleri için
export const normalizeLanguage = (value?: string): string =>
  value ? value.toUpperCase() : FALLBACK_LOCALE.toUpperCase();

export const withLocalePath = (locale: string, path?: string): string => {
  if (!path) {
    return `/${locale}`;
  }
  const normalizedPath = path.startsWith("/") ? path : `/${path}`;
  return `/${locale}${normalizedPath}`;
};
