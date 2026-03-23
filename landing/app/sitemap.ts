import type { MetadataRoute } from "next";
import { SITE_URL } from "@/lib/site";

export const dynamic = "force-static";

const BUILD_DATE = new Date("2026-03-10");

export default function sitemap(): MetadataRoute.Sitemap {
  return [
    {
      url: `${SITE_URL}/en`,
      lastModified: BUILD_DATE,
      alternates: {
        languages: {
          tr: `${SITE_URL}/tr`,
          en: `${SITE_URL}/en`,
        },
      },
    },
    {
      url: `${SITE_URL}/tr`,
      lastModified: BUILD_DATE,
      alternates: {
        languages: {
          tr: `${SITE_URL}/tr`,
          en: `${SITE_URL}/en`,
        },
      },
    },
  ];
}
