import type { MetadataRoute } from "next";
import { getSitemapPages } from "@/lib/core/cms/client";
import { loadSiteConfig } from "@/lib/core/cms/loaders";
import { toUrlLocale } from "@/lib/core/i18n/locale";

export default async function sitemap(): Promise<MetadataRoute.Sitemap> {
  try {
    const site = await loadSiteConfig();
    if (!site.searchEngine?.sitemapEnabled || !site.canonicalBaseUrl) {
      return [];
    }

    const base = site.canonicalBaseUrl.replace(/\/$/, "");
    const entryGroups = await Promise.all(
      site.enabledLanguages.map(async ({ code }) => {
        const lang = toUrlLocale(code);

        try {
          const pages = await getSitemapPages(lang);
          return (pages ?? []).map((page) => ({
            url: `${base}/${lang}${page.canonicalUrl}`,
            lastModified: new Date(page.updatedAt),
            changeFrequency: "weekly" as const,
            priority: page.canonicalUrl === "/" ? 1 : 0.8,
          }));
        } catch (error) {
          console.warn(`[sitemap] Failed to load sitemap entries for locale "${lang}".`, error);
          return [];
        }
      }),
    );

    return entryGroups.flat();
  } catch (error) {
    console.warn("[sitemap] Falling back to an empty sitemap because CMS is unavailable.", error);
    return [];
  }
}
