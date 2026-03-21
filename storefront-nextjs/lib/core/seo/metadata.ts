import type { Metadata } from "next";
import type { PageDeliveryResponse, SiteDeliveryResponse } from "../../types";
import { buildMediaUrl } from "../media/url";

const SAFE_DEFAULT_ROBOTS = "noindex,nofollow";

const parseRobotsTag = (
  tag?: string | null,
  defaultRobots?: string | null,
): Metadata["robots"] => {
  const effective = tag || defaultRobots || SAFE_DEFAULT_ROBOTS;
  const lower = effective.toLowerCase();

  return {
    index: !lower.includes("noindex"),
    follow: !lower.includes("nofollow"),
  };
};

export const ensureTrailingSlash = (value: string): string =>
  value.endsWith("/") ? value : `${value}/`;

export const buildCanonicalUrl = (canonicalBaseUrl: string, path: string): string => {
  const normalizedPath = path.startsWith("/") ? path.slice(1) : path;
  return new URL(normalizedPath, ensureTrailingSlash(canonicalBaseUrl)).toString();
};

const resolveCanonicalUrl = (
  pageCanonicalUrl?: string | null,
  canonicalBaseUrl?: string,
  localizedPath?: string,
): string | undefined => {
  if (pageCanonicalUrl) {
    if (!canonicalBaseUrl || /^https?:\/\//i.test(pageCanonicalUrl)) {
      return pageCanonicalUrl;
    }

    return buildCanonicalUrl(canonicalBaseUrl, pageCanonicalUrl);
  }

  if (!canonicalBaseUrl || !localizedPath) {
    return undefined;
  }

  return buildCanonicalUrl(canonicalBaseUrl, localizedPath);
};

const toTwitterCard = (
  card?: string | null,
): "summary" | "summary_large_image" | "player" | "app" | undefined => {
  const normalized = card?.trim().toLowerCase();

  switch (normalized) {
    case "summary":
    case "summary_large_image":
    case "player":
    case "app":
      return normalized;
    default:
      return undefined;
  }
};

const extractBasePath = (localizedPath?: string): string => {
  if (!localizedPath) {
    return "/";
  }

  const match = localizedPath.match(/^\/[a-z]{2,3}(\/.*)?$/i);
  return match?.[1] ?? "/";
};

const buildAlternateLanguages = (
  site: SiteDeliveryResponse,
  localizedPath?: string,
): Record<string, string> | undefined => {
  const { enabledLanguages, defaultLanguage, canonicalBaseUrl } = site;
  if (!canonicalBaseUrl || enabledLanguages.length <= 1) {
    return undefined;
  }

  const basePath = extractBasePath(localizedPath);
  const langs: Record<string, string> = {};

  for (const { code } of enabledLanguages) {
    const lang = code.toLowerCase();
    langs[lang] = buildCanonicalUrl(canonicalBaseUrl, `/${lang}${basePath}`);
  }

  const defaultLang = defaultLanguage.toLowerCase();
  if (langs[defaultLang]) {
    langs["x-default"] = langs[defaultLang];
  }

  return langs;
};

export const buildPageMetadata = (
  page: PageDeliveryResponse,
  site: SiteDeliveryResponse,
  localizedPath?: string,
): Metadata => {
  const siteTitle = site.seo?.title || site.siteName;
  const pageTitle = page.title || page.name;
  const separator = site.seo?.titleSeparator ?? " | ";
  const title =
    pageTitle && siteTitle && pageTitle !== siteTitle
      ? `${pageTitle}${separator}${siteTitle}`
      : pageTitle;
  const description = page.description || site.seo?.description || undefined;
  const canonical = resolveCanonicalUrl(page.canonicalUrl, site.canonicalBaseUrl, localizedPath);
  const ogImage = site.ogImageUrl ? buildMediaUrl(site.ogImageUrl) : undefined;
  const ogTitle = pageTitle || site.seo?.ogTitle || title;
  const ogDescription = page.description || site.seo?.ogDescription || site.seo?.description || undefined;
  const twitterCard = toTwitterCard(site.seo?.twitterCard);

  return {
    title,
    description,
    keywords: site.seo?.keywords,
    robots: parseRobotsTag(page.robotTag, site.searchEngine?.defaultRobots),
    alternates: {
      canonical: canonical ?? undefined,
      languages: buildAlternateLanguages(site, localizedPath),
    },
    openGraph: {
      title: ogTitle,
      description: ogDescription,
      images: ogImage ? [ogImage] : undefined,
    },
    twitter: {
      card: twitterCard,
      title: ogTitle,
      description: ogDescription,
      images: ogImage ? [ogImage] : undefined,
    },
  };
};
