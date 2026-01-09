# CMS Delivery (Public APIs)

## Purpose

CMS Delivery provides **storefront-friendly** endpoints that return render-ready data without requiring authentication.
Requests are still **tenant-scoped** via tenant resolution (subdomain/header/hostname).

## Controllers

- CMS content delivery: [`backend/src/main/java/com/backend/presentation/controller/CmsDeliveryController.java`](../../backend/src/main/java/com/backend/presentation/controller/CmsDeliveryController.java)
- CMS media delivery: [`backend/src/main/java/com/backend/presentation/controller/CmsMediaDeliveryController.java`](../../backend/src/main/java/com/backend/presentation/controller/CmsMediaDeliveryController.java)

## Endpoints

Base path: `/api/cms`

### Components

- `GET /api/cms/components/{uid}?lang=TR`
- `GET /api/cms/components?uids=uid1&uids=uid2&lang=TR` (max 50)
  - Query format is **repeated params** because the controller uses `@RequestParam List<String> uids`.
  - Example: `/api/cms/components?uids=header&uids=footer&lang=EN`

### Pages

- `GET /api/cms/pages/{uid}?lang=TR`
- `GET /api/cms/pages?uids=uid1&uids=uid2&lang=TR` (max 50)

### Navigation

- `GET /api/cms/navigation/{uid}`

### Media

Base path: `/api/cms/media`

- `GET /api/cms/media/{uid}` (optional `?format=thumbnail`)
- `GET /api/cms/media?uids=uid1&uids=uid2` (max 50)

## Language resolution

From `CmsDeliveryController`:

- `lang` query parameter wins when provided (uses `Language` enum values like `TR`, `EN`)
- otherwise `Accept-Language` is mapped to `Language` (ISO codes like `tr`, `en`)
- otherwise a default language is used

Note:

- CMS media delivery (`CmsMediaDeliveryController`) does not accept `lang`; it uses `Accept-Language` only.

## Rate limiting

From `CmsDeliveryController` and `CmsMediaDeliveryController`:

- **100 req/min per tenant**, enforced using `TenantContext.tenantId`

## Response contract (high level)

- Delivery endpoints return `ApiResponse<T>` where `T` is a delivery DTO.
- Batch endpoints return a wrapper with:
  - `data`: map of `{ uid -> deliveryDto }`
  - `meta`: `{ requested, found, notFound[] }`
- Max batch size is enforced server-side (**50**).

DTO references (source of truth):

- `backend/src/main/java/com/backend/application/dto/delivery/ComponentDeliveryResponse.java`
- `backend/src/main/java/com/backend/application/dto/delivery/BatchDeliveryResponse.java`
- `backend/src/main/java/com/backend/application/dto/delivery/PageDeliveryResponse.java`
- `backend/src/main/java/com/backend/application/dto/delivery/BatchPageDeliveryResponse.java`

## Frontend integration

Frontend client code exists for CMS component delivery (and can be extended for other delivery endpoints):

- `storefront/src/app/cms/` (delivery service + types)

## Security & tenant isolation

- Delivery endpoints do not require auth, but they are still tenant-scoped.
- Tenant resolution is enforced by [`backend/src/main/java/com/backend/infrastructure/tenant/TenantFilter.java`](../../backend/src/main/java/com/backend/infrastructure/tenant/TenantFilter.java).

