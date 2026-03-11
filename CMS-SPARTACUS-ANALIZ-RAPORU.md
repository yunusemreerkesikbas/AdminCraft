# SAP Spartacus CMS Yapi Analiz Raporu

## 1) Kapsam
Bu rapor, projedeki `pageTemplate -> contentSlot -> cmsComponent` akisinin nasil kurgulandigini aciklar.
Odak dosyalar:
- `src/config/spa-cmscomponents-config.ts`
- `src/config/spa-page-templates.ts`
- `src/config/pages/*`
- `src/config/spa-layouts.ts`
- `src/app/app.component.html`
- `src/app/spartacus/spartacus-configuration.module.ts`

---

## 2) Ust Seviye Mimari Akis

### 2.1 Config injection
Spartacus config merkezi olarak su dosyada veriliyor:

```ts
// src/app/spartacus/spartacus-configuration.module.ts
provideConfig({
  cmsComponents: SPA_CMSCOMPONENTS_CONFIG,
  layoutSlots: SPA_LAYOUTS,
  routing: SPA_ROUTING_CONFIG,
  backend: SPA_BACKEND_CONFIG,
})
```

### 2.2 Sayfa iskeleti (template -> slots)
Projede sayfa yerlesimi custom outlet yapisiyla render ediliyor:

```html
<!-- src/app/app.component.html -->
@for (page of pages | keyvalue; track $any(page).uid) {
  <ng-template [cxOutletRef]="page.key">
    @for (template of page.value; track $index) {
      @for (slot of template.slots; track slot.position) {
        <cx-page-slot [position]="slot.position"></cx-page-slot>
      }
    }
  </ng-template>
}
```

`pages` kaynagi:

```ts
// src/app/app.component.ts
public pages? = pageTemplates;
```

Yani bu projede custom page outlet tarafinda ana kaynak `spa-page-templates.ts` ve `src/config/pages/*`.

### 2.3 Component esleme (typeCode/flexType -> Angular component)

```ts
// src/config/spa-cmscomponents-config.ts
export const SPA_CMSCOMPONENTS_CONFIG = {
  HomeBannerCarouselComponent: {
    component: () => import('../modules/spartacus/home-banner-carousel/home-banner-carousel.component')
      .then((m) => m.HomeBannerCarouselComponent),
  },
  CategoryTabComponent: {
    component: () => import('../modules/spartacus/category-tab/category-tab.component')
      .then((m) => m.CategoryTabComponent),
  },
  CampaignsStripeComponent: {
    component: () => import('../modules/custom/campaigns-stripe/campaigns-stripe.component')
      .then((m) => m.CampaignsStripeComponent),
  },
  SpaCopyrightComponent: {
    component: () => import('../modules/custom/copyright/copyright.component')
      .then((m) => m.CopyrightComponent),
  },
};
```

Not: Dosyada toplam yaklasik `70` component mapping var. Bir kismina `disableSSR` ve `guards` verilmis.

---

## 3) OCC Response -> Frontend Render Eslesmesi

## 3.1 `cms/pages` response yorumu (senin paylastigin homepage)

Ozet alanlar:
- `template: "LandingPage1Template"`
- `contentSlots.contentSlot[].position` (orn: `TopHeaderSlot`, `CenterContent`, `BackgroundContent`)
- `components.component[].typeCode` (orn: `CampaignsStripeComponent`, `HomeBannerCarouselComponent`, `CategoryTabComponent`)

Template karsiligi:

```ts
// src/config/spa-page-templates.ts
export const pageTemplates = {
  LandingPage1Template: SPA_LANDING_PAGE_TEMPLATE,
  ProductListPageTemplate: SPA_PRODUCT_LISTING_PAGE_TEMPLATE,
  ProductDetailsPageTemplate: SPA_PRODUCT_DETAILS_PAGE_TEMPLATE,
  // ...
};
```

Landing slot karsiligi:

```ts
// src/config/pages/spa-landing-page-template.ts
slots: [
  { position: 'CenterContent' },
  { position: 'BackgroundContent' },
  { position: 'BottomContent' },
  { position: 'LeftContent' },
  { position: 'RightContent' },
  { position: 'LandingPage1WidgetsSlot' },
]
```

## 3.2 `cms/components` response yorumu
Senin ornekteki `CMSLinkComponent` verisi (SupportHeaderBandLink vb.) navigation node/entry tarafinda kullaniliyor.
Navigation tree populate akisinin ana noktasi:

```ts
// src/core/services/navigation/spa-navigation.service.ts
this.cmsService.getNavigationEntryItems(navigation.uid ?? '')
// entry.itemType === 'CMSLinkComponent' oldugunda node.url/node.title set edilir
```

---

## 4) Bu Projede PageTemplate/Slot/Component Nasil Olusturulur?

## 4.1 Yeni CMS component ekleme adimlari
1. Angular component yaz (`standalone` pattern kullaniliyor).
2. Gerekirse Spartacus model alanlarini extend et (`declare module '@spartacus/core'`).
3. `src/config/spa-cmscomponents-config.ts` icine mapping ekle.
4. Gerekliyse `disableSSR` ve `guards` ekle.
5. CMS tarafinda component tipini olustur ve slota ata.
6. OCC ile dogrula:
   - `cms/pages` icinde ilgili slot/component gorunuyor mu?
   - `cms/components?componentIds=...` alanlari dogru mu?

## 4.2 Yeni sayfa/template ekleme adimlari
1. CMS page `template` degeri belirle (orn: `LandingPage1Template` gibi).
2. Frontendde `src/config/pages/` altina template slot grubunu tanimla.
3. `src/config/spa-page-templates.ts` icine `templateAdi -> slot grubu` map ekle.
4. CMS slot position adlari ile frontend `position` adlari birebir ayni olmali.
5. Gerekirse metadata/canonical alanlarini resolver ve page model tarafinda dogrula.

---

## 5) `spa-layouts.ts` ile `pageTemplates/pages/*` arasindaki durum

Senin sorunun cevabi: Bu noktada runtime crash degil, **cift tanimdan dogan konfigurasyon ayrismasi** var.

- `spa-layouts.ts` Spartacus `LayoutConfig` icin slot listesi tutuyor.
- `app.component.html` ise custom outlet yapisinda `pageTemplates/pages/*` slot listesini render ediyor.
- Ayni template isimleri icin bu iki dosyada slot adlari birebir degil.

### Kisa netlestirme: \"cakisma\" tam olarak ne?
- Bu bir \"iki config ayni anda farkli deger tasiyor\" durumu.
- Teknik olarak ikisi de derlenir, uygulama acilir; yani bu tek basina runtime hata degildir.
- Sorun daha cok su: yeni bir ekip uyesi veya yeni gelistirme yaparken hangi dosyayi degistirecegine karar vermekte zorlanir.
- Sonuc: davranis sapmasi riski artar (bir yerde slot eklenir, digerinde unutulur).

### Somut ornekler

| Template | `spa-layouts.ts` | `src/config/pages/*` |
|---|---|---|
| `LandingPage1Template` | `SectionFullWidthSLot`, `SectionBoxedWidthSLot`, `LandingPage1WidgetsSlot` | `CenterContent`, `BackgroundContent`, `BottomContent`, `LeftContent`, `RightContent`, `LandingPage1WidgetsSlot` |
| `CartPageTemplate` | `TopCartSlot`, `TopContent`, `CenterRightContentSlot`, `EmptyCartMiddleContent`, ... | `CartCampaignsSlot`, `CartCenterSlot`, `CartBankCampaignsSlot`, `CartBottomSlot` |
| `AccountPageTemplate` | `TopContentSlot`, `LeftContentSlot`, `RightContentSlot` | `AccountTopSlot`, `AccountCenterSlot`, `AccountLeftSlot`, `AccountRightSlot`, `AccountBottomSlot` |
| `StaticLandingMultipleColumnPageTemplate` | `TopContentSlot`, `LeftContentSlot`, `RightContentSlot` | `StaticTopSlot`, `StaticCenterSlot`, `StaticLeftSlot`, `StaticRightSlot`, `StaticBottomSlot` |

### Pratikte ne anlama geliyor?
- Bu projede custom outlet render akisi oldugu icin, gorunen page-slot yapisini daha cok `pageTemplates/pages/*` belirliyor.
- `spa-layouts.ts` yine sistemde mevcut ama ayni template icin farkli slot setleri tuttugu icin onboardingde kafa karistiriyor.
- Bu nedenle ekipte net bir "source of truth" belirlemek teknik borcu azaltir.

---

## 6) Ek Teknik Notlar
- `SPA_CONTENT_PAGE_TEMPLATE` var ama `pageTemplates` map'inde kullanilmiyor.
- Header ve footer slotlari da custom componentler icinde ayri position isimleriyle render ediliyor:
  - `src/modules/flex/header/header.component.html`
  - `src/modules/spartacus/navigation/footer/footer.component.html`
- `cms/pages` cagrilarinda cihaz tipine gore `Platform` header ekleniyor (`Mobile/Desktop`).

---

## 7) Kisa Ornek Response Parcalari

### 7.1 Page response (ornek)

```json
{
  "uid": "homepage",
  "template": "LandingPage1Template",
  "contentSlots": {
    "contentSlot": [
      {
        "position": "TopHeaderSlot",
        "components": {
          "component": [
            { "uid": "HeaderCampaignsStripeComponent", "typeCode": "CampaignsStripeComponent" },
            { "uid": "HeaderSliderBannerComponent", "typeCode": "HomeBannerCarouselComponent" }
          ]
        }
      }
    ]
  }
}
```

### 7.2 Components response (ornek)

```json
{
  "component": [
    {
      "uid": "SupportHeaderBandLink",
      "typeCode": "CMSLinkComponent",
      "linkName": "Destek",
      "url": "/sikca-sorulan-sorular"
    }
  ]
}
```

---

## 8) Acik Konular (Karar Gerektiren)
1. Ayni template icin tek kaynak hangisi olsun?
   - Opsiyon A: `pageTemplates/pages/*`
   - Opsiyon B: `spa-layouts.ts`
   - Opsiyon C: ikisini birebir esitleme
2. `SPA_CONTENT_PAGE_TEMPLATE` aktif kullanima alinacak mi?
3. `flexType` tabanli componentlerde adlandirma standardi dokumante edilsin mi?

Bu 3 soruyu netlersen, bir sonraki adimda sana "tek sayfalik operasyon checklist" de cikaririm.
