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

- Admin read response (`ComponentResponse`):

```
{
  "id": 10,
  "tenantId": 1,
  "type": "NAVBAR",
  "key": "primary-navbar",
  "status": "ACTIVE",
  "visible": true,
  "sortOrder": 0,
  "tr": { "title": "Birincil", "subtitle": "Üst menü", "data": "{...}" },
  "en": { "title": "Primary", "subtitle": "Top menu", "data": "{...}" }
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
