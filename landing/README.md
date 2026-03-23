# Craftive Landing

Next.js App Router based marketing landing site, deployed as static output on Cloudflare Pages.

## Local development

```bash
npm install
npm run dev
```

## Build

```bash
npm run build
```

This project is configured with static export (`output: "export"`), so build artifacts are generated under `out/`.

## Cloudflare Pages deployment

1. Cloudflare Dashboard -> Workers & Pages -> Create -> Pages.
2. Connect the repository.
3. Configure build settings:
   - Framework preset: `Next.js`
   - Root directory: `landing`
   - Build command: `npm run pages:build`
   - Build output directory: `out`
4. Add environment variable if needed:
   - `NEXT_PUBLIC_SITE_URL=https://landing.craftive.io`
5. Deploy and verify preview URL.
6. Attach custom domain: `landing.craftive.io`.

## Post-deploy verification checklist

- `/` opens language selection page.
- `/en/` and `/tr/` load correctly.
- `/sitemap.xml` responds.
- `/robots.txt` responds.
- OG image URL resolves: `/images/og-image.svg`.
- TLS is active and Cloudflare SSL mode is `Full (strict)`.
