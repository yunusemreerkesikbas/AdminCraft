# Craftive Public Docs Site

Public documentation site for `docs.craftive.io`.

This site contains product, technical flow, and editor usage documentation for Craftive.

## Commands

```powershell
npm install
npm run start:tr
npm run start:en
npm run dev
npm run build
```

`npm run dev` builds and serves all locales, so the language switcher works locally. For hot-reload work on one locale, use `npm run start:tr` or `npm run start:en`.

## Internal deployment notes

These settings are repository-facing operational notes for the team that deploys
`docs.craftive.io`; they are not published as public documentation pages.

### Cloudflare Pages

- Project name: `craftive-docs`
- Root directory: `docs-site`
- Build command: `npm run build`
- Output directory: `build`
- Custom domain: `docs.craftive.io`

### SEO and analytics environment

Use Cloudflare Pages environment variables for production values. Do not commit real tracking or verification tokens.

| Variable | Required | Purpose |
| --- | --- | --- |
| `DOCS_SITE_URL` | No | Public canonical URL. Defaults to `https://docs.craftive.io`; set this explicitly in production. |
| `DOCS_GA4_MEASUREMENT_ID` | No | GA4 Measurement ID. When set, Docusaurus injects gtag and records page views. |
| `DOCS_GOOGLE_SITE_VERIFICATION` | No | Search Console HTML tag token value for `docs.craftive.io`. |

The docs site also ships `robots.txt`, sitemap generation, canonical/alternate links, and Organization/WebSite structured data.
