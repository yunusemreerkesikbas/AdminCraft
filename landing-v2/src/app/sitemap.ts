import type { MetadataRoute } from "next";
import { SITE_URL } from "@/lib/site";
import { routing } from "@/i18n/routing";

export const dynamic = "force-static";

// trailingSlash: true → all URLs end with "/".
const languages = Object.fromEntries(
  routing.locales.map((locale) => [locale, `${SITE_URL}/${locale}/`]),
);

const lastModified = new Date();

export default function sitemap(): MetadataRoute.Sitemap {
  return routing.locales.map((locale) => ({
    url: `${SITE_URL}/${locale}/`,
    lastModified,
    changeFrequency: "weekly",
    priority: locale === routing.defaultLocale ? 1 : 0.8,
    alternates: { languages },
  }));
}
