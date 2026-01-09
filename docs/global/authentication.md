# Authentication

AdminCraft supports two login modes via the same endpoint:

- **Tenant user login** (tenant-scoped)
- **Platform admin login (SUPER_ADMIN)** (no tenant)

## API endpoints

Because the backend context path is `/api`, controller mappings under `@RequestMapping("/auth")` are reachable as:

- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `POST /api/auth/logout`

Controllers:

- [`backend/src/main/java/com/backend/presentation/controller/AuthController.java`](../../backend/src/main/java/com/backend/presentation/controller/AuthController.java)

## Tenant user login

Tenant can be supplied in two ways:

1) Header-based:

- `X-Tenant-ID: {id}` (takes precedence)
- `X-Tenant-Subdomain: {subdomain}`

2) Body-based fallback:

- `LoginRequest.subdomain` (used when `X-Tenant-Subdomain` is not provided)

The actual decision logic is implemented in:

- [`backend/src/main/java/com/backend/application/service/impl/AuthenticationServiceImpl.java`](../../backend/src/main/java/com/backend/application/service/impl/AuthenticationServiceImpl.java)

### Special case: `admin` subdomain

If `subdomain == "admin"`, the system routes the login attempt to **platform admin authentication**.

## Platform admin login (SUPER_ADMIN)

If no tenant identifier is provided (`tenantId == null` and empty subdomain), the login attempt is treated as a platform admin login.

Platform admins are stored in the platform database (`platform_management`) and receive JWTs with:

- `role = SUPER_ADMIN`
- `tenantId = null`

## Refresh token

Token refresh uses the Authorization header:

- `Authorization: Bearer {refreshToken}`

The refresh flow detects whether the token belongs to a platform admin or a tenant user and issues a new access token accordingly.

