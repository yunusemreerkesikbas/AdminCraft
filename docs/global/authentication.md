# Authentication

AdminCraft supports two login modes via the same endpoint:

- **Tenant user login** (tenant-scoped)
- **Platform admin login (SUPER_ADMIN)** (no tenant)

## Source of Truth

| Component | Location |
|-----------|----------|
| Auth Controller | [`backend/.../controller/AuthController.java`](../../backend/src/main/java/com/backend/presentation/controller/AuthController.java) |
| Auth Service | [`backend/.../service/impl/AuthenticationServiceImpl.java`](../../backend/src/main/java/com/backend/application/service/impl/AuthenticationServiceImpl.java) |
| Email Service | [`backend/.../service/impl/EmailServiceImpl.java`](../../backend/src/main/java/com/backend/application/service/impl/EmailServiceImpl.java) |
| OTP Service | [`backend/.../service/impl/OtpServiceImpl.java`](../../backend/src/main/java/com/backend/application/service/impl/OtpServiceImpl.java) |
| Trusted Device Service | [`backend/.../service/impl/TrustedDeviceServiceImpl.java`](../../backend/src/main/java/com/backend/application/service/impl/TrustedDeviceServiceImpl.java) |
| Verification Token Entity | [`backend/.../entity/VerificationToken.java`](../../backend/src/main/java/com/backend/domain/entity/VerificationToken.java) |
| Email Templates | `backend/src/main/resources/templates/email/` |

## API Endpoints

Base path: `/api/auth`

| Method | Path | Description | Auth Required |
|--------|------|-------------|---------------|
| `POST` | `/login` | User login (may trigger 2FA) | No |
| `POST` | `/verify-otp` | Verify OTP code for 2FA | No |
| `POST` | `/refresh` | Refresh access token | Bearer token |
| `POST` | `/logout` | Logout user | Bearer token |
| `POST` | `/forgot-password` | Request password reset email | No |
| `POST` | `/reset-password` | Reset password with token | No |
| `GET` | `/verify-reset-token` | Validate reset token | No |
| `POST` | `/set-initial-password` | New user sets password | No |

## Tenant User Login

Tenant can be supplied in two ways:

1) Header-based:
   - `X-Tenant-ID: {id}` (takes precedence)
   - `X-Tenant-Subdomain: {subdomain}`

2) Body-based fallback:
   - `LoginRequest.subdomain` (used when `X-Tenant-Subdomain` is not provided)

### Special case: `admin` subdomain

If `subdomain == "admin"`, the system routes the login attempt to **platform admin authentication**.

## Platform Admin Login (SUPER_ADMIN)

If no tenant identifier is provided (`tenantId == null` and empty subdomain), the login attempt is treated as a platform admin login.

Platform admins are stored in the platform database (`platform_management`) and receive JWTs with:
- `role = SUPER_ADMIN`
- `tenantId = null`

## Refresh Token

Token refresh uses the Authorization header:
- `Authorization: Bearer {refreshToken}`

The refresh flow detects whether the token belongs to a platform admin or a tenant user and issues a new access token accordingly.

---

## Two-Factor Authentication (2FA)

AdminCraft supports tenant-level 2FA with email OTP and trusted device management.

### 2FA Policy Levels

Configured per tenant via Site Dashboard → Security tab:

| Policy | Behavior |
|--------|----------|
| `DISABLED` | 2FA not used, standard login |
| `REQUIRED` | 2FA mandatory for all tenant users |

### Login Flow with 2FA

```
POST /api/auth/login
├── Validate credentials
├── Check tenant 2FA policy
│   └── If DISABLED → Return JWT immediately
│   └── If REQUIRED → Always require 2FA
├── Check trusted device (if 2FA required)
│   └── If trusted → Return JWT immediately
│   └── If not trusted → Generate OTP
├── Send OTP email
└── Return { requires2FA: true, pendingToken: "..." }
```

### Login Response

**Standard login (no 2FA)**:
```json
{
  "result": "SUCCESS",
  "data": {
    "accessToken": "eyJ...",
    "refreshToken": "eyJ...",
    "tokenType": "Bearer",
    "expiresIn": 3600
  }
}
```

**2FA required**:
```json
{
  "result": "SUCCESS",
  "data": {
    "requires2FA": true,
    "pendingToken": "abc123-session-token",
    "maskedEmail": "u***@example.com",
    "subdomain": "acme",
    "tenantId": 1
  }
}
```

### OTP Verification

```
POST /api/auth/verify-otp

Request:
{
  "pendingToken": "abc123-session-token",
  "otpCode": "123456",
  "subdomain": "acme",
  "tenantId": 1,
  "trustDevice": true,
  "deviceFingerprint": "sha256-hash"
}

Response (success):
{
  "result": "SUCCESS",
  "data": {
    "accessToken": "eyJ...",
    "refreshToken": "eyJ...",
    ...
  }
}
```

**Validation**:
- `deviceFingerprint`: Max 128 chars, alphanumeric with underscore/hyphen only (`[A-Za-z0-9_-]*`)
- `otpCode`: Exactly 6 digits

**Error Responses**:
- `401 Unauthorized`: Invalid OTP code or expired session
- `429 Too Many Requests`: OTP request rate limit exceeded (3 per 5 minutes)

### OTP Configuration

| Parameter | Value | Environment |
|-----------|-------|-------------|
| OTP Length | 6 digits | All |
| OTP Expiry | 5 minutes | All |
| Max Attempts | 5 | All |
| Request Rate Limit | 3 per 5 minutes | All |
| Rate Limit Cleanup | Every 5 minutes | All |
| Bypass Code | `123456` | Dev only (auto-disabled in prod) |

Configuration in `application.yml`:
```yaml
app:
  otp:
    length: 6
    expiry-seconds: 300
    max-attempts: 5
    bypass-code: null  # Set to "123456" in dev profile
```

**Security Notes**:
- OTP codes are stored as SHA-256 hashes (never plaintext)
- Bypass code is automatically disabled in non-dev profiles via `@PostConstruct` validation
- Rate limiting: Max 3 OTP requests per email per 5-minute window (returns HTTP 429)

---

## Trusted Devices

Users can opt to "trust this device" during OTP verification, skipping 2FA for future logins from the same device.

### Device Fingerprint

Frontend generates a device fingerprint using:
- Browser name and version
- Operating system
- Screen resolution
- Timezone
- Canvas fingerprint hash

The fingerprint is SHA-256 hashed before sending to backend.

### Trusted Device Lifecycle

| Parameter | Value |
|-----------|-------|
| Trust Duration | 30 days |
| Storage | `trusted_devices` table (tenant DB) |
| Cleanup | Automatic expiry check on login |

### Database Schema

```sql
CREATE TABLE trusted_devices (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    device_fingerprint VARCHAR(64) NOT NULL,
    device_name VARCHAR(100),
    browser VARCHAR(50),
    os VARCHAR(50),
    ip_address VARCHAR(45),
    trusted_at DATETIME,
    last_used_at DATETIME,
    expires_at DATETIME NOT NULL,
    UNIQUE KEY uk_user_device (user_id, device_fingerprint),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

---

## Password Reset (Token-Based)

Secure password reset flow using email tokens.

### Flow

```
1. POST /api/auth/forgot-password { "email": "user@example.com" }
   └── Generate token, send email
   └── Response: { "result": "SUCCESS", "message": "Reset link sent" }

2. User clicks email link → Frontend reset-password page
   └── GET /api/auth/verify-reset-token?token=abc123
   └── Response: { "valid": true, "email": "u***@example.com" }

3. POST /api/auth/reset-password
   {
     "token": "abc123",
     "password": "NewPass123!",
     "confirmPassword": "NewPass123!"
   }
   └── Response: { "result": "SUCCESS", "message": "Password changed" }
```

### Token Configuration

| Parameter | Value |
|-----------|-------|
| Token Type | UUID (stored as SHA-256 hash) |
| Expiry | 1 hour |
| Single Use | Yes (marked USED after reset) |

---

## Email Verification (New Users)

New users created by admin receive a verification email to set their password.

### Flow

```
1. Admin creates user (no password)
   └── POST /api/users { email, firstName, ... } (no password fields)
   └── User created with email_verified=false, temporary password hash generated
   └── Note: Temporary hash is passwordEncoder.encode(UUID.randomUUID().toString())
   └── User cannot login with temporary password (unknown value)
   └── Verification email sent automatically

2. User clicks email link → Frontend set-password page
   └── Token validated
   └── User sets password

3. POST /api/auth/set-initial-password
   {
     "token": "abc123",
     "password": "SecurePass123!",
     "confirmPassword": "SecurePass123!"
   }
   └── email_verified=true, password set
   └── User can now login
```

### Token Configuration

| Parameter | Value |
|-----------|-------|
| Token Type | `EMAIL_VERIFY` |
| Expiry | 24 hours |
| Single Use | Yes |

---

## Verification Tokens

All email-based verification uses the `verification_tokens` table.

### Token Types

| Type | Purpose | Expiry |
|------|---------|--------|
| `EMAIL_VERIFY` | New user email verification | 24 hours |
| `PASSWORD_RESET` | Password reset | 1 hour |
| `LOGIN_OTP` | 2FA login OTP | 5 minutes |
| `OPERATION_OTP` | Sensitive operation OTP | 5 minutes |

### Token Status

| Status | Description |
|--------|-------------|
| `ACTIVE` | Token is valid and can be used |
| `USED` | Token has been consumed |
| `EXPIRED` | Token has passed expiry time |
| `REVOKED` | Token manually invalidated |

### Database Schema

```sql
CREATE TABLE verification_tokens (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    token_type ENUM('EMAIL_VERIFY', 'PASSWORD_RESET', 'LOGIN_OTP', 'OPERATION_OTP'),
    status ENUM('ACTIVE', 'USED', 'EXPIRED', 'REVOKED'),
    target_value VARCHAR(255),  -- OTP: SHA-256 hash; PASSWORD_RESET/EMAIL_VERIFY: null
    expires_at DATETIME NOT NULL,
    attempt_count INT DEFAULT 0,
    max_attempts INT DEFAULT 5,
    used_at DATETIME,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    created_at DATETIME,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

**Note**:
- For `LOGIN_OTP` and `OPERATION_OTP` tokens, the `target_value` field stores the SHA-256 hash of the OTP code, not the plaintext value. This ensures OTP codes cannot be extracted from the database.
- For `PASSWORD_RESET` and `EMAIL_VERIFY` tokens, `target_value` is `null`. The plaintext token is only returned to the caller for email sending and is never stored in the database.

---

## Email Service

### Providers

| Provider | Usage | Configuration |
|----------|-------|---------------|
| `smtp` | Production | JavaMailSender with SMTP credentials |
| `console` | Development | Logs email content to console |

### Email Templates

Located in `backend/src/main/resources/templates/email/`:

| Template | Purpose |
|----------|---------|
| `otp-login-{lang}.html` | 2FA OTP code |
| `password-reset-{lang}.html` | Password reset link |
| `email-verify-{lang}.html` | New user verification |

Languages supported: `tr`, `en`

### Configuration

```yaml
# application-prod.yml
spring:
  mail:
    host: ${SMTP_HOST}
    port: ${SMTP_PORT:587}
    username: ${SMTP_USERNAME}
    password: ${SMTP_PASSWORD}
    properties:
      mail.smtp.starttls.enable: true

app:
  email:
    enabled: true
    provider: smtp
    from-address: noreply@admincraft.com
    from-name: AdminCraft

# application-dev.yml
app:
  email:
    provider: console
    log-content: true
```

---

## Frontend Integration

### Components

**Location**: `storefront/src/app/modules/auth/`

| Component | Route | Purpose |
|-----------|-------|---------|
| `sign-in/` | `/sign-in` | User login with 2FA support |
| `forgot-password/` | `/forgot-password` | Request password reset email |
| `reset-password/` | `/reset-password?token=...` | Reset password with token |
| `set-password/` | `/set-password?token=...` | New user sets initial password |

### Sign-In Component (2FA)

**Features**:
- Standard email/password login
- OTP verification form (shown when 2FA required)
- Device fingerprint generation
- "Trust this device" checkbox
- Auto-redirect after OTP verification

**Flow**:
```typescript
1. User enters credentials → signIn()
2. If requires2FA === true:
   - Show OTP form
   - User enters 6-digit code
   - Optional: Check "Trust this device"
   - Call verifyOtp()
3. If successful → Redirect to dashboard
```

### Reset Password Component

**Location**: `storefront/src/app/modules/auth/reset-password/`

**Features**:
- Extracts token from URL query parameter (`?token=...`)
- Validates token on component init
- Shows loading state during validation
- Password form with confirmation
- Password visibility toggle
- Min 8 characters validation
- Auto-redirect to sign-in after success (3 seconds)

**Implementation**:
```typescript
ngOnInit():
  - Extract token from route.snapshot.queryParamMap
  - Call authService.verifyResetToken(token)
  - If valid → Show password form
  - If invalid → Show error alert

resetPassword():
  - Validate passwords match
  - Call authService.resetPassword(token, password, confirmPassword)
  - On success → Show success message → Redirect to /sign-in
```

### Set Password Component (New Users)

**Location**: `storefront/src/app/modules/auth/set-password/`

**Features**:
- Extracts token from URL query parameter (`?token=...`)
- Validates email verification token on component init
- Shows masked email address
- Password form with confirmation
- Password visibility toggle
- Min 8 characters validation
- Auto-redirect to sign-in after success (3 seconds)

**Implementation**:
```typescript
ngOnInit():
  - Extract token from route.snapshot.queryParamMap
  - Call authService.verifyEmailToken(token)
  - If valid → Show password form + masked email
  - If invalid → Show error alert

setPassword():
  - Validate passwords match
  - Call authService.setInitialPassword(token, password, confirmPassword)
  - On success → Show success message → Redirect to /sign-in
```

### Forgot Password Component

**Location**: `storefront/src/app/modules/auth/forgot-password/`

**Features**:
- Email input form
- Email validation
- Success message (always shown for security)
- Loading state during API call

**Implementation**:
```typescript
sendResetLink():
  - Validate email format
  - Call authService.forgotPassword(email)
  - Show generic success message (even if email not found)
  - Reset form
```

### Auth Service Methods

**Location**: `storefront/src/app/core/auth/auth.service.ts`

```typescript
// Password Reset
forgotPassword(email: string): Observable<any>
verifyResetToken(token: string): Observable<any>
resetPassword(token: string, password: string, confirmPassword: string): Observable<any>

// Email Verification
verifyEmailToken(token: string): Observable<any>
setInitialPassword(token: string, password: string, confirmPassword: string): Observable<any>

// 2FA
signIn(credentials): Observable<boolean | 'requires2FA'>
verifyOtp(request: VerifyOtpRequest): Observable<boolean>
cancel2FA(): void
```

### Device Fingerprint Generation

**Location**: `storefront/src/app/modules/auth/sign-in/sign-in.component.ts`

```typescript
async generateDeviceFingerprint(): Promise<string> {
  const components = [
    navigator.userAgent,
    navigator.language,
    screen.colorDepth,
    screen.width + 'x' + screen.height,
    new Date().getTimezoneOffset(),
  ];

  const fingerprint = components.join('|');
  const encoder = new TextEncoder();
  const data = encoder.encode(fingerprint);
  const hashBuffer = await crypto.subtle.digest('SHA-256', data);
  const hashArray = Array.from(new Uint8Array(hashBuffer));
  return hashArray.map(b => b.toString(16).padStart(2, '0')).join('');
}
```

### Routes Configuration

**Location**: `storefront/src/app/app.routes.ts`

```typescript
{
  path: '',
  children: [
    { path: 'sign-in', loadChildren: () => import('.../sign-in.routes') },
    { path: 'forgot-password', loadChildren: () => import('.../forgot-password.routes') },
    { path: 'reset-password', loadChildren: () => import('.../reset-password.routes') },
    { path: 'set-password', loadChildren: () => import('.../set-password.routes') },
  ]
}
```

---

## Security Considerations

### Token Security

- All tokens stored as SHA-256 hashes in `token_hash` column
- OTP codes (LOGIN_OTP, OPERATION_OTP): stored as SHA-256 hash in `target_value`
- PASSWORD_RESET and EMAIL_VERIFY: `target_value` is `null` (plaintext never stored in DB)
- Tokens are single-use (status changes to USED after consumption)
- Automatic expiry enforcement
- Rate limiting on verification attempts (max 5)

### OTP Security

- 6-digit numeric codes (cryptographically random)
- **Hash-based storage** (OTP codes stored as SHA-256 hash, never plaintext)
- 5-minute expiry window
- Max 5 verification attempts before token invalidation
- **Request rate limiting**: Max 3 OTP requests per email per 5-minute window
- **Rate limiter cleanup**: Scheduled task removes expired entries every 5 minutes (prevents memory leak)
- IP address and user agent logged
- **Bypass code protection**: Automatically disabled in non-dev profiles

### Password Security

- Minimum 8 characters required
- **Complexity requirements**: At least 1 lowercase, 1 uppercase, and 1 digit
- Pattern: `^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$`
- BCrypt hashing for storage
- Password confirmation validated at DTO layer via `@AssertTrue`

### Device Trust

- Device fingerprint is SHA-256 hashed
- **Fingerprint validation**: Alphanumeric with underscore/hyphen only, max 128 chars
- 30-day trust expiry
- Automatic cleanup of expired devices
- User can revoke all trusted devices

### Input Validation

- **X-Forwarded-For validation**: IP addresses validated against IPv4/IPv6 patterns
- **Device fingerprint pattern**: Only `[A-Za-z0-9_-]` characters allowed
- Invalid inputs are rejected or sanitized before processing

### Audit Trail

All authentication events are logged with:
- User ID
- IP address (validated format)
- User agent
- Timestamp
- Success/failure status

### PII Protection (GDPR Compliance)

**Logging Policy**:
- ❌ **No raw email addresses in logs**
- ✅ **userId** used for user identification
- ✅ **maskEmail()** used when email display is necessary (e.g., `u***@example.com`)
- ✅ **MDC Context** populated in all tenant operations:
  - `tenantId`: Tenant identifier
  - `tenantDb`: Tenant database name
  - `correlationId`: Request correlation UUID

**MDC Population**: All TenantContext blocks populate MDC for distributed tracing and log correlation.

**Example Log Output**:
```
[tenantId:1][tenantDb:ac_tenant_1][correlationId:7f3a9d2e...] Login successful for userId: 42
```

### Input Validation

**Query Parameters**:
- All token query parameters validated with `@NotBlank`
- Routes: `/verify-reset-token?token=...`, `/verify-email-token?token=...`
- Invalid/missing tokens return HTTP 400 Bad Request

**Device Fingerprint**:
- Pattern: `[A-Za-z0-9_-]{1,128}`
- Max length: 128 characters
- Alphanumeric with underscore/hyphen only

**IP Address Validation**:
- X-Forwarded-For header validated against IPv4/IPv6 patterns
- Invalid IPs rejected or sanitized

### Tenant Security

**Active Status Validation**:
- Inactive tenants blocked from all token operations:
  - `refreshToken()`: Checks tenant status before issuing new tokens
  - `validateResetToken()`: Ensures tenant is ACTIVE before validation
  - `validateEmailVerificationToken()`: Ensures tenant is ACTIVE before validation
- Prevents token abuse after tenant suspension

---

## reCAPTCHA v3 Protection

AdminCraft provides **per-tenant reCAPTCHA v3** bot protection for authentication endpoints. Each tenant configures their own Google keys via Site Dashboard.

> **See also**: [`public-tenant-config.md`](public-tenant-config.md) for frontend integration patterns.

### Architecture Overview

```mermaid
sequenceDiagram
    participant User
    participant Frontend
    participant PublicConfigAPI as /api/config/public
    participant AuthAPI as /api/auth/login
    participant Google as Google reCAPTCHA

    User->>Frontend: Navigate to login page
    Frontend->>PublicConfigAPI: GET (with X-Tenant-Subdomain)
    PublicConfigAPI-->>Frontend: { recaptcha: { enabled, siteKey } }
    Frontend->>Frontend: Cache config in sessionStorage
    
    User->>Frontend: Submit credentials
    Frontend->>Google: Execute reCAPTCHA (if enabled)
    Google-->>Frontend: Token
    Frontend->>AuthAPI: POST { email, password, recaptchaToken }
    AuthAPI->>Google: Verify token with secret key
    Google-->>AuthAPI: { success, score, action }
    AuthAPI-->>Frontend: JWT or Error
```

### Database Schema

reCAPTCHA settings stored in `sites` table:

| Column | Type | Description |
|--------|------|-------------|
| `recaptcha_enabled` | BOOLEAN | Master switch (default: false) |
| `recaptcha_site_key` | VARCHAR(100) | Public key (visible to frontend) |
| `recaptcha_secret_key_encrypted` | VARCHAR(255) | AES-256 encrypted private key |
| `recaptcha_threshold` | DECIMAL(3,2) | Min score 0.0-1.0 (default: 0.5) |

**Migration**: [`V30__add_recaptcha_to_sites.sql`](../../backend/src/main/resources/db/tenant/core/V30__add_recaptcha_to_sites.sql)

**Security**: Secret keys encrypted via `EncryptionService` with master key from `app.encryption.secret-key` env var.

### Protected Endpoints

| Endpoint | Action | Behavior |
|----------|--------|----------|
| `POST /api/auth/login` | `login` | Requires token if enabled |
| `POST /api/auth/forgot-password` | `forgot_password` | Requires token if enabled |
| `POST /api/auth/reset-password` | `reset_password` | Requires token if enabled |
| `POST /api/auth/set-initial-password` | `set_password` | Requires token if enabled |

### Configuration

**Admin UI**: Site Dashboard → Security Tab → reCAPTCHA Protection

**Get keys**: https://www.google.com/recaptcha/admin

### Backend Implementation

**Service**: `RecaptchaServiceImpl`
- Checks if enabled for tenant's site
- Decrypts secret key
- Calls Google API: `https://www.google.com/recaptcha/api/siteverify`
- Validates: `success=true`, `score >= threshold`, `action` matches

**Fail Strategy**:
- Config loading: **Fail-open** (return disabled config if API fails)
- Verification: **Fail-closed** (block request if enabled but token missing/invalid)

### Frontend Implementation

**Minimal Example**:

```typescript
// 1. Load config in ngOnInit
ngOnInit(): void {
    const subdomain = this.#tenantContext.extractSubdomainFromHost();
    this.#publicConfigService.loadConfig(subdomain)
        .pipe(take(1))
        .subscribe(config => this.recaptchaConfigSig.set(config.recaptcha));
}

// 2. Generate token before login
async #getRecaptchaToken(): Promise<string | undefined> {
    const config = this.recaptchaConfigSig();
    if (!config?.enabled || !config.siteKey) return undefined;
    
    return await this.#recaptchaService.execute('login', config.siteKey);
}

// 3. Send with credentials
const credentials = {
    email, password,
    recaptchaToken: await this.#getRecaptchaToken()
};
```

**Services**:
- `PublicTenantConfigService`: Loads config from `/api/config/public`, caches in sessionStorage
- `RecaptchaService`: Loads Google script, executes reCAPTCHA, returns token

### Testing

**Development** (Google test keys - always pass):
```
Site Key:   6LeIxAcTAAAAAJcZVRqyHh71UMIEGNQ_MXjiZKhI
Secret Key: 6LeIxAcTAAAAAGG-vFI1TnRWxMZNFuojJ4WifJWe
```

**Production**:
1. Create keys at Google reCAPTCHA console
2. Configure in Site Dashboard
3. Monitor scores and adjust threshold (start at 0.5)

### Monitoring

**Backend Logs**:
```
INFO  - reCAPTCHA verification passed: action=login, score=0.9, threshold=0.5
WARN  - reCAPTCHA verification failed: action=login, score=0.3, threshold=0.5
```

**Google Console**: Track requests, score distribution, suspicious patterns

---

