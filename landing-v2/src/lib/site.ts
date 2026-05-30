// Centralized site-level constants for SEO (metadata, sitemap, robots, JSON-LD).
// Canonical production host is craftive.io; override per environment with
// NEXT_PUBLIC_SITE_URL (must be a fully-qualified origin, no trailing slash).

export const SITE_URL = (process.env.NEXT_PUBLIC_SITE_URL ?? "https://craftive.io").replace(
  /\/+$/,
  "",
);

export const SITE_NAME = "Craftive";

export const CONTACT_EMAIL = "hello@craftive.io";

/** Brand logo used as the Organization logo in structured data. */
export const LOGO_PATH = "/brand/logo-dark.svg";

/** Public social profiles — surfaced via Organization `sameAs`. */
export const SOCIAL_PROFILES = [
  "https://www.linkedin.com/company/craftive-io",
  "https://x.com/craftive_io",
  "https://www.instagram.com/craftive.io",
  "https://www.youtube.com/@craftive-io",
] as const;
