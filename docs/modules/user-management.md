# User Management

## Purpose

User Management provides tenant-level user administration with role-based access control (RBAC). Features include:

- **CRUD operations** - Create, read, update, delete users
- **Role assignment** - Assign roles with specific permissions
- **Password management** - Reset and change password flows
- **Account status** - Activate/deactivate users
- **Account security** - Automatic lock on failed login attempts

## Database

**Migrations**: `backend/src/main/resources/db/tenant/core/`

- `V1__baseline.sql` - Initial schema (users table)
- `R__seed_roles.sql` - Repeatable role data
- `R__seed_system_user.sql` - System user initialization
- `V25__remove_preferred_language.sql` - Remove user-level language preference

**Users Table Schema**:

| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT | Primary key |
| email | VARCHAR(255) | Unique per tenant |
| password_hash | VARCHAR(60) | BCrypt hash |
| full_name | VARCHAR(100) | Derived display name (first/last/email) |
| first_name | VARCHAR(50) | Optional |
| last_name | VARCHAR(50) | Optional |
| role | ENUM | User role |
| phone | VARCHAR(20) | Optional |
| job_title | VARCHAR(100) | Optional |
| department | VARCHAR(100) | Optional |
| is_active | BOOLEAN | Account status |
| email_verified | BOOLEAN | Email verification status |
| two_factor_enabled | BOOLEAN | 2FA status |
| password_changed_at | DATETIME | Last password change |
| last_login_at | DATETIME | Last successful login |
| last_login_ip | VARCHAR(45) | Last login IP |
| failed_login_attempts | INT | Failed login counter |
| locked_until | DATETIME | Lock expiration (null if not locked) |
| notes | VARCHAR(500) | Admin notes |
| created_at | DATETIME | Creation timestamp |
| updated_at | DATETIME | Last update timestamp |
| created_by | BIGINT | Creator user ID |
| updated_by | BIGINT | Last updater user ID |

## Admin API

**Base path**: `/api/users`

**Authentication**: All endpoints require `TENANT_ADMIN` role

### List Users (Paginated, Searchable, Sortable)

```
GET /api/users?page=0&size=20&sort=createdAt,desc&search=admin
```

**Query Parameters**:

| Parameter | Default | Description |
|-----------|---------|-------------|
| page | 0 | Zero-based page index |
| size | 20 | Items per page (max: 100) |
| sort | createdAt,desc | Sort expression |
| search | - | Search across display name, email, phone, jobTitle, department |

**Response**: `PageableResponse<UserResponse>`

```json
{
  "result": "SUCCESS",
  "data": {
    "content": [...],
    "page": 0,
    "size": 20,
    "totalElements": 45,
    "totalPages": 3,
    "sortConfig": {
      "currentSort": "createdAt,desc",
      "availableSorts": [...]
    }
  }
}
```

**Sortable Fields**: `createdAt`, `email`, `role`, `isActive`, `lastLoginAt`

### Get User by ID

```
GET /api/users/{id}
```

### Get User by Email

```
GET /api/users/email/{email}
```

### Get Current User

```
GET /api/users/current
```

### Create User

```
POST /api/users
```

**Request Body**: `CreateUserRequest`

```json
{
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "role": "VIEWER",
  "phone": "+90 555 123 4567",
  "jobTitle": "Developer",
  "department": "Engineering",
  "isActive": true,
  "notes": "Optional notes"
}
```

**Validation**:

- Email: Required, valid format, unique per tenant
- Role: Required enum value

**Note**: Password is **not** set during user creation. The user receives an email verification link to set their own password. See [Email Verification Flow](#email-verification-new-users) below.

**Note**: `full_name` is derived server-side from first/last name (fallback: email).

**Response**: `UserResponse` (201 Created)

### Update User

```
PUT /api/users/{id}
```

**Request Body**: `UpdateUserRequest` (partial update - all fields optional)

```json
{
  "firstName": "John",
  "lastName": "Smith",
  "jobTitle": "Senior Developer",
  "isActive": false
}
```

### Delete User

```
DELETE /api/users/{id}
```

### Activate User

```
POST /api/users/{id}/activate
```

### Deactivate User

```
POST /api/users/{id}/deactivate
```

### Reset Password (Admin-Initiated)

```
POST /api/users/{id}/reset-password
```

Sends a password reset email to the user with a secure token link.

**Response**:

```json
{
  "result": "SUCCESS",
  "message": "Password reset email sent."
}
```

The user receives an email with a secure link to set a new password. The token expires after 1 hour.

See [authentication.md](../global/authentication.md#password-reset-token-based) for full password reset documentation.

### Self-Service Password Reset (Forgot Password)

Users can also reset their password via the login page:

```
POST /api/auth/forgot-password
{ "email": "user@example.com" }
```

This is documented in [authentication.md](../global/authentication.md#password-reset-token-based).

### Change Password (Manual)

```
POST /api/users/{id}/change-password
```

**Request Body**: `ChangePasswordRequest`

```json
{
  "currentPassword": "OldPassword123!",
  "password": "NewPassword123!",
  "confirmPassword": "NewPassword123!"
}
```

**Validation**:

- Current password must be correct
- New password and confirm password must match (`@AssertTrue isPasswordMatching()`)
- New password must be at least 8 characters
- New password must contain at least 1 lowercase, 1 uppercase, and 1 digit

## DTOs

**Source**: `backend/src/main/java/com/backend/presentation/dto/`

### UserResponse

Response DTO (excludes `passwordHash` for security):

- id, email, firstName, lastName, role
- phone, jobTitle, department, notes
- isActive, emailVerified, twoFactorEnabled
- lastLoginAt, lastLoginIp, failedLoginAttempts, accountLocked
- createdAt, updatedAt
- displayName (computed), isSuperAdmin, isTenantAdmin

### CreateUserRequest

Create request with email, role, and optional profile fields. Password is **not** included - users set their own password via email verification.

### UpdateUserRequest

Partial update request (inputs trimmed in constructor).

### ChangePasswordRequest

Password change request with current password verification and `@PasswordMatch`.

### ResetPasswordResponse

Response containing the auto-generated password after a reset.

## User Roles

Defined in [`UserRole.java`](../../backend/src/main/java/com/backend/domain/enums/UserRole.java):

| Role | Description | Permissions |
|------|-------------|-------------|
| SUPER_ADMIN | Platform-wide access | All tenants |
| TENANT_ADMIN | Full tenant access | User management, all modules |
| EDITOR | Content editing | Read/write content |
| VIEWER | Read-only access | Read content only |

## Frontend Integration

**Source**: `storefront/src/app/modules/admin/custom/users/`

### Components

- `list/users-list.component.ts` - Main list view with grid, pagination, search, sort
- `dialogs/user-form-dialog/` - Create/Edit dialog with password visibility toggle
- `dialogs/user-password-dialog/` - Change/Reset password dialog

### Services

- `users.service.ts` - Extends `CrudHttpService` (pagination, search, sort)
- `services/user.store.ts` - Extends `CrudStore` (Signal-based state)

### Architecture

- **Angular 19 Signals** for reactive state management
- **Base Components** inheritance (`BasePaginatedListComponent`)
- **Dialog-based Forms** (Material Dialog)
- **Server-side Operations** (pagination, search, sort)
- **Grid Component** (`SpaAdminGrid` for declarative tables)

## Security & Tenant Isolation

### Access Control

- All endpoints require `@PreAuthorize("hasRole('TENANT_ADMIN')")`
- Users are scoped to tenant database (`ac_tenant_{id}`)
- Email uniqueness enforced per tenant

### Password Security

- Passwords hashed with BCrypt before storage
- Password hashes **never** exposed in API responses
- JPQL queries use parameterized search (SQL injection protection)
- **Password complexity requirements**:
  - Minimum 8 characters
  - At least 1 lowercase letter
  - At least 1 uppercase letter
  - At least 1 digit
  - Pattern: `^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$`

### Email Verification

- New users must verify their email before login
- `email_verified` field is checked during authentication
- Users with `email_verified=false` cannot login (returns `InvalidCredentialsException` for security)

### Account Lock Mechanism

Automatic account locking protects against brute-force attacks:

**Configuration**:

| Setting | Value |
|---------|-------|
| Max Failed Attempts | 5 |
| Lock Duration | 30 minutes |
| Applies to | All user roles |

**Flow**:

1. User enters wrong password → `failedLoginAttempts` increments
2. After 5 failed attempts → `lockedUntil` set to current time + 30 minutes
3. Locked user attempts login → `AccountLockedException` thrown with remaining minutes
4. Successful login → `failedLoginAttempts` reset to 0, `lockedUntil` cleared

**API Response** (when locked):

```json
{
  "result": "ERROR",
  "message": "Your account has been locked due to multiple failed login attempts. Please try again in 25 minutes.",
  "data": {
    "errorCode": "ACCOUNT_LOCKED",
    "remainingMinutes": 25
  }
}
```

**Frontend Handling**:

- Detects `ACCOUNT_LOCKED` error code
- Shows warning notification (yellow) with 10 second duration
- Message includes remaining lock time

**Key Methods** ([`User.java`](../../backend/src/main/java/com/backend/domain/entity/User.java)):

| Method | Description |
|--------|-------------|
| `isAccountLocked()` | Returns true if `lockedUntil` is in the future |
| `getRemainingLockMinutes()` | Calculates remaining lock time |
| `recordFailedLogin()` | Increments counter, sets lock if threshold reached |
| `recordSuccessfulLogin(ip)` | Resets counter, clears lock, updates last login |

**Note**: Lock is temporary and auto-expires. Unlike deactivation, users don't need admin intervention to regain access after the lock period.

## Implementation Guide

### Creating a New User

1. **Frontend**:
   - User clicks "Create User" → Opens `UserFormDialogComponent`
   - Form validates: email format, required fields (no password fields)
   - On submit → Calls `UsersService.create(CreateUserRequest)`

2. **Backend**:
   - Controller validates DTO (`@Valid CreateUserRequest`)
   - Service checks email uniqueness
   - User saved with `email_verified=false`, `password=null`
   - Verification email sent automatically via `EmailService`
   - Returns `UserResponse` (201 Created)

3. **Frontend**:
   - Success notification shown (includes email sent info)
   - Dialog closes
   - Users list auto-refreshes via `loadItems()`

4. **User Email Verification**:
   - New user receives verification email with secure link
   - User clicks link → Set Password page
   - User sets their password → `email_verified=true`, password saved
   - User can now login

See [authentication.md](../global/authentication.md#email-verification-new-users) for full email verification documentation.

### Searching and Sorting Users

1. **User Input**: Types in search box or selects sort option
2. **Debounced Request**: 300ms delay before API call
3. **API Call**: `GET /api/users?search=john&sort=fullName,asc&page=0&size=20`
4. **Server-side Processing**:
   - JPQL search across 5 fields (fullName, email, phone, jobTitle, department)
   - Sort applied by Spring Data JPA
   - Pagination applied
5. **Response**: `PageableResponse` with items, totalElements, sortConfig
6. **UI Update**: Grid re-renders with signals

### Adding a New User Field

Example: Adding a `title` field (e.g., "Dr.", "Prof.")

1. **Database Migration** (`V26__add_user_title.sql`):

```sql
ALTER TABLE users ADD COLUMN title VARCHAR(20) AFTER full_name;
```

2. **Update Entity** (`User.java`):

```java
@Size(max = 20, message = "validation.title.size")
private String title;
```

3. **Update DTOs**:
   - Add to `CreateUserRequest`: `String title`
   - Add to `UpdateUserRequest`: `String title`
   - Add to `UserResponse`: `String title`

4. **Update Service** (`UserServiceImpl.java`):

```java
// In createUser()
user.setTitle(request.title());

// In updateUser()
if (request.title() != null) user.setTitle(request.title());
```

5. **Update Frontend Types** (`users.types.ts`):

```typescript
export interface User extends CrudEntity {
  // ... existing fields
  title?: string;
}
```

6. **Update Dialog** (`user-form-dialog.component.html`):

```html
<spa-input [label]="'Title'" [type]="'text'"
  [ngModel]="form.get('title')?.value"
  (ngModelChange)="form.get('title')?.setValue($event)">
</spa-input>
```

7. **Update Grid** (optional):

```typescript
{
  key: 'title',
  label: 'admin.users.grid.title',
  cellRenderer: (user) => user.title || '-'
}
```
