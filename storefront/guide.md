## Components API Frontend Guide

### Base URL

- `/api/components/{type}`
- Required header: `X-Tenant-ID`
- Optional: `Authorization: Bearer <token>`, `Accept-Language`

### Types

- `navbar`, `logo`, `cta`, `brands`, `faq`, `breadcrumb`

### Endpoints

1) List by type

- GET `/api/components/{type}?status=ACTIVE|INACTIVE`

2) Get by id

- GET `/api/components/{type}/{id}`

3) Create

- POST `/api/components/{type}`
- URL type must equal body `type`

4) Update

- PUT `/api/components/{type}/{id}`

5) Delete

- DELETE `/api/components/{type}/{id}`

6) Site list (localized)

- GET `/api/components/{type}/site?lang=tr|en`

### Request/Response Contracts

- Create/Update request body (excerpt):

```
{
  "tenantId": 1,
  "type": "NAVBAR",
  "key": "primary-navbar",
  "status": "ACTIVE",
  "visible": true,
  "sortOrder": 0,
  "translations": {
    "tr": { "title": "Birincil", "subtitle": "Üst menü", "data": "{...}" },
    "en": { "title": "Primary", "subtitle": "Top menu", "data": "{...}" }
  }
}
```

- Admin read response (`ComponentResponse`, Stage 2):

```
{
  "id": 10,
  "tenantId": 1,
  "type": "NAVBAR",
  "key": "primary-navbar",
  "status": "ACTIVE",
  "visible": true,
  "sortOrder": 0,
  "translations": {
    "tr": { "title": "Birincil", "subtitle": "Üst menü", "data": "{...}" },
    "en": { "title": "Primary", "subtitle": "Top menu", "data": "{...}" },
    "es": { "title": "Principal" }
  }
}
```

- Site read response (`SiteComponentResponse`):

```
{
  "id": 10,
  "type": "NAVBAR",
  "key": "primary-navbar",
  "sortOrder": 0,
  "translation": { "title": "Birincil", "subtitle": "Üst menü", "data": "{...}" }
}
```

### Headers

- `X-Tenant-ID`: number (required)
- `Authorization`: Bearer token (if protected)
- `Accept-Language`: `tr` or `en` (optional)

### Error handling

- 400: URL–body type mismatch, invalid params
- 403: Tenant mismatch
- 404: Not found
- 409: `(tenantId,type,key)` unique conflict

### Sample fetch (Angular/TS)

```
getComponents(type: string, tenantId: number, status?: 'ACTIVE'|'INACTIVE') {
  const params = status ? `?status=${status}` : '';
  return this.http.get<ApiResponse<ComponentResponse[]>>(
    `${env.api}/components/${type}${params}`,
    { headers: { 'X-Tenant-ID': String(tenantId) } }
  );
}
```

### Notes

- URL `type` MUST match body `type` on POST/PUT.
- `key` must match `^[a-z0-9._-]+$`.
- `site` endpoint returns only active and visible items in requested language.

---

## Sprint 11 – Tenant Supported Languages (Admin UI)

### Yeni Dil API’leri

- `GET /languages` → platform kataloğu
- `GET /languages/tenant` → `{ defaultLanguage, supported: string[] }`
- `PATCH /languages/tenant` → tenant dil ayarlarını günceller

Headerlar: `X-Tenant-ID` (zorunlu), `Authorization` (gerekirse)

### Admin UI Kullanımı

1) Tenant ekranında diller

- Açılışta `GET /languages/tenant` ile değerleri yükleyin.
- Kullanıcı seçimlerini `PATCH /languages/tenant` ile kaydedin.
- Kaydın ardından uygulama durumundaki (store/service) dil bilgisini yenileyin.

2) Component formları (dinamik sekmeler)

- Tenant `supportedLanguages` listesine göre sekmeleri üretin.
- Form submit sırasında `translations` map’ini seçili dillere göre gönderin.

3) Admin okuma (`ComponentResponse` Stage 2)

- `translations: Record<string, ComponentTranslation>` döner.
- UI, sekme/dil alanlarını bu map’ten doldurur (ör. tr,en,es,ar,ru).

### Geçiş Notları (Stage 1 → Stage 2)

- Eski `tr`/`en` alanları kaldırıldı. Tüm diller `translations` map’inde.
- UI tarafta TR/EN’e özel kodları kaldırın; sekmeleri `supportedLanguages` ve
  `ComponentResponse.translations` üzerinden dinamik üretin.
