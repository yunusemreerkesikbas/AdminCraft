import type { Metadata } from "next";
import { PageDeliveryResponse, SiteDeliveryResponse } from "./types";
import { buildMediaUrl } from "./utils";

const parseRobotsTag = (tag?: string | null): Metadata["robots"] => {
  if (!tag) {
    return { index: true, follow: true };
  }
  const lower = tag.toLowerCase();
  return {
    index: !lower.includes("noindex"),
    follow: !lower.includes("nofollow"),
  };
};

export const buildPageMetadata = (
  page: PageDeliveryResponse,
  site: SiteDeliveryResponse
): Metadata => {
  const siteTitle = site.siteTitle || site.siteName;
  const pageTitle = page.title || page.name || siteTitle;
  const title =
    pageTitle && siteTitle && pageTitle !== siteTitle
      ? `${pageTitle} | ${siteTitle}`
      : pageTitle;
  const description = page.description || site.siteDescription;
  const canonical = page.canonicalUrl || undefined;
  const ogImage = site.ogImageUrl ? buildMediaUrl(site.ogImageUrl) : undefined;

  return {
    title,
    description,
    robots: parseRobotsTag(page.robotTag),
    alternates: canonical ? { canonical } : undefined,
    openGraph: {
      title,
      description,
      images: ogImage ? [ogImage] : undefined,
    },
  };
};
