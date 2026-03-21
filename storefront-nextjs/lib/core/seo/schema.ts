import type { SiteDeliveryResponse } from "../../types";
import { buildMediaUrl } from "../media/url";

export const buildOrganizationSchema = (site: SiteDeliveryResponse) => {
  const sameAs = Object.values(site.social ?? {}).filter(Boolean) as string[];

  return {
    "@context": "https://schema.org",
    "@type": "Organization",
    name: site.i18n?.siteName ?? site.siteName,
    description: site.i18n?.tagline ?? site.seo?.description,
    url: site.canonicalBaseUrl,
    logo: site.logoDarkUrl ? buildMediaUrl(site.logoDarkUrl) : undefined,
    email: site.contact?.email,
    telephone: site.contact?.phone,
    address: site.address?.city
      ? {
          "@type": "PostalAddress",
          streetAddress: site.address.line1,
          addressLocality: site.address.city,
          addressRegion: site.address.state,
          postalCode: site.address.postalCode,
          addressCountry: site.address.country,
        }
      : undefined,
    sameAs: sameAs.length > 0 ? sameAs : undefined,
  };
};

export const buildWebSiteSchema = (site: SiteDeliveryResponse, lang: string) => {
  const base = site.canonicalBaseUrl;
  if (!base) {
    return null;
  }

  return {
    "@context": "https://schema.org",
    "@type": "WebSite",
    url: `${base}/`,
    potentialAction: {
      "@type": "SearchAction",
      target: {
        "@type": "EntryPoint",
        urlTemplate: `${base}/${lang}/search?q={search_term_string}`,
      },
      "query-input": "required name=search_term_string",
    },
  };
};
