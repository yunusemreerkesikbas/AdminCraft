## Sprint 10 Components API Guide (Backend → Frontend)

### Base Route

- `/api/components`

### Auth & Headers

- `Authorization: Bearer <token>` (gerekliyse)
- `X-Tenant-ID: <tenantId>` (zorunlu)
- `Accept-Language: tr|en` (opsiyonel)

### Types

- `type`: enum `ComponentType` → `NAVBAR|LOGO|CTA|BRANDS|FAQ|BREADCRUMB`
- `status`: enum `ComponentStatus` → `ACTIVE|INACTIVE`

### DTO’lar

ComponentRequest

```
{
  tenantId: number,
  type: 'NAVBAR'|'LOGO'|'CTA'|'BRANDS'|'FAQ'|'BREADCRUMB',
  key: string (<=100),
  status?: 'ACTIVE'|'INACTIVE',
  visible?: boolean (default true),
  sortOrder?: number (default 0),
  translations: {
    [lang: 'tr'|'en'|string]: {
      title?: string (<=200),
      subtitle?: string (<=300),
      data?: string (JSON string)
    }
  }
}
```

ComponentResponse

```
{
  id: number,
  tenantId: number,
  type: 'NAVBAR'|'LOGO'|'CTA'|'BRANDS'|'FAQ'|'BREADCRUMB',
  key: string,
  status: 'ACTIVE'|'INACTIVE',
  visible: boolean,
  sortOrder: number,
  tr?: { title?: string, subtitle?: string, data?: string },
  en?: { title?: string, subtitle?: string, data?: string }
}
```

### Endpoints

List

```
GET /api/components
Headers: X-Tenant-ID
Response: ApiResponse<ComponentResponse[]>
```

Get by ID

```
GET /api/components/{id}
Headers: X-Tenant-ID
Response: ApiResponse<ComponentResponse>
```

Create

```
POST /api/components
Headers: X-Tenant-ID,  Content-Type: application/json
Body: ComponentRequest
Response: ApiResponse<ComponentResponse>
```

Update

```
PUT /api/components/{id}
Headers: X-Tenant-ID,  Content-Type: application/json
Body: ComponentRequest
Response: ApiResponse<ComponentResponse>
```

Delete

```
DELETE /api/components/{id}
Headers: X-Tenant-ID
Response: ApiResponse<void>
```

### Validasyon Kuralları (Özet)

- `tenantId` zorunlu (header/body eşleşir); eşleşmezse 403
- `(tenantId, type, key)` benzersiz; ihlalde 409
- `key` ≤ 100, `titleTr` ≤ 200, `subtitleTr` ≤ 300 (EN alanları aynı)
- `dataTr`/`dataEn` JSON string olmalı (frontend’de stringify edilmesi önerilir)
- `sortOrder` varsayılan 0, `visible` varsayılan true

### Örnek Kullanımlar

Navbar oluşturma (TR/EN)

```
POST /api/components
Headers: X-Tenant-ID: 1, 
{
  "tenantId": 1,
  "type": "NAVBAR",
  "key": "primary-navbar",
  "visible": true,
  "sortOrder": 0,
  "titleTr": "Birincil Navigasyon",
  "dataTr": "{\"items\":[{\"label\":\"Ana Sayfa\",\"url\":\"/\"}]}",
  "titleEn": "Primary Navigation",
  "dataEn": "{\"items\":[{\"label\":\"Home\",\"url\":\"/en\"}]}"
}
```

Navbar güncelleme

```
PUT /api/components/1
Headers: X-Tenant-ID: 1, 
{
  "sortOrder": 1,
  "titleTr": "Birincil Navigasyon - Güncellendi"
}
```

### Frontend Notları

- İsteklerde `X-Tenant-ID` header’ını zorunlu gönderin.
- `dataTr`/`dataEn` alanları JSON string; TS tarafında tip güvenliği için interface oluşturun ve `JSON.stringify`/`JSON.parse` kullanın.
- Cevap `ApiResponse` sarmalında gelir: `result`, `message`, `data`.
