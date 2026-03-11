# Landing Page SEO — Implementation Guide

> Uygulanan: 2026-03-08 | App: `landing/` (Next.js App Router)

---

## Genel Bakış

`landing/` uygulaması `https://www.craftive.io` adresinde yayınlanacak Next.js 16 App Router projesidir. TR/EN lokalizasyonu desteklenir. Bu belgede implement edilen teknik SEO altyapısı açıklanmaktadır.

---

## Dosya Haritası

| Dosya | Rol |
|-------|-----|
| `app/layout.tsx` | Root layout — `lang="en"` fallback, font, global CSS |
| `app/[locale]/layout.tsx` | Locale layout — `generateMetadata`, JSON-LD script blokları |
| `app/robots.ts` | `/robots.txt` endpoint |
| `app/sitemap.ts` | `/sitemap.xml` endpoint |
| `app/opengraph-image.tsx` | Root OG image (edge runtime, 1200×630) |
| `app/[locale]/opengraph-image.tsx` | Locale-aware OG image |
| `components/SetLang.tsx` | Client-side `<html lang>` güncellemesi (useEffect) |

---

## Phase 1 — Teknik Temel

### `app/layout.tsx`
- `lang="tr"` → `lang="en"` (İngilizce fallback)
- Türkçe description kaldırıldı, İngilizce genel açıklama eklendi
- `SetLang` bileşeni locale'e göre `document.documentElement.lang` günceller (client-side)

> **Not:** Next.js App Router'da root layout html/body'yi tanımlamak zorundadır; nested layout bunu override edemez. Bu nedenle doğru `lang` değeri hem `SetLang` (client) hem de `generateMetadata`'daki `openGraph.locale` (server) üzerinden iletilir.

### `app/[locale]/layout.tsx` — `generateMetadata`

Her locale için server-side üretilen metadata:

```ts
{
  title:        locale-specific,
  description:  locale-specific,
  alternates: {
    canonical:  "https://www.craftive.io/{locale}",
    languages: {
      tr:        "https://www.craftive.io/tr",
      en:        "https://www.craftive.io/en",
      "x-default": "https://www.craftive.io/en",
    },
  },
  openGraph: { locale: "tr_TR" | "en_US", type: "website", images: [...] },
  twitter:   { card: "summary_large_image", ... },
}
```

### `app/robots.ts`

```
User-agent: *
Allow: /
Sitemap: https://www.craftive.io/sitemap.xml
```

### `app/sitemap.ts`

`/en` ve `/tr` URL'lerini `alternates.languages` ile birlikte döner.

---

## Phase 2 — JSON-LD Structured Data

`app/[locale]/layout.tsx` içinde üç ayrı `<script type="application/ld+json">` bloğu server-side render edilir:

| Schema | Tip | İçerik |
|--------|-----|--------|
| Organization | `@type: Organization` | name, url, logo, contactPoint (email) |
| SoftwareApplication | `@type: SoftwareApplication` | name, category, OS, fiyat (1500 TRY) |
| FAQPage | `@type: FAQPage` | `content/home.{locale}.json` → `faq.items` dinamik olarak üretilir |

---

## Phase 3 — On-Page SEO

### Section ID Düzeltmesi
- `HowItWorks.tsx`: `id="comparison"` → `id="howitworks"` *(hatalı id'ydi)*
- `Navbar.tsx`: `#comparison` → `#howitworks`
- `home.en.json` + `home.tr.json`: footer product linklerinde `#comparison` → `#howitworks`

### Footer Temizliği
- "Why AdminCraft?" / "Neden AdminCraft?" kaldırıldı (Comparison section sayfaya eklenmedi)
- Tüm `href="#"` placeholder company linkleri (About, Contact, Privacy) kaldırıldı
- `Footer.tsx`: boş `items` olan grupları atlamak için `.filter(g => g.items.length > 0)` eklendi

### Erişilebilirlik / SEO İyileştirmeleri
| Bileşen | Değişiklik |
|---------|-----------|
| `Hero.tsx` — `DashboardMockup` | `role="img"` + `aria-label={content.hero.mockupAlt}` eklendi |
| `TechStack.tsx` — ikon span | `role="img"` + `aria-label={tech.name}` eklendi |
| `FAQ.tsx` — `AccordionTrigger` | shadcn/radix zaten `<h3>` render eder — ayrıca wrapper eklenmedi |

---

## Phase 4 — OG Image

`next/og` `ImageResponse` ile edge runtime'da dinamik görsel üretilir.

- **Root:** `app/opengraph-image.tsx` — her zaman İngilizce
- **Locale:** `app/[locale]/opengraph-image.tsx` — locale'e göre TR/EN altyazı

Boyut: 1200×630 px | Arka plan: `#0f172a` (koyu lacivert)

---

## Doğrulama Kontrol Listesi

Site canlıya alındığında aşağıdakiler kontrol edilmeli:

- [ ] `https://www.craftive.io/robots.txt` — kurallar + sitemap URL doğru
- [ ] `https://www.craftive.io/sitemap.xml` — TR ve EN URL'ler mevcut
- [ ] `/en` kaynak kodu → `<link rel="alternate" hreflang>` mevcut
- [ ] `/tr` kaynak kodu → `<meta property="og:locale" content="tr_TR">`
- [ ] JSON-LD blokları `<head>` içinde görünüyor (kaynak kod)
- [ ] [Google Rich Results Test](https://search.google.com/test/rich-results) → FAQPage geçiyor
- [ ] [Facebook Sharing Debugger](https://developers.facebook.com/tools/debug/) → OG image görünüyor
- [ ] Lighthouse SEO skoru ≥ 90
- [ ] `#howitworks` anchor navbar'dan scroll çalışıyor
