# i18n & Composite Operations

## Entity-level i18n pattern

Craftive models localized data using a split-table approach:

- Base entity: language-agnostic fields
- I18n entity: language-specific fields, unique by `(base_id, language)`

This avoids attribute-level localization tables and keeps SQL schema clean.

## Composite endpoints (atomic writes)

For workflows that must be atomic (base + translations in one transaction), use **composite endpoints**:

- Create/update accepts base fields + a `translations` map keyed by `Language`
- Service method is `@Transactional`
- Validation failures rollback the entire operation

### Response message contract

For admin composite/action endpoints, success and error responses should return a localized `ApiResponse.message` resolved by backend `MessageSource` using `Accept-Language`.

- Backend should return resolved user-facing text, not raw i18n keys
- Frontend should show backend `response.message` directly for backend-driven notifications
- Frontend fallback i18n keys should be reserved for client-only situations such as network failures, empty states, or local validation flows

This pattern is used by component-library composite/action flows (`component`, `component type`, `entry`, `entry field`).

### Example: Media composite upload

Backend endpoint is implemented at:

- `POST /api/media/composite` in [`backend/src/main/java/com/backend/presentation/controller/MediaController.java`](../../backend/src/main/java/com/backend/presentation/controller/MediaController.java)

The request is `multipart/form-data` with:

- `file`: uploaded file
- `uploadedBy`: uploader user id
- `translations`: optional JSON string mapping `Language` enum values (`TR`, `EN`, ...) to per-language metadata request

Example `translations` payload:

- `{ "TR": { "alt": "Hero banner" }, "EN": { "alt": "Hero banner" } }`

## Language resolution

For public delivery endpoints (CMS), language is resolved by:

- explicit `lang` query param when the endpoint supports it (e.g. components/pages in `CmsDeliveryController`)
- otherwise `Accept-Language` (ISO codes like `tr`, `en`), then a default language

For authenticated admin endpoints, localized `ApiResponse.message` text is typically resolved from the `Accept-Language` header.

See: [`backend/src/main/java/com/backend/presentation/controller/CmsDeliveryController.java`](../../backend/src/main/java/com/backend/presentation/controller/CmsDeliveryController.java)
