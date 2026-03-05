# AdminCraft Storefront (Next.js)

Headless storefront for AdminCraft CMS delivery APIs.

## Requirements

- Node.js 20+

## Environment

Create `.env.local` with:

```
NEXT_PUBLIC_CMS_API_URL=http://localhost:8080/api
TENANT_SUBDOMAIN=demo
NEXT_PUBLIC_TENANT_SUBDOMAIN=demo
TENANT_ID=1
NEXT_PUBLIC_TENANT_ID=1
```

## Locale Routing

All routes are locale-prefixed. Missing locales are redirected to `/tr`.

Example routes:

- `/tr` (homepage)
- `/tr/about` (content page)
- `/tr/products/ABC123`
- `/tr/c/electronics`
- `/tr/search?q=laptop`

## Development

```bash
npm install
npm run dev
```
