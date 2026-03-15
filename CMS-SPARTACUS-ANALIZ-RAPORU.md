# Yataş Spartacus CMS Mimari İnceleme Raporu

## Context

Yataş e-ticaret storefront'u SAP Commerce Cloud (Hybris) backend ile Spartacus Angular frontend kullanıyor. Bu rapor CMS Slot, PageTemplate ve CMS Component mimarilerini detaylı olarak analiz eder.

---

## 1. GENEL MİMARİ AKIŞ

```
Hybris Backoffice (WCMS Cockpit)
    ↓ CMS Page + Slot + Component tanımları
OCC API (REST)
    ↓ JSON response
Spartacus CmsService
    ↓ Page data normalize
AppComponent (pageTemplates mapping)
    ↓ cx-page-slot render
Angular Component (lazy loaded)
```

**Akış özeti:**

1. Backoffice'de **PageTemplate** oluşturulur, içine **ContentSlot** eklenir
2. Her ContentSlot'a **CMS Component**'ler atanır
3. Spartacus OCC API üzerinden page data'yı çeker
4. `SPA_LAYOUTS` ile hangi template'de hangi slot'ların render edileceği belirlenir
5. `SPA_CMSCOMPONENTS_CONFIG` ile CMS component type → Angular component eşleştirilir
6. `app.component.html`'deki `cx-page-slot` direktifi ile slot'lar DOM'a render edilir

---

## 2. PAGE TEMPLATE MİMARİSİ

### 2.1 İki Katmanlı Konfigürasyon

**Katman 1 — LayoutConfig** (`src/config/spa-layouts.ts`):
Spartacus'un `LayoutConfig` interface'i ile hangi template'de hangi slot'ların bulunduğu tanımlanır.

| Template                                    | Slot'lar                                                                                                                                    |
| ------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------- |
| **LandingPage1Template**                    | SectionFullWidthSLot, SectionBoxedWidthSLot, LandingPage1WidgetsSlot                                                                        |
| **ProductListPageTemplate**                 | BreadcrumbSlot, ProductListTopSlot, ProductListSlot, ProductListBottomSlot                                                                  |
| **ProductDetailsPageTemplate**              | BreadcrumbSlot, ProductDetailsTopSlot, ProductDetailsSlot, ProductDetailsIconBandSlot, ProductDetailsFeaturesSlot, ProductDetailsBottomSlot |
| **LoginPageTemplate**                       | CenterContent, LeftContent, RightContent                                                                                                    |
| **CartPageTemplate**                        | TopCartSlot, TopContent, CenterRightContentSlot, EmptyCartMiddleContent, BottomCartSlot, CartPageWidgetsSlot                                |
| **MultiStepCheckoutPageTemplate**           | TopCheckoutSlot, LeftCheckoutSlot, RightCheckoutSlot, CenterContentSlot                                                                     |
| **AccountPageTemplate**                     | TopContentSlot, LeftContentSlot, RightContentSlot                                                                                           |
| **ContentPageTemplate**                     | BreadcrumbSlot, TopContentSlot, LeftContentSlot, RightContentSlot                                                                           |
| **EmptyPageTemplate**                       | BreadcrumbSlot, CenterContentSlot                                                                                                           |
| **StaticLandingMultipleColumnPageTemplate** | BreadcrumbSlot, TopContentSlot, LeftContentSlot, RightContentSlot                                                                           |
| **smartMatchTemplate**                      | CenterContentSlot                                                                                                                           |

**Katman 2 — SpaSlotGroup konfigürasyonu** (`src/config/pages/*.ts`):
Her template için detaylı layout bilgisi (CSS sınıfları, pageFold, kolon yapısı) tanımlanır.

**Model:** `src/core/models/slot/spa-slot-group.model.ts`

```typescript
interface SpaSlotGroup {
  containerClass?: string; // Container CSS sınıfı
  slots?: Array<SpaSlot>; // Slot dizisi
  template?: string; // Template CSS sınıfı
}
interface SpaSlot {
  columnClass?: string; // Bootstrap grid sınıfı (col-12, col-lg-3 vb.)
  position?: string; // Slot adı (Backoffice'deki position ile eşleşir)
  template?: string; // Ek template sınıfı
  isPageFold?: boolean; // Above-the-fold mı? (SSR/lazy loading optimizasyonu)
}
```

### 2.2 Template Kayıt Merkezi

`src/config/spa-page-templates.ts` — Tüm template'leri bir obje olarak export eder:

```typescript
export const pageTemplates = {
  LandingPage1Template: SPA_LANDING_PAGE_TEMPLATE,
  ProductListPageTemplate: SPA_PRODUCT_LISTING_PAGE_TEMPLATE,
  ProductDetailsPageTemplate: SPA_PRODUCT_DETAILS_PAGE_TEMPLATE,
  LoginPageTemplate: SPA_LOGIN_PAGE_TEMPLATE,
  MultiStepCheckoutPageTemplate: SPA_MULTISTEP_CHECKOUT_PAGE_TEMPLATE,
  AccountPageTemplate: SPA_ACCOUNT_PAGE_TEMPLATE,
  StaticLandingMultipleColumnPageTemplate: SPA_STATIC_LANDING_MULTIPLE_COLUMN_PAGE_TEMPLATE,
  EmptyPageTemplate: SPA_EMPTY_PAGE_TEMPLATE,
  CartPageTemplate: SPA_CART_PAGE_TEMPLATE,
};
```

### 2.3 Render Mekanizması

`src/app/app.component.html` — Ana render template'i:

```html
@for (page of pages | keyvalue; track $any(page).uid) {
<ng-template [cxOutletRef]="page.key">
  @for (template of page.value; track $index) {
  <div [ngClass]="[template.containerClass, template.template]">
    <div class="row">
      @for (slot of template.slots; track slot.position) {
      <div [ngClass]="[slot.columnClass, slot.template]">
        <cx-page-slot [position]="slot.position" [isPageFold]="slot.isPageFold ? slot.isPageFold : false"></cx-page-slot>
      </div>
      }
    </div>
  </div>
  }
</ng-template>
}
```

`app.component.ts` — `pageTemplates` objesini `pages` property'sine atar, `CmsService.getCurrentPage()` ile aktif sayfayı takip eder.

---

## 3. CMS SLOT MİMARİSİ

### 3.1 Slot Türleri

**Global Slot'lar (her sayfada):**

- **Header:** TopHeaderSlot, HeaderLogoSlot, HeaderNavigationSlot, HeaderAccountLinksSlot, MiniCartSlot, BottomHeaderSlot
- **Footer:** FooterTopSlot, FooterNavigationSlot, FooterCopyrightSlot

**Template-Specific Slot'lar:** Yukarıdaki tabloda belirtilen slot'lar sadece ilgili template'de render edilir.

### 3.2 Backoffice ↔ Spartacus Slot Eşleşmesi

Backoffice'de (WCMS Cockpit):

1. **PageTemplate** oluşturulur (ör: `ProductDetailsPageTemplate`)
2. Template'e **ContentSlot** eklenir (ör: position = `ProductDetailsSlot`)
3. ContentSlot'a **CMS Component** atanır (ör: `ProductDetailsComponent`)

Spartacus tarafında:

- `SPA_LAYOUTS.layoutSlots[templateName].slots` array'i Backoffice'deki slot position'larıyla birebir eşleşmelidir
- Eşleşmeyen slot'lar render edilmez

### 3.3 PageFold Optimizasyonu

`isPageFold: true` olan slot'lar above-the-fold olarak işaretlenir:

- SSR'da öncelikli render edilir
- Lazy loading stratejisi bu işarete göre belirlenir
- Kullanıcının ilk gördüğü içerik hızlı yüklenir

---

## 4. CMS COMPONENT MİMARİSİ

### 4.1 Component Mapping

`src/config/spa-cmscomponents-config.ts` — 80+ CMS component type → Angular component eşleşmesi:

```typescript
export const SPA_CMSCOMPONENTS_CONFIG = {
  SimpleResponsiveBannerComponent: {
    component: () => import("../modules/spartacus/spa-banner/spa-banner.component").then((m) => m.SpaBannerComponent),
  },
  CartComponent: {
    component: () => import("../modules/custom/cart-page/cart-page.component").then((m) => m.CartPageComponent),
    guards: [SpaCartNotEmptyGuard],
  },
  CheckoutDeliveryPaymentPageComponent: {
    component: () => import("...").then((m) => m.CheckoutDeliveryPaymentPageComponent),
    disableSSR: true,
    guards: [CheckoutAuthGuard, SpaCartNotEmptyGuard],
  },
  // ...
};
```

### 4.2 Component Kategorileri

| Kategori         | CMS Type'lar                                                                                                                      | Angular Component                                             |
| ---------------- | --------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------- |
| **Banner**       | SimpleResponsiveBannerComponent, SimpleBannerComponent, BannerComponent, AdvancedResponsiveBanner                                 | SpaBannerComponent                                            |
| **Carousel**     | RotatingImagesComponent, HomeBannerCarouselComponent, HeaderSliderBannerComponent                                                 | SpaBannerCarouselComponent                                    |
| **Paragraf**     | CMSParagraphComponent, CMSTabParagraphComponent                                                                                   | SpaParagraphComponent                                         |
| **Navigasyon**   | CategoryNavigationComponent                                                                                                       | SpaCategoryNavigationComponent                                |
| **Ürün Listesi** | CMSProductListComponent                                                                                                           | SpaProductListComponent                                       |
| **Breadcrumb**   | BreadcrumbComponent                                                                                                               | SpaBreadcrumbComponent                                        |
| **Sepet**        | CartComponent, EmptyCartComponent, CartTotalsComponent                                                                            | CartPageComponent, EmptyCartComponent, SpaCartTotalsComponent |
| **Checkout**     | CheckoutDeliveryPaymentPageComponent, CheckoutConfirmationPageComponent                                                           | İlgili checkout component'leri                                |
| **Hesap**        | MyAccountComponent, AddressBookComponent, PersonalInformationComponent                                                            | İlgili hesap component'leri                                   |
| **Özel**         | SmartMatchComponent, ProductComparisonComponent, WishListComponent, PriceAlertComponent, StockAlertComponent, NewsletterComponent | Custom component'ler                                          |

### 4.3 Component Özellikleri

- **Lazy Loading:** Tüm component'ler `() => import(...)` ile lazy load edilir (code splitting)
- **Guard Desteği:** `guards: [AuthGuard]` ile korumalı component'ler
- **SSR Kontrolü:** `disableSSR: true` ile client-side-only component'ler (sepet, checkout, hesap)
- **CmsComponentData Injection:** Her component `CmsComponentData<T>` üzerinden Backoffice'deki data'ya erişir

### 4.4 Component Data Akışı

```typescript
// Tipik bir custom component
export class SpaBannerComponent extends BannerComponent {
  componentData$: Observable<CmsBannerComponent> = this.component.data$.pipe(
    tap((data) => {
      this.setRouterLink(data);
      this.styleClasses = data.styleClasses;
    }),
  );
}
```

Backoffice'de component'e atanan tüm property'ler (headline, content, urlLink, media vb.) `this.component.data$` observable'ı üzerinden gelir.

---

## 5. BACKOFFICE YÖNETİMİ

### 5.1 Backoffice'den Yönetilen Öğeler

| Öğe             | Backoffice Lokasyonu  | Spartacus Karşılığı                        |
| --------------- | --------------------- | ------------------------------------------ |
| Page Template   | WCMS > Page Templates | `spa-layouts.ts` + `spa-page-templates.ts` |
| Content Slot    | WCMS > Content Slots  | `cx-page-slot[position]`                   |
| CMS Component   | WCMS > Components     | `spa-cmscomponents-config.ts`              |
| Content Page    | WCMS > Pages          | CmsService.getCurrentPage()                |
| Media/Banner    | WCMS > Media          | `spa-media.ts` format config               |
| Navigation Node | WCMS > Navigation     | CategoryNavigationComponent                |

### 5.2 Yeni Component Ekleme Süreci

1. **Backoffice:** Yeni CMS Component Type oluştur (ör: `MyNewComponent`)
2. **Backoffice:** Component'i ilgili ContentSlot'a ata
3. **Spartacus:** `spa-cmscomponents-config.ts`'ye mapping ekle:

   ```typescript
   MyNewComponent: {
     component: () => import('../modules/custom/my-new/my-new.component')
       .then((m) => m.MyNewComponent),
   },
   ```

4. **Spartacus:** Angular component'i oluştur, `CmsComponentData` inject et

### 5.3 Yeni Slot Ekleme Süreci

1. **Backoffice:** PageTemplate'e yeni ContentSlot ekle (ör: position = `NewSlot`)
2. **Spartacus:** `spa-layouts.ts`'de ilgili template'in slots array'ine ekle
3. **Spartacus:** İlgili `src/config/pages/spa-*-template.ts` dosyasına slot konfigürasyonu ekle

---

## 6. KONFIGÜRASYON DOSYALARI HARİTASI

```
src/config/
├── spa-layouts.ts                    → LayoutConfig (slot → template mapping)
├── spa-page-templates.ts             → Tüm template'lerin kayıt merkezi
├── spa-cmscomponents-config.ts       → CMS type → Angular component mapping (80+)
├── spa-config-spartacus.ts           → Dil, breakpoint, i18n ayarları
├── spa-backend-config.ts             → OCC backend + auth config
├── spa-endpoints-config.ts           → 160+ custom API endpoint
├── spa-checkout-config.ts            → 3 adımlı checkout akışı
├── spa-routing-config.ts             → URL matcher'lar (category, brand, product)
├── spa-gtm-config.ts                 → Google Tag Manager (10 event type)
├── spa-media.ts                      → Responsive media formatları + CDN
└── pages/
    ├── spa-landing-page-template.ts
    ├── spa-product-listing-page-template.ts
    ├── spa-product-detail-page-template.ts
    ├── spa-login-page-template.ts
    ├── spa-cart-page-template.ts
    ├── spa-account-page-template.ts
    ├── spa-multistep-checkout-page-template.ts
    ├── spa-content-page-template.ts
    ├── spa-empty-page-template.ts
    ├── spa-static-Landing-Multiple-Column-Page-Template.ts
    └── spa-footer-template.ts

src/app/spartacus/
├── spartacus.module.ts                → Ana Spartacus modülü
├── spartacus-configuration.module.ts  → Tüm config'lerin provideConfig ile kaydı
├── spartacus-features.module.ts       → 50+ feature modül importu + feature toggles
└── features/
    ├── asm/
    ├── cart/
    ├── checkout/
    ├── order/
    ├── product/
    ├── pickup-in-store/
    ├── storefinder/
    ├── tracking/
    ├── smartedit/
    └── user/
```

---

## 7. ÖNEMLİ DETAYLAR

- **Dil:** Varsayılan `tr` (Türkçe), fallback da `tr`
- **Feature Level:** Spartacus 6.0
- **Breakpoints:** xs:0, sm:576, md:768, lg:1140
- **Image Loading:** LAZY strategy
- **Infinite Scroll:** Aktif, productLimit: 0, showMoreButton: true
- **Default Page Size:** 24 ürün
- **CDN:** Medianova (environment-based)
- **Guest Checkout:** Aktif
- **SSR:** Aktif (disableSSR ile kontrollü)

---

## 8. DİKKAT EDİLMESİ GEREKENLER

1. `spa-layouts.ts` ile `pages/*.ts` arasında slot isimleri tutarlı olmalı (ör: CartPageTemplate'de `spa-layouts.ts` farklı slotlar, `spa-cart-page-template.ts` farklı slotlar listeler — bu custom render mekanizması nedeniyle)
2. `spa-cmscomponents-config.ts`'deki type ismi Backoffice'deki component typeCode ile birebir eşleşmeli
3. Yeni template eklerken hem `spa-layouts.ts` hem `spa-page-templates.ts` hem de `pages/` altında dosya oluşturulmalı
4. `isPageFold` doğru ayarlanmalı — yanlış ayar SSR performansını etkiler
