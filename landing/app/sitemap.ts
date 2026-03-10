import type { MetadataRoute } from "next";

export const BASE_URL = "https://www.craftive.io";

const BUILD_DATE = new Date("2026-03-10");

export default function sitemap(): MetadataRoute.Sitemap {
  return [
    {
      url: `${BASE_URL}/en`,
      lastModified: BUILD_DATE,
      alternates: {
        languages: {
          tr: `${BASE_URL}/tr`,
          en: `${BASE_URL}/en`,
        },
      },
    },
    {
      url: `${BASE_URL}/tr`,
      lastModified: BUILD_DATE,
      alternates: {
        languages: {
          tr: `${BASE_URL}/tr`,
          en: `${BASE_URL}/en`,
        },
      },
    },
  ];
}
